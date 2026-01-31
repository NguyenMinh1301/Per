package com.per.mail.service.impl;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.per.mail.config.MailProperties;
import com.per.mail.dto.SendEmailEvent;
import com.per.mail.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMTP implementation of the EmailService. Renders Thymeleaf templates and sends HTML emails via
 * JavaMailSender (Google SMTP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
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

        // Build and send MIME message
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProps.getFrom());
            helper.setTo(event.getTo());
            helper.setSubject(getSubject(event.getTemplateCode()));
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("Email sent successfully: to={}", event.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email: to={}", event.getTo(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String getSubject(String templateCode) {
        return TEMPLATE_SUBJECTS.getOrDefault(templateCode, "Notification from PER");
    }
}
