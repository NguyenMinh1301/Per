package com.per.auth.service;

public interface MailService {
    void sendEmail(String to, String subject, String content);
}
