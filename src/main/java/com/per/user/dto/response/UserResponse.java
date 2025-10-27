package com.per.user.dto.response;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private final UUID id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean emailVerified;
    private final boolean active;
    private final Set<String> roles;
}
