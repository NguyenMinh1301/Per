package com.per.category.service.impl;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.per.category.document.CategoryDocument;
import com.per.category.entity.Category;
import com.per.category.mapper.CategoryDocumentMapper;
import com.per.category.repository.CategoryRepository;
import com.per.category.repository.CategorySearchRepository;
import com.per.category.service.CategorySearchService;
import com.per.common.response.PageResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorySearchServiceImpl implements CategorySearchService {

    private final ElasticsearchOperations esOperations;
    private final CategorySearchRepository searchRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryDocumentMapper documentMapper;

    @Override
    public PageResponse<CategoryDocument> search(String query, Pageable pageable) {
        Query esQuery = buildSearchQuery(query);

        NativeQuery nativeQuery =
                NativeQuery.builder().withQuery(esQuery).withPageable(pageable).build();

        SearchHits<CategoryDocument> searchHits =
                esOperations.search(nativeQuery, CategoryDocument.class);

        List<CategoryDocument> results =
                searchHits.getSearchHits().stream().map(SearchHit::getContent).toList();

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<CategoryDocument>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public void index(CategoryDocument document) {
        searchRepository.save(document);
        log.debug("Indexed category: {}", document.getId());
    }

    @Override
    public void delete(String id) {
        searchRepository.deleteById(id);
        log.debug("Deleted category from index: {}", id);
    }

    @Override
    public void reindexAll() {
        log.info("Starting full category reindex");
        searchRepository.deleteAll();

        List<Category> categories = categoryRepository.findAll();
        int indexed = 0;

        for (Category category : categories) {
            try {
                CategoryDocument doc = documentMapper.toDocument(category);
                searchRepository.save(doc);
                indexed++;
            } catch (Exception e) {
                log.error("Failed to index category {}: {}", category.getId(), e.getMessage());
            }
        }

        log.info("Category reindex completed: {} categories indexed", indexed);
    }

    private Query buildSearchQuery(String query) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        boolQuery.filter(f -> f.term(t -> t.field("isActive").value(true)));

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
