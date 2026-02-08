package com.per.brand.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.brand.mapper.BrandDocumentMapper;
import com.per.brand.repository.BrandRepository;
import com.per.brand.repository.BrandSearchRepository;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.cdc.BrandCdcPayload;
import com.per.product.service.ProductCdcIndexService;
import com.per.rag.service.QdrantCdcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CDC consumer for Brand entity changes from Debezium. Syncs Brand to Elasticsearch/Qdrant and
 * triggers cascade re-indexing of related Products.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BrandCdcConsumer {

    private final BrandRepository brandRepository;
    private final BrandSearchRepository brandSearchRepository;
    private final BrandDocumentMapper brandDocumentMapper;
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
            topics = KafkaTopicNames.CDC_BRANDS_TOPIC,
            groupId = KafkaTopicNames.CDC_BRAND_GROUP,
            containerFactory = "cdcKafkaListenerContainerFactory")
    public void consume(String message) {
        if (message == null || message.isEmpty()) {
            log.debug("Received empty or tombstone record, skipping");
            return;
        }

        try {
            BrandCdcPayload payload = objectMapper.readValue(message, BrandCdcPayload.class);
            UUID brandId = UUID.fromString(payload.getId());

            log.info("Processing brand CDC event: op={}, brandId={}", payload.getOp(), brandId);

            if (payload.isDeleted()) {
                // Delete brand from Elasticsearch
                brandSearchRepository.deleteById(brandId.toString());
                qdrantCdcService.deleteBrand(brandId);
                log.info("Brand deleted from ES/Qdrant: {}", brandId);
                // Note: Products with this brand should be handled by FK constraint
            } else {
                // Index brand itself
                brandRepository
                        .findById(brandId)
                        .ifPresent(
                                brand -> {
                                    brandSearchRepository.save(
                                            brandDocumentMapper.toDocument(brand));
                                    qdrantCdcService.indexBrand(brandId);
                                    log.info("Brand indexed: {}", brandId);

                                    // CASCADE: Re-index all products that reference this brand
                                    productCdcIndexService.reindexProductsByBrand(brandId);
                                });
            }
        } catch (Exception e) {
            log.error("Failed to process brand CDC event: {}", e.getMessage(), e);
            throw new RuntimeException("CDC processing failed", e);
        }
    }
}
