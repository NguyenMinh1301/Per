package com.per.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.auth.dto.request.ForgotPasswordRequest;
import com.per.auth.dto.request.IntrospectRequest;
import com.per.auth.dto.request.LogoutRequest;
import com.per.auth.dto.request.RefreshTokenRequest;
import com.per.auth.dto.request.ResetPasswordRequest;
import com.per.auth.dto.request.SigninRequest;
import com.per.auth.dto.request.SignupRequest;
import com.per.auth.dto.request.VerifyEmailRequest;
import com.per.auth.dto.response.AuthTokenResponse;
import com.per.auth.dto.response.IntrospectResponse;
import com.per.auth.dto.response.ResetTokenValidationResponse;
import com.per.auth.dto.response.user.MeResponse;
import com.per.auth.service.AuthService;
import com.per.auth.service.MeService;
import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Auth.ROOT)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MeService meService;

    @PostMapping(ApiConstants.Auth.REGISTER)
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody SignupRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.AUTH_REGISTER_SUCCESS));
    }

    @PostMapping(ApiConstants.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
            @Valid @RequestBody SigninRequest request) {
        AuthTokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_LOGIN_SUCCESS, response));
    }

    @PostMapping(ApiConstants.Auth.REFRESH)
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.AUTH_REFRESH_SUCCESS, response));
    }

    @PostMapping(ApiConstants.Auth.INTROSPECT)
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @Valid @RequestBody IntrospectRequest request) {
        IntrospectResponse response = authService.introspect(request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.AUTH_INTROSPECT_SUCCESS, response));
    }

    @PostMapping(ApiConstants.Auth.LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        authService.logout(request, username);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_LOGOUT_SUCCESS));
    }

    @PostMapping(ApiConstants.Auth.VERIFY_EMAIL)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_VERIFY_SUCCESS));
    }

    @GetMapping(ApiConstants.Auth.VERIFY_EMAIL)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        VerifyEmailRequest request = VerifyEmailRequest.builder().token(token).build();
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_VERIFY_SUCCESS));
    }

    @PostMapping(ApiConstants.Auth.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_FORGOT_SUCCESS));
    }

    @PostMapping(ApiConstants.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_RESET_SUCCESS));
    }

    @GetMapping(ApiConstants.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<ResetTokenValidationResponse>> validateResetToken(
            @RequestParam("token") String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiSuccessCode.AUTH_RESET_TOKEN_VALID,
                        ResetTokenValidationResponse.builder().token(token).build()));
    }

    @GetMapping(ApiConstants.Auth.ME)
    public ResponseEntity<ApiResponse<MeResponse>> getCurrentUser() {
        MeResponse response = meService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.AUTH_ME_SUCCESS, response));
    }
}
