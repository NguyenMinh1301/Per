package com.per.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IntrospectRequest {

    @NotBlank(message = "Access token is required")
    private String accessToken;
}
