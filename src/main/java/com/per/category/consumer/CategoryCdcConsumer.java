package com.per.category.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.category.mapper.CategoryDocumentMapper;
import com.per.category.repository.CategoryRepository;
import com.per.category.repository.CategorySearchRepository;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.cdc.CategoryCdcPayload;
import com.per.product.service.ProductCdcIndexService;
import com.per.rag.service.QdrantCdcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CDC consumer for Category entity changes from Debezium. Syncs Category to Elasticsearch/Qdrant
 * and triggers cascade re-indexing of related Products.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryCdcConsumer {

    private final CategoryRepository categoryRepository;
    private final CategorySearchRepository categorySearchRepository;
    private final CategoryDocumentMapper categoryDocumentMapper;
    private final ProductCdcIndexService productCdcIndexService;
    private final QdrantCdcService qdrantCdcService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.CDC_CATEGORIES_TOPIC,
            groupId = KafkaTopicNames.CDC_CATEGORY_GROUP,
            containerFactory = "cdcKafkaListenerContainerFactory")
    public void consume(String message) {
        if (message == null || message.isEmpty()) {
            log.debug("Received empty or tombstone record, skipping");
            return;
        }

        try {
            CategoryCdcPayload payload = objectMapper.readValue(message, CategoryCdcPayload.class);
            UUID categoryId = UUID.fromString(payload.getId());

            log.info(
                    "Processing category CDC event: op={}, categoryId={}",
                    payload.getOp(),
                    categoryId);

            if (payload.isDeleted()) {
                categorySearchRepository.deleteById(categoryId.toString());
                qdrantCdcService.deleteCategory(categoryId);
                log.info("Category deleted from ES/Qdrant: {}", categoryId);
            } else {
                categoryRepository
                        .findById(categoryId)
                        .ifPresent(
                                category -> {
                                    categorySearchRepository.save(
                                            categoryDocumentMapper.toDocument(category));
                                    qdrantCdcService.indexCategory(categoryId);
                                    log.info("Category indexed: {}", categoryId);

                                    // CASCADE: Re-index all products that reference this category
                                    productCdcIndexService.reindexProductsByCategory(categoryId);
                                });
            }
        } catch (Exception e) {
            log.error("Failed to process category CDC event: {}", e.getMessage(), e);
            throw new RuntimeException("CDC processing failed", e);
        }
    }
}
