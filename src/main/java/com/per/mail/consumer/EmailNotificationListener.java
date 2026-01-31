package com.per.mail.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.per.common.config.kafka.KafkaTopicNames;
import com.per.mail.dto.SendEmailEvent;
import com.per.mail.service.EmailService;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for email notification events. Listens to notification.email.send topic and
 * dispatches emails via SMTP.
 *
 * <p>Resilience:
 *
 * <ul>
 *   <li>Resilience4j @Retry for transient SMTP network failures
 *   <li>Failed messages after retries are routed to notification.email.dlq
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;

    @Retry(name = "emailSend", fallbackMethod = "handleEmailFailure")
    @KafkaListener(topics = KafkaTopicNames.EMAIL_SEND_TOPIC, groupId = KafkaTopicNames.EMAIL_GROUP)
    public void onEmailEvent(SendEmailEvent event) {
        log.info(
                "Processing email event: to={}, template={}",
                event.getTo(),
                event.getTemplateCode());

        emailService.sendTemplatedEmail(event);

        log.info("Email sent successfully: to={}", event.getTo());
    }

    /**
     * Fallback method called when all retry attempts are exhausted. Logs the failure for
     * monitoring/alerting.
     */
    @SuppressWarnings("unused")
    private void handleEmailFailure(SendEmailEvent event, Exception ex) {
        log.error(
                "Email delivery failed after all retries: to={}, template={}, error={}",
                event.getTo(),
                event.getTemplateCode(),
                ex.getMessage());

        // Future: persist to failed_emails table or send to external alerting
    }

    /**
     * DLQ handler for permanently failed messages. Called when Kafka error handling routes to DLQ.
     */
    @KafkaListener(
            topics = KafkaTopicNames.EMAIL_DLQ_TOPIC,
            groupId = KafkaTopicNames.EMAIL_GROUP + "-dlq")
    public void handleDlq(SendEmailEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error(
                "Email in DLQ - Topic: {}, To: {}, Template: {}",
                topic,
                event.getTo(),
                event.getTemplateCode());

        // Future enhancements:
        // 1. Persist to database for admin dashboard
        // 2. Send alert to Slack/PagerDuty
        // 3. Metrics: Prometheus counter for DLQ entries
    }
}
