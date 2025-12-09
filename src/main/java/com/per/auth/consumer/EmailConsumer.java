package com.per.auth.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.per.auth.service.MailService;
import com.per.common.event.EmailEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final MailService mailService;

    @KafkaListener(topics = "email-topic", groupId = "email-group")
    public void consume(EmailEvent event) {
        log.info("Received email event: {}", event);
        mailService.sendEmail(event.getTo(), event.getSubject(), event.getContent());
    }
}
