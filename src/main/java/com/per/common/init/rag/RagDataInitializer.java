package com.per.common.init.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.per.rag.service.DocumentIndexService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automated RAG data initializer that checks vector store state on application startup and triggers
 * indexing if the store is empty. This component ensures the RAG system is ready for use without
 * manual intervention.
 *
 * <p>Execution flow:
 * <ol>
 *   <li>On application startup, performs a lightweight check of vector store document count</li>
 *   <li>If vector store is empty, automatically triggers product and knowledge base indexing</li>
 *   <li>Runs asynchronously to avoid blocking application startup</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Run after database and other critical initializers
public class RagDataInitializer implements CommandLineRunner {

    private final VectorStoreService vectorStoreService;
    private final DocumentIndexService documentIndexService;

    @Value("${app.rag.auto-index:true}")
    private boolean autoIndex;

    @Override
    @Async
    public void run(String... args) {
        if (!autoIndex) {
            log.info("RAG auto-indexing is disabled via configuration");
            return;
        }

        log.info("Verifying vector store state...");

        try {
            // Lightweight check: query document count
            var status = vectorStoreService.getKnowledgeStatus();
            int totalDocs = (Integer) status.getOrDefault("totalDocuments", 0);

            if (totalDocs > 0) {
                log.info(
                        "Vector store verification passed. Found {} documents ({} products, {} knowledge).",
                        totalDocs,
                        status.get("productDocuments"),
                        status.get("knowledgeDocuments"));
                return;
            }

            // Vector store is empty - trigger automatic indexing
            log.warn("Vector store is empty. Initiating automatic indexing...");

            // Index products
            log.info("Indexing products into vector store...");
            vectorStoreService.indexAllProducts();
            log.info("Product indexing completed successfully");

            // Index knowledge base
            log.info("Indexing knowledge base documents...");
            documentIndexService.indexKnowledgeBase();
            log.info("Knowledge base indexing completed successfully");

            // Verify final state
            var finalStatus = vectorStoreService.getKnowledgeStatus();
            log.info(
                    "RAG initialization completed: {} total documents indexed ({} products, {} knowledge)",
                    finalStatus.get("totalDocuments"),
                    finalStatus.get("productDocuments"),
                    finalStatus.get("knowledgeDocuments"));

        } catch (Exception e) {
            log.error(
                    "RAG automatic initialization failed: {}. Manual indexing may be required.",
                    e.getMessage(),
                    e);
        }
    }
}
