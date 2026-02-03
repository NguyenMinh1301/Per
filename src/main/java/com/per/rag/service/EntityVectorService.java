package com.per.rag.service;

import java.util.Map;
import java.util.UUID;

/**
 * Service for managing vector embeddings for Brand and Category entities. Provides methods to index
 * entities into Qdrant collections and query collection status.
 */
public interface EntityVectorService {

    /** Index all active brands into the brand_vectors collection. */
    void indexAllBrands();

    /** Index all active categories into the category_vectors collection. */
    void indexAllCategories();

    /** Index a single brand by ID. */
    void indexBrand(UUID brandId);

    /** Index a single category by ID. */
    void indexCategory(UUID categoryId);

    /** Get status of a specific Qdrant collection. */
    Map<String, Object> getCollectionStatus(String collectionName);

    /** Check if a collection is empty. */
    boolean isCollectionEmpty(String collectionName);
}
