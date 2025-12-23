package com.per.brand.service.impl;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.per.brand.document.BrandDocument;
import com.per.brand.entity.Brand;
import com.per.brand.mapper.BrandDocumentMapper;
import com.per.brand.repository.BrandRepository;
import com.per.brand.repository.BrandSearchRepository;
import com.per.brand.service.BrandSearchService;
import com.per.common.response.PageResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandSearchServiceImpl implements BrandSearchService {

    private final ElasticsearchOperations esOperations;
    private final BrandSearchRepository searchRepository;
    private final BrandRepository brandRepository;
    private final BrandDocumentMapper documentMapper;

    @Override
    public PageResponse<BrandDocument> search(String query, Pageable pageable) {
        Query esQuery = buildSearchQuery(query);

        NativeQuery nativeQuery =
                NativeQuery.builder().withQuery(esQuery).withPageable(pageable).build();

        SearchHits<BrandDocument> searchHits =
                esOperations.search(nativeQuery, BrandDocument.class);

        List<BrandDocument> results =
                searchHits.getSearchHits().stream().map(SearchHit::getContent).toList();

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<BrandDocument>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public void index(BrandDocument document) {
        searchRepository.save(document);
        log.debug("Indexed brand: {}", document.getId());
    }

    @Override
    public void delete(String id) {
        searchRepository.deleteById(id);
        log.debug("Deleted brand from index: {}", id);
    }

    @Override
    public void reindexAll() {
        log.info("Starting full brand reindex");
        searchRepository.deleteAll();

        List<Brand> brands = brandRepository.findAll();
        int indexed = 0;

        for (Brand brand : brands) {
            try {
                BrandDocument doc = documentMapper.toDocument(brand);
                searchRepository.save(doc);
                indexed++;
            } catch (Exception e) {
                log.error("Failed to index brand {}: {}", brand.getId(), e.getMessage());
            }
        }

        log.info("Brand reindex completed: {} brands indexed", indexed);
    }

    private Query buildSearchQuery(String query) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Filter active brands only
        boolQuery.filter(f -> f.term(t -> t.field("isActive").value(true)));

        // Full-text search on name and description
        if (StringUtils.hasText(query)) {
            String lowerQuery = query.toLowerCase();
            String wildcardQuery = lowerQuery + "*";

            // Use should with minimum_should_match for flexible matching
            boolQuery.should(
                    // Prefix match on name (highest priority)
                    s -> s.prefix(p -> p.field("name").value(lowerQuery).boost(3.0f)));
            boolQuery.should(
                    // Wildcard match on name
                    s -> s.wildcard(w -> w.field("name").value(wildcardQuery).boost(2.0f)));
            boolQuery.should(
                    // Fuzzy match for typos
                    s ->
                            s.multiMatch(
                                    mm ->
                                            mm.query(query)
                                                    .fields("name^3", "description")
                                                    .fuzziness("AUTO")));
            boolQuery.should(
                    // Wildcard on description
                    s -> s.wildcard(w -> w.field("description").value(wildcardQuery)));

            boolQuery.minimumShouldMatch("1");
        }

        return Query.of(q -> q.bool(boolQuery.build()));
    }
}
