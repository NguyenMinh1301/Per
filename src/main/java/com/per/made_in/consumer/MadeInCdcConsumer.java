package com.per.made_in.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.cdc.MadeInCdcPayload;
import com.per.made_in.mapper.MadeInDocumentMapper;
import com.per.made_in.repository.MadeInRepository;
import com.per.made_in.repository.MadeInSearchRepository;
import com.per.product.service.ProductCdcIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CDC consumer for MadeIn (country of origin) entity changes from Debezium. Syncs MadeIn to
 * Elasticsearch and triggers cascade re-indexing of related Products.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MadeInCdcConsumer {

    private final MadeInRepository madeInRepository;
    private final MadeInSearchRepository madeInSearchRepository;
    private final MadeInDocumentMapper madeInDocumentMapper;
    private final ProductCdcIndexService productCdcIndexService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.CDC_MADE_INS_TOPIC,
            groupId = KafkaTopicNames.CDC_MADEIN_GROUP,
            containerFactory = "cdcKafkaListenerContainerFactory")
    public void consume(String message) {
        if (message == null || message.isEmpty()) {
            log.debug("Received empty or tombstone record, skipping");
            return;
        }

        try {
            MadeInCdcPayload payload = objectMapper.readValue(message, MadeInCdcPayload.class);
            UUID madeInId = UUID.fromString(payload.getId());

            log.info("Processing madeIn CDC event: op={}, madeInId={}", payload.getOp(), madeInId);

            if (payload.isDeleted()) {
                madeInSearchRepository.deleteById(madeInId.toString());
                log.info("MadeIn deleted from ES: {}", madeInId);
            } else {
                madeInRepository
                        .findById(madeInId)
                        .ifPresent(
                                madeIn -> {
                                    madeInSearchRepository.save(
                                            madeInDocumentMapper.toDocument(madeIn));
                                    log.info("MadeIn indexed: {}", madeInId);

                                    // CASCADE: Re-index all products that reference this madeIn
                                    productCdcIndexService.reindexProductsByMadeIn(madeInId);
                                });
            }
        } catch (Exception e) {
            log.error("Failed to process madeIn CDC event: {}", e.getMessage(), e);
            throw new RuntimeException("CDC processing failed", e);
        }
    }
}
