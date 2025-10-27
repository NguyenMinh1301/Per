package com.per.auth.service;

import com.per.user.entity.User;

public interface MailService {
    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);
}
