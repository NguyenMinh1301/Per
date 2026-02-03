package com.per.rag.service;

import java.util.List;

import org.springframework.ai.document.Document;

/**
 * Service for performing semantic search across multiple Qdrant collections. Orchestrates retrieval
 * from product, brand, category, and knowledge collections.
 */
public interface MultiCollectionSearchService {

    /** Search brand_vectors collection for relevant brands. */
    List<Document> searchBrands(String query, int topK, double threshold);

    /** Search category_vectors collection for relevant categories. */
    List<Document> searchCategories(String query, int topK, double threshold);

    /** Search product_vectors collection for relevant products. */
    List<Document> searchProducts(String query, int topK, double threshold);

    /** Search knowledge_vectors collection for policies/FAQ. */
    List<Document> searchKnowledge(String query, int topK, double threshold);

    /** Multi-source result container. */
    record MultiSearchResult(
            List<Document> brands,
            List<Document> categories,
            List<Document> products,
            List<Document> knowledge) {}

    /** Perform multi-source search across all collections. */
    MultiSearchResult searchAll(String query, int topK, double threshold);
}
