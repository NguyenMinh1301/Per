package com.per.mail.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.per.mail.config.MailProperties;
import com.per.mail.dto.SendEmailEvent;
import com.per.mail.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

/**
 * AWS SES implementation of the EmailService. Renders Thymeleaf templates and sends HTML emails via
 * SES API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AwsSesEmailService implements EmailService {

    private final SesClient sesClient;
    private final MailProperties mailProps;
    private final TemplateEngine templateEngine;

    private static final Map<String, String> TEMPLATE_SUBJECTS =
            Map.of(
                    "welcome", "Welcome to PER - The Essence of Elegance",
                    "reset-password", "Security Notification - Password Reset Request",
                    "verify-email", "PER Account Verification");

    @Override
    public void sendTemplatedEmail(SendEmailEvent event) {
        log.debug("Rendering template: {}", event.getTemplateCode());

        // Render Thymeleaf template
        Context ctx = new Context();
        ctx.setVariables(event.getVariables());
        String htmlBody = templateEngine.process("email/" + event.getTemplateCode(), ctx);

        // Build SES request
        String subject = getSubject(event.getTemplateCode());
        SendEmailRequest request =
                SendEmailRequest.builder()
                        .source(mailProps.getSourceEmail())
                        .destination(Destination.builder().toAddresses(event.getTo()).build())
                        .message(
                                Message.builder()
                                        .subject(
                                                Content.builder()
                                                        .data(subject)
                                                        .charset("UTF-8")
                                                        .build())
                                        .body(
                                                Body.builder()
                                                        .html(
                                                                Content.builder()
                                                                        .data(htmlBody)
                                                                        .charset("UTF-8")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        // Send via SES
        SendEmailResponse response = sesClient.sendEmail(request);
        log.info(
                "Email sent successfully: to={}, messageId={}",
                event.getTo(),
                response.messageId());
    }

    private String getSubject(String templateCode) {
        return TEMPLATE_SUBJECTS.getOrDefault(templateCode, "Notification from PER");
    }
}
