package com.per.common.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.per.brand.repository.BrandSearchRepository;
import com.per.brand.service.BrandSearchService;
import com.per.category.repository.CategorySearchRepository;
import com.per.category.service.CategorySearchService;
import com.per.made_in.repository.MadeInSearchRepository;
import com.per.made_in.service.MadeInSearchService;
import com.per.product.repository.ProductSearchRepository;
import com.per.product.service.ProductSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes Elasticsearch indexes on application startup. Only reindexes when indexes are empty
 * (first run or after ES data loss). Controlled by app.elasticsearch.auto-reindex property.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after AdminInitializer
public class ElasticsearchInitializer implements CommandLineRunner {

    private final ProductSearchRepository productSearchRepository;
    private final BrandSearchRepository brandSearchRepository;
    private final CategorySearchRepository categorySearchRepository;
    private final MadeInSearchRepository madeInSearchRepository;

    private final ProductSearchService productSearchService;
    private final BrandSearchService brandSearchService;
    private final CategorySearchService categorySearchService;
    private final MadeInSearchService madeInSearchService;

    @Value("${app.elasticsearch.auto-reindex:false}")
    private boolean autoReindex;

    @Override
    public void run(String... args) {
        if (!autoReindex) {
            log.info("Elasticsearch auto-reindex is disabled");
            return;
        }

        log.info("Checking Elasticsearch indexes for initial data sync...");

        reindexIfEmpty(
                "products", productSearchRepository.count(), productSearchService::reindexAll);
        reindexIfEmpty("brands", brandSearchRepository.count(), brandSearchService::reindexAll);
        reindexIfEmpty(
                "categories", categorySearchRepository.count(), categorySearchService::reindexAll);
        reindexIfEmpty("made_in", madeInSearchRepository.count(), madeInSearchService::reindexAll);

        log.info("Elasticsearch initialization completed");
    }

    private void reindexIfEmpty(String indexName, long count, Runnable reindexAction) {
        if (count == 0) {
            log.info("Index '{}' is empty, triggering reindex...", indexName);
            try {
                reindexAction.run();
                log.info("Index '{}' reindex completed", indexName);
            } catch (Exception e) {
                log.error("Failed to reindex '{}': {}", indexName, e.getMessage());
            }
        } else {
            log.info("Index '{}' already has {} documents, skipping reindex", indexName, count);
        }
    }
}
