package com.per.auth.service.token.db;

import com.per.auth.entity.TokenType;
import com.per.user.entity.User;
import com.per.auth.entity.UserToken;

public interface UserTokenService {
    UserToken create(User user, TokenType type, java.time.Duration ttl);

    UserToken validate(String token, TokenType type);

    UserToken consume(String token, TokenType type);

    void revokeTokens(User user, TokenType type);
}
