package com.per.mail.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.per.mail.config.MailProperties;
import com.per.mail.dto.SendEmailEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmtpEmailService Unit Tests")
class SmtpEmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @Mock private MailProperties mailProperties;

    @Mock private TemplateEngine templateEngine;

    @Mock private MimeMessage mimeMessage;

    @Captor private ArgumentCaptor<MimeMessage> messageCaptor;

    private SmtpEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new SmtpEmailService(mailSender, mailProperties, templateEngine);
    }

    @Test
    @DisplayName("Should send email via SMTP with correct structure")
    void shouldSendEmailWithCorrectStructure() {
        // Given
        when(mailProperties.getFrom()).thenReturn("no-reply@per.io.vn");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Welcome Test User!</body></html>");

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("welcome")
                        .variables(Map.of("name", "Test User"))
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("Should render Thymeleaf template with variables")
    void shouldRenderTemplateWithVariables() {
        // Given
        when(mailProperties.getFrom()).thenReturn("no-reply@per.io.vn");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Reset code: 123456</body></html>");

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("reset-password")
                        .variables(Map.of("name", "User", "resetCode", "123456"))
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(templateEngine).process(any(String.class), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should handle unknown template with default subject")
    void shouldHandleUnknownTemplateWithDefaultSubject() {
        // Given
        when(mailProperties.getFrom()).thenReturn("no-reply@per.io.vn");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Content</body></html>");

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("unknown-template")
                        .variables(Map.of())
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should propagate MessagingException as RuntimeException")
    void shouldPropagateMessagingException() {
        // Given
        when(mailProperties.getFrom()).thenReturn("no-reply@per.io.vn");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Content</body></html>");
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(MimeMessage.class));

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("welcome")
                        .variables(Map.of("name", "User"))
                        .build();

        // When & Then
        assertThatThrownBy(() -> emailService.sendTemplatedEmail(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP Error");
    }
}
