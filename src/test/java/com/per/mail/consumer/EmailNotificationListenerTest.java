package com.per.mail.consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.per.mail.dto.SendEmailEvent;
import com.per.mail.service.EmailService;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationListener Unit Tests")
class EmailNotificationListenerTest {

    @Mock private EmailService emailService;

    @InjectMocks private EmailNotificationListener listener;

    @Test
    @DisplayName("Should process email event successfully")
    void shouldProcessEmailEventSuccessfully() {
        // Given
        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("test@example.com")
                        .templateCode("welcome")
                        .variables(
                                Map.of(
                                        "name",
                                        "Test User",
                                        "verificationLink",
                                        "https://example.com"))
                        .build();

        // When
        listener.onEmailEvent(event);

        // Then
        verify(emailService).sendTemplatedEmail(event);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    @DisplayName("Should pass event to email service with correct parameters")
    void shouldPassEventToEmailServiceWithCorrectParameters() {
        // Given
        Map<String, Object> variables =
                Map.of("name", "John", "resetCode", "123456", "expiryMinutes", 15);
        SendEmailEvent event =
                SendEmailEvent.builder()
                        .to("john@example.com")
                        .templateCode("reset-password")
                        .variables(variables)
                        .build();

        // When
        listener.onEmailEvent(event);

        // Then
        verify(emailService).sendTemplatedEmail(event);
    }
}
