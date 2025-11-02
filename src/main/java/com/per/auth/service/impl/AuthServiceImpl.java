package com.per.auth.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.auth.configuration.JwtProperties;
import com.per.auth.dto.request.ForgotPasswordRequest;
import com.per.auth.dto.request.LogoutRequest;
import com.per.auth.dto.request.RefreshTokenRequest;
import com.per.auth.dto.request.ResetPasswordRequest;
import com.per.auth.dto.request.SigninRequest;
import com.per.auth.dto.request.SignupRequest;
import com.per.auth.dto.request.VerifyEmailRequest;
import com.per.auth.dto.response.AuthResponse;
import com.per.auth.dto.response.AuthTokenResponse;
import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.auth.entity.TokenType;
import com.per.auth.entity.UserToken;
import com.per.auth.mapper.UserMapper;
import com.per.auth.repository.RoleRepository;
import com.per.auth.repository.UserRepository;
import com.per.auth.security.jwt.JwtService;
import com.per.auth.security.jwt.JwtTokenType;
import com.per.auth.security.principal.UserPrincipal;
import com.per.auth.service.AuthService;
import com.per.auth.service.MailService;
import com.per.auth.service.token.RefreshTokenService;
import com.per.auth.service.token.db.UserTokenService;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Duration EMAIL_VERIFY_TTL = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final UserTokenService userTokenService;
    private final MailService mailService;
    private final Clock clock;

    @Override
    public AuthResponse register(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role userRole =
                roleRepository
                        .findByName(RoleType.USER)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Role USER has not been initialised"));

        User user =
                User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .emailVerified(false)
                        .active(true)
                        .build();
        user.addRole(userRole);
        user = userRepository.save(user);

        UserToken verificationToken =
                userTokenService.create(user, TokenType.EMAIL_VERIFICATION, EMAIL_VERIFY_TTL);
        mailService.sendVerificationEmail(user, verificationToken.getToken());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(SigninRequest request) {
        User user = resolveUser(request.getUsername());
        if (!user.isActive()) {
            throw new IllegalStateException("Account has been locked");
        }

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        user.setLastLoginAt(Instant.now(clock));
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String username = jwtService.extractUsername(token);
        User user = resolveUser(username);
        if (!user.isActive()) {
            throw new IllegalStateException("Account has been locked");
        }

        if (!jwtService.isTokenValid(token, UserPrincipal.from(user), JwtTokenType.REFRESH)) {
            throw new IllegalArgumentException("Refresh token is invalid");
        }

        if (!refreshTokenService.isValid(token, user.getUsername())) {
            throw new IllegalArgumentException("Refresh token has expired or was revoked");
        }

        refreshTokenService.revoke(token);

        return buildAuthResponse(user);
    }

    @Override
    public void logout(LogoutRequest request, String username) {
        if (username == null) {
            return;
        }
        String token = request.getRefreshToken();
        if (refreshTokenService.isValid(token, username)) {
            refreshTokenService.revoke(token);
        }
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        UserToken token =
                userTokenService.consume(request.getToken(), TokenType.EMAIL_VERIFICATION);
        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository
                .findByEmail(request.getEmail())
                .ifPresent(
                        user -> {
                            UserToken token =
                                    userTokenService.create(
                                            user, TokenType.PASSWORD_RESET, PASSWORD_RESET_TTL);
                            mailService.sendPasswordResetEmail(user, token.getToken());
                        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        UserToken token = userTokenService.consume(request.getToken(), TokenType.PASSWORD_RESET);
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateResetToken(String token) {
        userTokenService.validate(token, TokenType.PASSWORD_RESET);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.store(refreshToken, user.getUsername());

        long expiresIn =
                Optional.ofNullable(jwtProperties.getAccessTtl())
                        .map(Duration::getSeconds)
                        .orElse(Duration.ofMinutes(15).getSeconds());

        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .tokens(AuthTokenResponse.bearer(accessToken, refreshToken, expiresIn))
                .build();
    }

    private User resolveUser(String username) {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            return byUsername.get();
        }
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }
}
