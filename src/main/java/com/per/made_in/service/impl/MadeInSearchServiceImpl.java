package com.per.made_in.service.impl;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.per.common.response.PageResponse;
import com.per.made_in.document.MadeInDocument;
import com.per.made_in.entity.MadeIn;
import com.per.made_in.mapper.MadeInDocumentMapper;
import com.per.made_in.repository.MadeInRepository;
import com.per.made_in.repository.MadeInSearchRepository;
import com.per.made_in.service.MadeInSearchService;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MadeInSearchServiceImpl implements MadeInSearchService {

    private final ElasticsearchOperations esOperations;
    private final MadeInSearchRepository searchRepository;
    private final MadeInRepository madeInRepository;
    private final MadeInDocumentMapper documentMapper;

    @Override
    public PageResponse<MadeInDocument> search(String query, Pageable pageable) {
        Query esQuery = buildSearchQuery(query);

        NativeQuery nativeQuery =
                NativeQuery.builder().withQuery(esQuery).withPageable(pageable).build();

        SearchHits<MadeInDocument> searchHits =
                esOperations.search(nativeQuery, MadeInDocument.class);

        List<MadeInDocument> results =
                searchHits.getSearchHits().stream().map(SearchHit::getContent).toList();

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<MadeInDocument>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public void index(MadeInDocument document) {
        searchRepository.save(document);
        log.debug("Indexed made_in: {}", document.getId());
    }

    @Override
    public void delete(String id) {
        searchRepository.deleteById(id);
        log.debug("Deleted made_in from index: {}", id);
    }

    @Override
    public void reindexAll() {
        log.info("Starting full made_in reindex");
        searchRepository.deleteAll();

        List<MadeIn> madeIns = madeInRepository.findAll();
        int indexed = 0;

        for (MadeIn madeIn : madeIns) {
            try {
                MadeInDocument doc = documentMapper.toDocument(madeIn);
                searchRepository.save(doc);
                indexed++;
            } catch (Exception e) {
                log.error("Failed to index made_in {}: {}", madeIn.getId(), e.getMessage());
            }
        }

        log.info("MadeIn reindex completed: {} entries indexed", indexed);
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
                    // Wildcard match on region
                    s -> s.wildcard(w -> w.field("region").value(wildcardQuery).boost(1.5f)));
            boolQuery.should(
                    // Fuzzy match for typos
                    s ->
                            s.multiMatch(
                                    mm ->
                                            mm.query(query)
                                                    .fields("name^3", "description", "region")
                                                    .fuzziness("AUTO")));
            boolQuery.should(
                    // Wildcard on description
                    s -> s.wildcard(w -> w.field("description").value(wildcardQuery)));

            boolQuery.minimumShouldMatch("1");
        }

        return Query.of(q -> q.bool(boolQuery.build()));
    }
}
