package com.per.made_in.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.MadeInIndexEvent;
import com.per.made_in.repository.MadeInSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Kafka consumer for syncing made-in data to Elasticsearch. */
@Component
@RequiredArgsConstructor
@Slf4j
public class MadeInIndexConsumer {

    private final MadeInSearchRepository searchRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(
            topics = KafkaTopicNames.MADEIN_INDEX_TOPIC,
            groupId = KafkaTopicNames.MADEIN_INDEX_GROUP)
    public void consume(MadeInIndexEvent event) {
        log.info(
                "Processing made-in index event: action={}, madeInId={}",
                event.getAction(),
                event.getMadeInId());

        switch (event.getAction()) {
            case INDEX -> {
                searchRepository.save(event.getDocument());
                log.info("MadeIn indexed: {}", event.getMadeInId());
            }
            case DELETE -> {
                searchRepository.deleteById(event.getMadeInId());
                log.info("MadeIn deleted from index: {}", event.getMadeInId());
            }
        }
    }
}
