package com.per.product.service.impl;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.per.common.response.PageResponse;
import com.per.product.document.ProductDocument;
import com.per.product.dto.request.ProductSearchRequest;
import com.per.product.dto.response.ProductSearchResponse;
import com.per.product.entity.Product;
import com.per.product.mapper.ProductDocumentMapper;
import com.per.product.repository.ProductRepository;
import com.per.product.repository.ProductSearchRepository;
import com.per.product.repository.ProductVariantRepository;
import com.per.product.service.ProductSearchService;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ProductSearchService using Elasticsearch. Provides full-text search with fuzzy
 * matching and filter capabilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchOperations esOperations;
    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductDocumentMapper documentMapper;

    @Override
    public PageResponse<ProductSearchResponse> search(
            ProductSearchRequest request, Pageable pageable) {
        Query query = buildSearchQuery(request);

        NativeQuery nativeQuery =
                NativeQuery.builder().withQuery(query).withPageable(pageable).build();

        SearchHits<ProductDocument> searchHits =
                esOperations.search(nativeQuery, ProductDocument.class);

        List<ProductSearchResponse> results =
                searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .map(this::toResponse)
                        .toList();

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<ProductSearchResponse>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void reindexAll() {
        log.info("Starting full product reindex");

        // Clear existing index
        searchRepository.deleteAll();
        log.info("Cleared existing product index");

        // Fetch all active products and index them
        List<Product> products = productRepository.findAll();
        int indexed = 0;

        for (Product product : products) {
            try {
                var variants = variantRepository.findByProductId(product.getId());
                ProductDocument doc = documentMapper.toDocument(product, variants);
                searchRepository.save(doc);
                indexed++;
            } catch (Exception e) {
                log.error("Failed to index product {}: {}", product.getId(), e.getMessage());
            }
        }

        log.info("Product reindex completed: {} products indexed", indexed);
    }

    private Query buildSearchQuery(ProductSearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Full-text search with fuzzy matching and prefix support
        if (StringUtils.hasText(request.getQuery())) {
            String query = request.getQuery();
            String lowerQuery = query.toLowerCase();
            String wildcardQuery = lowerQuery + "*";

            // Use should with minimum_should_match for flexible matching
            boolQuery.should(
                    // Prefix match on name (highest priority)
                    s -> s.prefix(p -> p.field("name").value(lowerQuery).boost(4.0f)));
            boolQuery.should(
                    // Wildcard match on name
                    s -> s.wildcard(w -> w.field("name").value(wildcardQuery).boost(3.0f)));
            boolQuery.should(
                    // Wildcard match on brand name
                    s -> s.wildcard(w -> w.field("brandName").value(wildcardQuery).boost(2.0f)));
            boolQuery.should(
                    // Fuzzy match for typos
                    s ->
                            s.multiMatch(
                                    mm ->
                                            mm.query(query)
                                                    .fields(
                                                            "name^3",
                                                            "shortDescription^2",
                                                            "description",
                                                            "brandName^2",
                                                            "categoryName")
                                                    .fuzziness("AUTO")));
            boolQuery.should(
                    // Wildcard on description
                    s -> s.wildcard(w -> w.field("shortDescription").value(wildcardQuery)));

            boolQuery.minimumShouldMatch("1");
        }

        // Filters
        if (request.getBrandId() != null) {
            boolQuery.filter(
                    f -> f.term(t -> t.field("brandId").value(request.getBrandId().toString())));
        }

        if (request.getCategoryId() != null) {
            boolQuery.filter(
                    f ->
                            f.term(
                                    t ->
                                            t.field("categoryId")
                                                    .value(request.getCategoryId().toString())));
        }

        if (request.getGender() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("gender").value(request.getGender().name())));
        }

        if (request.getFragranceFamily() != null) {
            boolQuery.filter(
                    f ->
                            f.term(
                                    t ->
                                            t.field("fragranceFamily")
                                                    .value(request.getFragranceFamily().name())));
        }

        if (request.getSillage() != null) {
            boolQuery.filter(
                    f -> f.term(t -> t.field("sillage").value(request.getSillage().name())));
        }

        if (request.getLongevity() != null) {
            boolQuery.filter(
                    f -> f.term(t -> t.field("longevity").value(request.getLongevity().name())));
        }

        if (request.getSeasonality() != null) {
            boolQuery.filter(
                    f ->
                            f.term(
                                    t ->
                                            t.field("seasonality")
                                                    .value(request.getSeasonality().name())));
        }

        if (request.getOccasion() != null) {
            boolQuery.filter(
                    f -> f.term(t -> t.field("occasion").value(request.getOccasion().name())));
        }

        // Price range filter (uses minPrice field for filtering)
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            boolQuery.filter(
                    f ->
                            f.range(
                                    r ->
                                            r.untyped(
                                                    u -> {
                                                        u.field("minPrice");
                                                        if (request.getMinPrice() != null) {
                                                            u.gte(
                                                                    JsonData.of(
                                                                            request.getMinPrice()
                                                                                    .doubleValue()));
                                                        }
                                                        if (request.getMaxPrice() != null) {
                                                            u.lte(
                                                                    JsonData.of(
                                                                            request.getMaxPrice()
                                                                                    .doubleValue()));
                                                        }
                                                        return u;
                                                    })));
        }

        // Active filter (default to true)
        Boolean isActive = request.getIsActive() != null ? request.getIsActive() : true;
        boolQuery.filter(f -> f.term(t -> t.field("isActive").value(isActive)));

        return boolQuery.build()._toQuery();
    }

    private ProductSearchResponse toResponse(ProductDocument doc) {
        return ProductSearchResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .shortDescription(doc.getShortDescription())
                .brandName(doc.getBrandName())
                .categoryName(doc.getCategoryName())
                .gender(doc.getGender())
                .minPrice(doc.getMinPrice())
                .maxPrice(doc.getMaxPrice())
                .imageUrl(doc.getImageUrl())
                .build();
    }
}
