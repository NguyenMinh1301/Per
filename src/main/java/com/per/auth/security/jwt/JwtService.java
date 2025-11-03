package com.per.auth.security.jwt;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.per.auth.configuration.JwtProperties;
import com.per.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private final Clock clock;

    public String generateAccessToken(User user) {
        return buildToken(
                user,
                properties.getAccessTtl(),
                JwtTokenType.ACCESS,
                Map.of("roles", extractRoleNames(user)));
    }

    public String generateRefreshToken(User user) {
        return buildToken(
                user,
                properties.getRefreshTtl(),
                JwtTokenType.REFRESH,
                Map.of("jti", UUID.randomUUID().toString()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails, JwtTokenType type) {
        try {
            Claims claims = extractAllClaims(token);
            String subject = claims.getSubject();
            String tokenType = claims.get("type", String.class);
            return subject.equals(userDetails.getUsername())
                    && JwtTokenType.valueOf(tokenType) == type
                    && !isExpired(claims.getExpiration());
        } catch (IllegalArgumentException | JwtException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String userId = extractAllClaims(token).get("userId", String.class);
        return UUID.fromString(userId);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(properties.signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isExpired(Date expiration) {
        return expiration.before(Date.from(Instant.now(clock)));
    }

    private String buildToken(
            User user,
            java.time.Duration ttl,
            JwtTokenType tokenType,
            Map<String, Object> extraClaims) {
        Instant now = Instant.now(clock);
        Map<String, Object> claims = buildDefaultClaims(user, tokenType);
        claims.putAll(extraClaims);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .signWith(properties.signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Map<String, Object> buildDefaultClaims(User user, JwtTokenType tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType.name());
        claims.put("userId", user.getId().toString());
        return claims;
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
