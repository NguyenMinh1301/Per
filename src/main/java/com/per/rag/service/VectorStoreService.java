package com.per.rag.service;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.document.Document;

public interface VectorStoreService {

    /** Index all active products into the vector store */
    void indexAllProducts();

    /**
     * Index a single product by ID
     *
     * @param productId Product UUID
     */
    void indexProduct(UUID productId);

    /**
     * Search for similar documents based on query
     *
     * @param query User query
     * @param topK Number of results to return
     * @param threshold Minimum similarity threshold (0.0-1.0)
     * @return List of similar documents
     */
    List<Document> searchSimilar(String query, int topK, double threshold);

    /**
     * Delete documents from vector store by metadata key-value pair
     *
     * @param key Metadata key
     * @param value Metadata value
     */
    void deleteByMetadata(String key, String value);

    /**
     * Get knowledge base indexing status
     *
     * @return Map with document counts and sources by type
     */
    java.util.Map<String, Object> getKnowledgeStatus();
}
