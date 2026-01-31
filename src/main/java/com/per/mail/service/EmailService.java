package com.per.mail.service;

import com.per.mail.dto.SendEmailEvent;

/** Email service interface for sending templated emails via SMTP. */
public interface EmailService {

    /**
     * Send a templated email using SMTP.
     *
     * @param event The email event containing recipient, template code, and variables
     */
    void sendTemplatedEmail(SendEmailEvent event);
}
