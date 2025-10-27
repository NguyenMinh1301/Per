package com.per.auth.service.token.impl;

import java.time.Duration;
import java.util.Objects;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.per.auth.configuration.JwtProperties;
import com.per.auth.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService implements RefreshTokenService {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public void store(String token, String username) {
        Duration ttl = jwtProperties.getRefreshTtl();
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofDays(30);
        }
        redisTemplate.opsForValue().set(buildKey(token), username, ttl);
    }

    @Override
    public boolean isValid(String token, String username) {
        String stored = redisTemplate.opsForValue().get(buildKey(token));
        return Objects.equals(stored, username);
    }

    @Override
    public void revoke(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
