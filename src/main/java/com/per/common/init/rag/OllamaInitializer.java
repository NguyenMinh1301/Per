package com.per.common.init.rag;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes Ollama models asynchronously after application startup. Pulls embedding and chat
 * models if not already available. Runs in background to avoid slowing down app startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Run after other initializers
public class OllamaInitializer {

    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.options.model:nomic-embed-text}")
    private String embeddingModel;

    @Value("${app.rag.chat-model:llama3.2}")
    private String chatModel;

    @Value("${app.ollama.auto-pull:true}")
    private boolean autoPull;

    @Value("${app.ollama.max-wait-seconds:30}")
    private int maxWaitSeconds;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void initializeModels() {
        if (!autoPull) {
            log.info("Ollama auto-pull is disabled");
            return;
        }

        log.info("Starting Ollama model initialization in background...");

        try {
            // Wait for Ollama to be ready
            if (!waitForOllama()) {
                log.warn("Ollama is not available, skipping model pull");
                return;
            }

            // Pull embedding model
            pullModel(embeddingModel, "embedding");

            // Pull chat model
            pullModel(chatModel, "chat");

            log.info("Ollama model initialization completed");

            // Publish event to trigger RAG indexing
            eventPublisher.publishEvent(new OllamaReadyEvent(this, true));

        } catch (Exception e) {
            log.error("Ollama initialization failed: {}", e.getMessage());
            eventPublisher.publishEvent(new OllamaReadyEvent(this, false));
        }
    }

    private boolean waitForOllama() {
        log.info("Waiting for Ollama to be ready at {}...", ollamaBaseUrl);

        long startTime = System.currentTimeMillis();
        long maxWaitMs = maxWaitSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) URI.create(ollamaBaseUrl).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    log.info("Ollama is ready");
                    return true;
                }
            } catch (Exception ignored) {
                // Ollama not ready yet
            }

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.warn("Ollama did not become ready within {} seconds", maxWaitSeconds);
        return false;
    }

    private void pullModel(String modelName, String modelType) {
        try {
            // Check if model already exists
            if (isModelAvailable(modelName)) {
                log.info("Model '{}' ({}) is already available", modelName, modelType);
                return;
            }

            log.info("Pulling {} model: {}", modelType, modelName);

            ProcessBuilder pb =
                    new ProcessBuilder("docker", "exec", "ollama", "ollama", "pull", modelName);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Log output in real-time
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("Ollama pull: {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Successfully pulled {} model: {}", modelType, modelName);
            } else {
                log.error(
                        "Failed to pull {} model: {} (exit code: {})",
                        modelType,
                        modelName,
                        exitCode);
            }

        } catch (Exception e) {
            log.error("Error pulling {} model {}: {}", modelType, modelName, e.getMessage());
        }
    }

    private boolean isModelAvailable(String modelName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "exec", "ollama", "ollama", "list");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Model names in ollama list include the tag (e.g., "llama3.2:latest")
                    if (line.contains(modelName.split(":")[0])) {
                        return true;
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            log.debug("Could not check model availability: {}", e.getMessage());
        }
        return false;
    }
}
