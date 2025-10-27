package com.per.auth.service;

import com.per.auth.dto.request.ForgotPasswordRequest;
import com.per.auth.dto.request.LogoutRequest;
import com.per.auth.dto.request.RefreshTokenRequest;
import com.per.auth.dto.request.ResetPasswordRequest;
import com.per.auth.dto.request.SigninRequest;
import com.per.auth.dto.request.SignupRequest;
import com.per.auth.dto.request.VerifyEmailRequest;
import com.per.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(SignupRequest request);

    AuthResponse login(SigninRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request, String username);

    void verifyEmail(VerifyEmailRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void validateResetToken(String token);
}
