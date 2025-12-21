package com.per.auth.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.per.auth.service.MailService;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.EmailEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for email events with retry and Dead Letter Queue (DLQ) support.
 *
 * <p>Retry behavior:
 *
 * <ul>
 *   <li>3 retry attempts with exponential backoff: 1s → 2s → 4s
 *   <li>Failed messages after all retries go to email-topic-dlt
 *   <li>DLQ messages are handled by the {@link #handleDlt} method in this class
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final MailService mailService;

    @RetryableTopic(
            attempts = "4", // 1 initial + 3 retries
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            dltTopicSuffix = "-dlt",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true",
            include = {Exception.class})
    @KafkaListener(topics = KafkaTopicNames.EMAIL_TOPIC, groupId = KafkaTopicNames.EMAIL_GROUP)
    public void consume(EmailEvent event) {
        log.info("Processing email event: to={}, subject={}", event.getTo(), event.getSubject());
        mailService.sendEmail(event.getTo(), event.getSubject(), event.getContent());
        log.info("Email sent successfully: to={}", event.getTo());
    }

    /**
     * Dead Letter Topic handler for failed email events.
     *
     * <p>Called when all retry attempts are exhausted. Logs the failure for manual review.
     *
     * <p>Future enhancements could include:
     *
     * <ul>
     *   <li>Persisting failed messages to database for admin review
     *   <li>Sending alerts to monitoring systems
     *   <li>Scheduled retry from DLT
     * </ul>
     */
    @DltHandler
    public void handleDlt(EmailEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error(
                "Email delivery failed permanently after all retries - Topic: {}, To: {}, Subject: {}",
                topic,
                event.getTo(),
                event.getSubject());

        // TODO: Future enhancements
        // 1. Persist to database: failedEmailRepository.save(FailedEmail.from(event));
        // 2. Send alert: alertService.sendAlert("Email DLT", event.toString());
        // 3. Metrics: metricsService.incrementCounter("email.dlt.count");
    }
}
