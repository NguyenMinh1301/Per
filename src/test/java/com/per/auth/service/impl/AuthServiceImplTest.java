package com.per.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.per.auth.configuration.JwtProperties;
import com.per.auth.dto.request.ForgotPasswordRequest;
import com.per.auth.dto.request.LogoutRequest;
import com.per.auth.dto.request.RefreshTokenRequest;
import com.per.auth.dto.request.ResetPasswordRequest;
import com.per.auth.dto.request.SigninRequest;
import com.per.auth.dto.request.SignupRequest;
import com.per.auth.dto.request.VerifyEmailRequest;
import com.per.auth.dto.response.AuthResponse;
import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.auth.entity.TokenType;
import com.per.auth.entity.UserToken;
import com.per.auth.repository.RoleRepository;
import com.per.auth.repository.UserRepository;
import com.per.auth.security.jwt.JwtService;
import com.per.auth.security.jwt.JwtTokenType;
import com.per.auth.security.principal.UserPrincipal;
import com.per.auth.service.MailService;
import com.per.auth.service.token.RefreshTokenService;
import com.per.auth.service.token.db.UserTokenService;
import com.per.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private JwtService jwtService;

    @Mock private JwtProperties jwtProperties;

    @Mock private RefreshTokenService refreshTokenService;

    @Mock private UserTokenService userTokenService;

    @Mock private MailService mailService;

    @Mock private Clock clock;

    @InjectMocks private AuthServiceImpl authService;

    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final Instant FIXED_TIME = Instant.parse("2024-01-01T00:00:00Z");

    private User testUser;
    private Role userRole;
    private Clock fixedClock;

    @BeforeAll
    static void setUpTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_TIME, ZoneId.systemDefault());
        lenient().when(clock.instant()).thenReturn(FIXED_TIME);

        userRole = new Role(RoleType.USER, "User role");
        testUser =
                User.builder()
                        .id(UUID.randomUUID())
                        .username(USERNAME)
                        .email(EMAIL)
                        .password(ENCODED_PASSWORD)
                        .firstName("Test")
                        .lastName("User")
                        .emailVerified(false)
                        .active(true)
                        .build();
        testUser.addRole(userRole);

        lenient().when(jwtProperties.getAccessTtl()).thenReturn(Duration.ofMinutes(15));

        // Reset SecurityContext before each test
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Given
            SignupRequest request =
                    SignupRequest.builder()
                            .username(USERNAME)
                            .email(EMAIL)
                            .password(PASSWORD)
                            .firstName("Test")
                            .lastName("User")
                            .build();

            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);

            UserToken verificationToken =
                    UserToken.builder()
                            .id(UUID.randomUUID())
                            .token("verification_token")
                            .type(TokenType.EMAIL_VERIFICATION)
                            .user(testUser)
                            .expiresAt(FIXED_TIME.plus(Duration.ofHours(24)))
                            .revoked(false)
                            .build();

            when(userTokenService.create(
                            any(User.class), eq(TokenType.EMAIL_VERIFICATION), any(Duration.class)))
                    .thenReturn(verificationToken);
            // Note: create() internally calls revokeTokens(), but we don't need to mock it
            // since it's a void method and Mockito handles void methods automatically

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getTokens()).isNotNull();
            assertThat(response.getTokens().getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getTokens().getRefreshToken()).isEqualTo(REFRESH_TOKEN);

            verify(userRepository).existsByUsername(USERNAME);
            verify(userRepository).existsByEmail(EMAIL);
            verify(roleRepository).findByName(RoleType.USER);
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(mailService).sendVerificationEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            SignupRequest request =
                    SignupRequest.builder()
                            .username(USERNAME)
                            .email(EMAIL)
                            .password(PASSWORD)
                            .build();

            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            SignupRequest request =
                    SignupRequest.builder()
                            .username(USERNAME)
                            .email(EMAIL)
                            .password(PASSWORD)
                            .build();

            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when USER role not found")
        void shouldThrowExceptionWhenUserRoleNotFound() {
            // Given
            SignupRequest request =
                    SignupRequest.builder()
                            .username(USERNAME)
                            .email(EMAIL)
                            .password(PASSWORD)
                            .build();

            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                    .hasMessageContaining("Role USER has not been initialised");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() {
            // Given
            SigninRequest request =
                    SigninRequest.builder().identifier(USERNAME).password(PASSWORD).build();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mock(Authentication.class));
            when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTokens()).isNotNull();
            assertThat(response.getTokens().getAccessToken()).isEqualTo(ACCESS_TOKEN);

            verify(userRepository).findByUsername(USERNAME);
            verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user account is locked")
        void shouldThrowExceptionWhenAccountLocked() {
            // Given
            User lockedUser =
                    User.builder()
                            .id(testUser.getId())
                            .username(USERNAME)
                            .email(EMAIL)
                            .password(ENCODED_PASSWORD)
                            .firstName("Test")
                            .lastName("User")
                            .emailVerified(false)
                            .active(false) // Locked account
                            .build();
            lockedUser.addRole(userRole);

            SigninRequest request =
                    SigninRequest.builder().identifier(USERNAME).password(PASSWORD).build();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(lockedUser));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Account has been locked");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            SigninRequest request =
                    SigninRequest.builder().identifier(USERNAME).password(PASSWORD).build();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should login with email identifier")
        void shouldLoginWithEmailIdentifier() {
            // Given
            SigninRequest request =
                    SigninRequest.builder().identifier(EMAIL).password(PASSWORD).build();

            when(userRepository.findByUsername(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mock(Authentication.class));
            when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).findByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            RefreshTokenRequest request =
                    RefreshTokenRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USERNAME);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(jwtService.isTokenValid(
                            eq(REFRESH_TOKEN), any(UserPrincipal.class), eq(JwtTokenType.REFRESH)))
                    .thenReturn(true);
            when(refreshTokenService.isValid(REFRESH_TOKEN, USERNAME)).thenReturn(true);
            when(jwtService.generateAccessToken(any(User.class))).thenReturn("new_access_token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("new_refresh_token");

            // When
            AuthResponse response = authService.refreshToken(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTokens()).isNotNull();

            verify(refreshTokenService).revoke(REFRESH_TOKEN);
            verify(refreshTokenService).store(anyString(), eq(USERNAME));
            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw exception when refresh token is invalid")
        void shouldThrowExceptionWhenRefreshTokenInvalid() {
            // Given
            RefreshTokenRequest request =
                    RefreshTokenRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USERNAME);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(jwtService.isTokenValid(
                            eq(REFRESH_TOKEN), any(UserPrincipal.class), eq(JwtTokenType.REFRESH)))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Refresh token is invalid");
        }

        @Test
        @DisplayName("Should throw exception when refresh token expired")
        void shouldThrowExceptionWhenRefreshTokenExpired() {
            // Given
            RefreshTokenRequest request =
                    RefreshTokenRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USERNAME);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(jwtService.isTokenValid(
                            eq(REFRESH_TOKEN), any(UserPrincipal.class), eq(JwtTokenType.REFRESH)))
                    .thenReturn(true);
            when(refreshTokenService.isValid(REFRESH_TOKEN, USERNAME)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Refresh token has expired or was revoked");
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            // Given
            LogoutRequest request = LogoutRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(refreshTokenService.isValid(REFRESH_TOKEN, USERNAME)).thenReturn(true);

            // When
            authService.logout(request, USERNAME);

            // Then
            verify(refreshTokenService).revoke(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("Should handle logout when username is null")
        void shouldHandleLogoutWhenUsernameIsNull() {
            // Given
            LogoutRequest request = LogoutRequest.builder().refreshToken(REFRESH_TOKEN).build();

            // When
            authService.logout(request, null);

            // Then
            verify(refreshTokenService, never()).revoke(anyString());
        }

        @Test
        @DisplayName("Should handle logout when token is invalid")
        void shouldHandleLogoutWhenTokenIsInvalid() {
            // Given
            LogoutRequest request = LogoutRequest.builder().refreshToken(REFRESH_TOKEN).build();

            when(refreshTokenService.isValid(REFRESH_TOKEN, USERNAME)).thenReturn(false);

            // When
            authService.logout(request, USERNAME);

            // Then
            verify(refreshTokenService, never()).revoke(anyString());
        }
    }

    @Nested
    @DisplayName("Verify Email Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email successfully")
        void shouldVerifyEmailSuccessfully() {
            // Given
            String token = "verification_token";
            VerifyEmailRequest request = VerifyEmailRequest.builder().token(token).build();

            UserToken userToken =
                    UserToken.builder()
                            .id(UUID.randomUUID())
                            .token(token)
                            .type(TokenType.EMAIL_VERIFICATION)
                            .user(testUser)
                            .expiresAt(FIXED_TIME.plus(Duration.ofHours(24)))
                            .revoked(false)
                            .build();

            when(userTokenService.consume(token, TokenType.EMAIL_VERIFICATION))
                    .thenReturn(userToken);
            when(userRepository.save(any(User.class)))
                    .thenAnswer(
                            invocation -> {
                                User savedUser = invocation.getArgument(0);
                                savedUser.setEmailVerified(true);
                                return savedUser;
                            });

            // When
            authService.verifyEmail(request);

            // Then
            verify(userRepository).save(any(User.class));
            verify(userTokenService).consume(token, TokenType.EMAIL_VERIFICATION);
        }
    }

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should send password reset email when user exists")
        void shouldSendPasswordResetEmailWhenUserExists() {
            // Given
            ForgotPasswordRequest request = ForgotPasswordRequest.builder().email(EMAIL).build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));

            UserToken resetToken =
                    UserToken.builder()
                            .id(UUID.randomUUID())
                            .token("reset_token")
                            .type(TokenType.PASSWORD_RESET)
                            .user(testUser)
                            .expiresAt(FIXED_TIME.plus(Duration.ofMinutes(15)))
                            .revoked(false)
                            .build();

            when(userTokenService.create(
                            any(User.class), eq(TokenType.PASSWORD_RESET), any(Duration.class)))
                    .thenReturn(resetToken);

            // When
            authService.forgotPassword(request);

            // Then
            verify(userRepository).findByEmail(EMAIL);
            verify(userTokenService)
                    .create(any(User.class), eq(TokenType.PASSWORD_RESET), any(Duration.class));
            verify(mailService).sendPasswordResetEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Should handle forgot password when user does not exist")
        void shouldHandleForgotPasswordWhenUserNotExists() {
            // Given
            ForgotPasswordRequest request = ForgotPasswordRequest.builder().email(EMAIL).build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // When
            authService.forgotPassword(request);

            // Then
            verify(userRepository).findByEmail(EMAIL);
            verify(userTokenService, never()).create(any(), any(), any());
            verify(mailService, never()).sendPasswordResetEmail(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            // Given
            String token = "reset_token";
            String newPassword = "new_password123";
            ResetPasswordRequest request =
                    ResetPasswordRequest.builder().token(token).newPassword(newPassword).build();

            UserToken resetToken =
                    UserToken.builder()
                            .id(UUID.randomUUID())
                            .token(token)
                            .type(TokenType.PASSWORD_RESET)
                            .user(testUser)
                            .expiresAt(FIXED_TIME.plus(Duration.ofMinutes(15)))
                            .revoked(false)
                            .build();

            when(userTokenService.consume(token, TokenType.PASSWORD_RESET)).thenReturn(resetToken);
            when(passwordEncoder.encode(newPassword)).thenReturn("encoded_new_password");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.resetPassword(request);

            // Then
            verify(userTokenService).consume(token, TokenType.PASSWORD_RESET);
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Validate Reset Token Tests")
    class ValidateResetTokenTests {

        @Test
        @DisplayName("Should validate reset token successfully")
        void shouldValidateResetTokenSuccessfully() {
            // Given
            String token = "reset_token";
            UserToken validToken =
                    UserToken.builder()
                            .id(UUID.randomUUID())
                            .token(token)
                            .type(TokenType.PASSWORD_RESET)
                            .user(testUser)
                            .expiresAt(FIXED_TIME.plus(Duration.ofMinutes(15)))
                            .revoked(false)
                            .build();

            when(userTokenService.validate(token, TokenType.PASSWORD_RESET)).thenReturn(validToken);

            // When
            authService.validateResetToken(token);

            // Then
            verify(userTokenService).validate(token, TokenType.PASSWORD_RESET);
        }
    }
}
