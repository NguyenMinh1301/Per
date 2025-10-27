package com.per.auth.mapper;

import java.util.stream.Collectors;

import com.per.auth.dto.response.user.MeResponse;
import com.per.user.entity.User;

public final class AuthUserMapper {

    private AuthUserMapper() {}

    public static MeResponse toMeResponse(User user) {
        return MeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .roles(
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet()))
                .build();
    }
}
