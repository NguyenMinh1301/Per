package com.per.common.init.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.per.rag.service.DocumentIndexService;
import com.per.rag.service.EntityVectorService;
import com.per.rag.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automated RAG data initializer that checks vector store state on application startup and triggers
 * indexing if empty. Indexes products, brands, categories, and knowledge base documents.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class RagDataInitializer implements CommandLineRunner {

    private final VectorStoreService vectorStoreService;
    private final EntityVectorService entityVectorService;
    private final DocumentIndexService documentIndexService;

    @Value("${app.rag.auto-index:true}")
    private boolean autoIndex;

    @Value("${app.rag.qdrant.collections.product:product_vectors}")
    private String productCollection;

    @Value("${app.rag.qdrant.collections.brand:brand_vectors}")
    private String brandCollection;

    @Value("${app.rag.qdrant.collections.category:category_vectors}")
    private String categoryCollection;

    @Override
    @Async
    public void run(String... args) {
        if (!autoIndex) {
            log.info("RAG auto-indexing is disabled via configuration");
            return;
        }

        log.info("Verifying vector store state for all collections...");

        try {
            // Index products if empty
            indexProductsIfEmpty();

            // Index brands if empty
            indexBrandsIfEmpty();

            // Index categories if empty
            indexCategoriesIfEmpty();

            // Index knowledge base (uses product_vectors collection)
            indexKnowledgeIfEmpty();

            log.info("RAG initialization completed successfully");

        } catch (Exception e) {
            log.error(
                    "RAG automatic initialization failed: {}. Manual indexing may be required.",
                    e.getMessage(),
                    e);
        }
    }

    private void indexProductsIfEmpty() {
        try {
            var status = vectorStoreService.getKnowledgeStatus();
            int productDocs = (Integer) status.getOrDefault("productDocuments", 0);

            if (productDocs == 0) {
                log.info("Product collection is empty. Indexing products...");
                vectorStoreService.indexAllProducts();
                log.info("Product indexing completed");
            } else {
                log.debug("Product collection has {} documents", productDocs);
            }
        } catch (Exception e) {
            log.error("Failed to index products: {}", e.getMessage());
        }
    }

    private void indexBrandsIfEmpty() {
        try {
            if (entityVectorService.isCollectionEmpty(brandCollection)) {
                log.info("Brand collection is empty. Indexing brands...");
                entityVectorService.indexAllBrands();
                log.info("Brand indexing completed");
            } else {
                log.debug("Brand collection already has data");
            }
        } catch (Exception e) {
            log.error("Failed to index brands: {}", e.getMessage());
        }
    }

    private void indexCategoriesIfEmpty() {
        try {
            if (entityVectorService.isCollectionEmpty(categoryCollection)) {
                log.info("Category collection is empty. Indexing categories...");
                entityVectorService.indexAllCategories();
                log.info("Category indexing completed");
            } else {
                log.debug("Category collection already has data");
            }
        } catch (Exception e) {
            log.error("Failed to index categories: {}", e.getMessage());
        }
    }

    private void indexKnowledgeIfEmpty() {
        try {
            var status = vectorStoreService.getKnowledgeStatus();
            int knowledgeDocs = (Integer) status.getOrDefault("knowledgeDocuments", 0);

            if (knowledgeDocs == 0) {
                log.info("Knowledge base is empty. Indexing knowledge documents...");
                documentIndexService.indexKnowledgeBase();
                log.info("Knowledge base indexing completed");
            } else {
                log.debug("Knowledge base has {} documents", knowledgeDocs);
            }
        } catch (Exception e) {
            log.error("Failed to index knowledge base: {}", e.getMessage());
        }
    }
}
