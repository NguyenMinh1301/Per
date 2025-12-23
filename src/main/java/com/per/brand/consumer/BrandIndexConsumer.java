package com.per.brand.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.per.brand.repository.BrandSearchRepository;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.BrandIndexEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Kafka consumer for syncing brand data to Elasticsearch. */
@Component
@RequiredArgsConstructor
@Slf4j
public class BrandIndexConsumer {

    private final BrandSearchRepository searchRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.BRAND_INDEX_TOPIC,
            groupId = KafkaTopicNames.BRAND_INDEX_GROUP)
    public void consume(BrandIndexEvent event) {
        log.info(
                "Processing brand index event: action={}, brandId={}",
                event.getAction(),
                event.getBrandId());

        switch (event.getAction()) {
            case INDEX -> {
                searchRepository.save(event.getDocument());
                log.info("Brand indexed: {}", event.getBrandId());
            }
            case DELETE -> {
                searchRepository.deleteById(event.getBrandId());
                log.info("Brand deleted from index: {}", event.getBrandId());
            }
        }
    }
}
