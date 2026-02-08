package com.per.product.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.cdc.ProductCdcPayload;
import com.per.product.service.ProductCdcIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CDC consumer for Product entity changes from Debezium. Listens to
 * per.public.products topic and
 * syncs to Elasticsearch/Qdrant.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCdcConsumer {

    private final ProductCdcIndexService productCdcIndexService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000), dltTopicSuffix = "-dlt", dltStrategy = DltStrategy.FAIL_ON_ERROR, autoCreateTopics = "true", include = {
            Exception.class })
    @KafkaListener(topics = KafkaTopicNames.CDC_PRODUCTS_TOPIC, groupId = KafkaTopicNames.CDC_PRODUCT_GROUP, containerFactory = "cdcKafkaListenerContainerFactory")
    public void consume(String message) {
        // Tombstones are filtered at container level, but double-check
        if (message == null || message.isEmpty()) {
            log.debug("Received empty or tombstone record, skipping");
            return;
        }

        try {
            // Handle double-serialized JSON if present
            String jsonContent = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                jsonContent = objectMapper.readValue(message, String.class);
            }

            ProductCdcPayload payload = objectMapper.readValue(jsonContent, ProductCdcPayload.class);
            UUID productId = UUID.fromString(payload.getId());

            log.info(
                    "Processing product CDC event: op={}, productId={}",
                    payload.getOp(),
                    productId);

            if (payload.isDeleted()) {
                productCdcIndexService.deleteProduct(productId);
                log.info("Product deleted from ES/Qdrant: {}", productId);
            } else {
                productCdcIndexService.indexProduct(productId);
                log.info("Product indexed to ES/Qdrant: {}", productId);
            }
        } catch (Exception e) {
            log.error("Failed to process product CDC event: {}", e.getMessage(), e);
            throw new RuntimeException("CDC processing failed", e);
        }
    }

    /**
     * Dead Letter Topic handler. Logs messages that failed after all retries.
     * This provides visibility into failed CDC events for monitoring and debugging.
     */
    @org.springframework.kafka.annotation.DltHandler
    public void handleDlt(
            String message,
            @org.springframework.messaging.handler.annotation.Header(org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC) String topic,
            @org.springframework.messaging.handler.annotation.Header(org.springframework.kafka.support.KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error(
                "CDC message sent to DLT. Topic: {}, Payload: {}, Error: {}",
                topic,
                message,
                exceptionMessage);
        // TODO: Consider sending to alerting system (Slack, PagerDuty, etc.)
    }
}
