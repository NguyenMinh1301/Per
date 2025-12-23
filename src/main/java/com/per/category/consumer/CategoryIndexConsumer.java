package com.per.category.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.per.category.repository.CategorySearchRepository;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.CategoryIndexEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Kafka consumer for syncing category data to Elasticsearch. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryIndexConsumer {

    private final CategorySearchRepository searchRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.CATEGORY_INDEX_TOPIC,
            groupId = KafkaTopicNames.CATEGORY_INDEX_GROUP)
    public void consume(CategoryIndexEvent event) {
        log.info(
                "Processing category index event: action={}, categoryId={}",
                event.getAction(),
                event.getCategoryId());

        switch (event.getAction()) {
            case INDEX -> {
                searchRepository.save(event.getDocument());
                log.info("Category indexed: {}", event.getCategoryId());
            }
            case DELETE -> {
                searchRepository.deleteById(event.getCategoryId());
                log.info("Category deleted from index: {}", event.getCategoryId());
            }
        }
    }
}
