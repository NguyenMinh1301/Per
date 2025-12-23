package com.per.product.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.ProductIndexEvent;
import com.per.product.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for syncing product data to Elasticsearch. Processes ProductIndexEvent messages
 * published by ProductServiceImpl on CRUD operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexConsumer {

    private final ProductSearchRepository searchRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.PRODUCT_INDEX_TOPIC,
            groupId = KafkaTopicNames.PRODUCT_INDEX_GROUP)
    public void consume(ProductIndexEvent event) {
        log.info(
                "Processing product index event: action={}, productId={}",
                event.getAction(),
                event.getProductId());

        switch (event.getAction()) {
            case INDEX -> {
                searchRepository.save(event.getDocument());
                log.info("Product indexed: {}", event.getProductId());
            }
            case DELETE -> {
                searchRepository.deleteById(event.getProductId());
                log.info("Product deleted from index: {}", event.getProductId());
            }
        }
    }
}
