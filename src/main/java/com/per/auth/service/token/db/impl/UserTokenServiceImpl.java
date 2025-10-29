package com.per.auth.service.token.db.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.auth.entity.TokenType;
import com.per.auth.entity.UserToken;
import com.per.auth.repository.UserTokenRepository;
import com.per.auth.service.token.db.UserTokenService;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {

    private final UserTokenRepository userTokenRepository;
    private final Clock clock;

    @Override
    @Transactional
    public UserToken create(User user, TokenType type, Duration ttl) {
        revokeTokens(user, type);
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(15);
        }
        Instant now = Instant.now(clock);
        UserToken token =
                UserToken.builder()
                        .user(user)
                        .type(type)
                        .token(UUID.randomUUID().toString())
                        .expiresAt(now.plus(ttl))
                        .revoked(false)
                        .build();
        return userTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserToken validate(String token, TokenType type) {
        UserToken userToken =
                userTokenRepository
                        .findByTokenAndType(token, type)
                        .orElseThrow(() -> new IllegalArgumentException("Token is invalid"));
        if (!userToken.isActive(Instant.now(clock))) {
            throw new IllegalArgumentException("Token has expired or was revoked");
        }
        return userToken;
    }

    @Override
    @Transactional
    public UserToken consume(String token, TokenType type) {
        UserToken userToken = validate(token, type);
        userToken.setConsumedAt(Instant.now(clock));
        return userTokenRepository.save(userToken);
    }

    @Override
    @Transactional
    public void revokeTokens(User user, TokenType type) {
        userTokenRepository.deleteByUserAndType(user, type);
    }
}
