package com.per.mail.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.per.mail.config.MailProperties;
import com.per.mail.dto.SendEmailEvent;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AwsSesEmailService Unit Tests")
class AwsSesEmailServiceTest {

    @Mock private SesClient sesClient;

    @Mock private MailProperties mailProperties;

    @Mock private TemplateEngine templateEngine;

    @Captor private ArgumentCaptor<SendEmailRequest> requestCaptor;

    private AwsSesEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new AwsSesEmailService(sesClient, mailProperties, templateEngine);
    }

    @Test
    @DisplayName("Should send email via SES with correct request structure")
    void shouldSendEmailWithCorrectRequestStructure() {
        // Given
        when(mailProperties.getSourceEmail()).thenReturn("no-reply@per.io.vn");
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Welcome Test User!</body></html>");
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-123").build());

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("welcome")
                        .variables(Map.of("name", "Test User"))
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(sesClient).sendEmail(requestCaptor.capture());
        SendEmailRequest request = requestCaptor.getValue();

        assertThat(request.source()).isEqualTo("no-reply@per.io.vn");
        assertThat(request.destination().toAddresses()).containsExactly("user@example.com");
        assertThat(request.message().subject().data())
                .isEqualTo("Welcome to PER - The Essence of Elegance");
        assertThat(request.message().body().html().data()).contains("Welcome Test User!");
    }

    @Test
    @DisplayName("Should use reset-password subject for reset-password template")
    void shouldUseResetPasswordSubject() {
        // Given
        when(mailProperties.getSourceEmail()).thenReturn("no-reply@per.io.vn");
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Reset code: 123456</body></html>");
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-456").build());

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("reset-password")
                        .variables(Map.of("name", "User", "resetCode", "123456"))
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(sesClient).sendEmail(requestCaptor.capture());
        assertThat(requestCaptor.getValue().message().subject().data())
                .isEqualTo("Security Notification - Password Reset Request");
    }

    @Test
    @DisplayName("Should use default subject for unknown template")
    void shouldUseDefaultSubjectForUnknownTemplate() {
        // Given
        when(mailProperties.getSourceEmail()).thenReturn("no-reply@per.io.vn");
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Content</body></html>");
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-789").build());

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("unknown-template")
                        .variables(Map.of())
                        .build();

        // When
        emailService.sendTemplatedEmail(event);

        // Then
        verify(sesClient).sendEmail(requestCaptor.capture());
        assertThat(requestCaptor.getValue().message().subject().data())
                .isEqualTo("Notification from PER");
    }

    @Test
    @DisplayName("Should propagate SES exception")
    void shouldPropagateSesException() {
        // Given
        when(mailProperties.getSourceEmail()).thenReturn("no-reply@per.io.vn");
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Content</body></html>");
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES Error").build());

        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("user@example.com")
                        .templateCode("welcome")
                        .variables(Map.of("name", "User"))
                        .build();

        // When & Then
        assertThatThrownBy(() -> emailService.sendTemplatedEmail(event))
                .isInstanceOf(SesException.class)
                .hasMessageContaining("SES Error");
    }
}
