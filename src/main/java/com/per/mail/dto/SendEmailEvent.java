package com.per.mail.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka event payload for sending templated emails. Published to topic: notification.email.send */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailEvent {

    /** Recipient email address. */
    private String to;

    /**
     * Template code matching a Thymeleaf template in templates/email/. Examples: "welcome",
     * "reset-password"
     */
    private String templateCode;

    /**
     * Template variables for Thymeleaf processing. Keys must match th:text="${key}" placeholders in
     * the template.
     */
    private Map<String, Object> variables;
}
