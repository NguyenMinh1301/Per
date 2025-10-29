package com.per.auth.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.per.auth.configuration.ApplicationProperties;
import com.per.auth.service.MailService;
import com.per.common.ApiConstants;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final ApplicationProperties applicationProperties;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String displayName = resolveDisplayName(user);
        SimpleMailMessage message = buildBaseMessage(user);
        message.setSubject("Verify your email address");
        message.setText(
                "Hello "
                        + displayName
                        + ",\n\n"
                        + "Please verify your email address by visiting the following link within 24 hours:\n"
                        + buildVerificationLink(token)
                        + "\n\n"
                        + "Thank you.");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String displayName = resolveDisplayName(user);
        SimpleMailMessage message = buildBaseMessage(user);
        message.setSubject("Password reset request");
        message.setText(
                "Hello "
                        + displayName
                        + ",\n\n"
                        + "A password reset was requested for your account. Please use the link below within 15 minutes:\n"
                        + buildResetLink(token)
                        + "\n\n"
                        + "If you did not initiate this request, you can safely ignore this email.");
        mailSender.send(message);
    }

    private SimpleMailMessage buildBaseMessage(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        String from = applicationProperties.getMail().getFrom();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        return message;
    }

    private String buildVerificationLink(String token) {
        return applicationProperties.getBaseUrl()
                + ApiConstants.Auth.VERIFY_EMAIL
                + "?token="
                + token;
    }

    private String buildResetLink(String token) {
        return applicationProperties.getBaseUrl()
                + ApiConstants.Auth.RESET_PASSWORD
                + "?token="
                + token;
    }

    private String resolveDisplayName(User user) {
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        return user.getUsername();
    }
}
