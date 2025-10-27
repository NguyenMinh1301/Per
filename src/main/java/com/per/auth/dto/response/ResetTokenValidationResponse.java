package com.per.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetTokenValidationResponse {
    private final String token;
}
