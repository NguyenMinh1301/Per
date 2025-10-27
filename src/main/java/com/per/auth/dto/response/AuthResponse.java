package com.per.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private UserResponse user;
    private AuthTokenResponse tokens;
}
