package com.per.auth.service.token;

public interface RefreshTokenService {
    void store(String token, String username);

    boolean isValid(String token, String username);

    void revoke(String token);
}
