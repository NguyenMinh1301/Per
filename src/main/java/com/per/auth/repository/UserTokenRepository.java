package com.per.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.auth.entity.TokenType;
import com.per.user.entity.User;
import com.per.auth.entity.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByTokenAndType(String token, TokenType type);

    void deleteByUserAndType(User user, TokenType type);

    long deleteByUserAndTypeAndExpiresAtBefore(User user, TokenType type, Instant threshold);
}
