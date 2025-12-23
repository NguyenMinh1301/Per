package com.per.product.service;

import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;
import com.per.product.dto.request.ProductSearchRequest;
import com.per.product.dto.response.ProductSearchResponse;

/**
 * Service interface for Elasticsearch-based product search. Provides full-text search with fuzzy
 * matching and filter capabilities.
 */
public interface ProductSearchService {

    /**
     * Search products with optional full-text query and filters.
     *
     * @param request the search request with query and filters
     * @param pageable pagination parameters
     * @return paginated search results
     */
    PageResponse<ProductSearchResponse> search(ProductSearchRequest request, Pageable pageable);

    /** Reindex all products from PostgreSQL to Elasticsearch. Admin operation. */
    void reindexAll();
}
