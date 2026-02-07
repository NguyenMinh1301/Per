package com.per.product.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.cdc.ProductVariantCdcPayload;
import com.per.product.service.ProductCdcIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CDC consumer for ProductVariant entity changes from Debezium. When a variant changes (price,
 * stock, etc.), triggers re-indexing of the parent Product.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductVariantCdcConsumer {

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
            topics = KafkaTopicNames.CDC_PRODUCT_VARIANTS_TOPIC,
            groupId = KafkaTopicNames.CDC_PRODUCT_VARIANT_GROUP,
            containerFactory = "cdcKafkaListenerContainerFactory")
    public void consume(String message) {
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

            ProductVariantCdcPayload payload =
                    objectMapper.readValue(jsonContent, ProductVariantCdcPayload.class);
            String productIdStr = payload.getProductId();

            if (productIdStr == null || productIdStr.isEmpty()) {
                log.warn(
                        "ProductVariant CDC event missing product_id, skipping: variantId={}",
                        payload.getId());
                return;
            }

            UUID productId = UUID.fromString(productIdStr);
            log.info(
                    "Processing variant CDC event: op={}, variantId={}, productId={}",
                    payload.getOp(),
                    payload.getId(),
                    productId);

            if (payload.isDeleted()) {
                // Variant deleted - re-index parent product to update variant list
                productCdcIndexService.indexProduct(productId);
                log.info("Parent product re-indexed after variant deletion: {}", productId);
            } else {
                // Variant created/updated - re-index parent product
                productCdcIndexService.indexProduct(productId);
                log.info("Parent product re-indexed after variant change: {}", productId);
            }
        } catch (Exception e) {
            log.error("Failed to process variant CDC event: {}", e.getMessage(), e);
            throw new RuntimeException("CDC processing failed", e);
        }
    }
}
