package com.per.common.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.per.rag.service.DocumentIndexService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes RAG vector store by indexing products and knowledge base. Listens for
 * OllamaReadyEvent which is published after Ollama models are pulled. This ensures embeddings are
 * available before indexing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(4) // Run after OllamaInitializer
public class RagInitializer {

    private final VectorStoreService vectorStoreService;
    private final DocumentIndexService documentIndexService;

    @Value("${app.rag.auto-index:true}")
    private boolean autoIndex;

    @Async
    @EventListener(OllamaReadyEvent.class)
    public void onOllamaReady(OllamaReadyEvent event) {
        if (!autoIndex) {
            log.info("RAG auto-indexing is disabled");
            return;
        }

        if (!event.isSuccess()) {
            log.warn("Ollama initialization failed, skipping RAG indexing");
            return;
        }

        log.info("Starting RAG vector store indexing...");

        try {
            // Check if already indexed
            var status = vectorStoreService.getKnowledgeStatus();
            int totalDocs = (Integer) status.getOrDefault("totalDocuments", 0);

            if (totalDocs > 0) {
                log.info("Vector store already has {} documents, skipping reindex", totalDocs);
                return;
            }

            // Index products
            log.info("Indexing products into vector store...");
            vectorStoreService.indexAllProducts();
            log.info("Product indexing completed");

            // Index knowledge base
            log.info("Indexing knowledge base documents...");
            documentIndexService.indexKnowledgeBase();
            log.info("Knowledge base indexing completed");

            // Log final status
            var finalStatus = vectorStoreService.getKnowledgeStatus();
            log.info(
                    "RAG indexing completed: {} total documents ({} products, {} knowledge)",
                    finalStatus.get("totalDocuments"),
                    finalStatus.get("productDocuments"),
                    finalStatus.get("knowledgeDocuments"));

        } catch (Exception e) {
            log.error("RAG indexing failed: {}", e.getMessage(), e);
        }
    }
}
