package com.per.user.mapper;

import java.util.stream.Collectors;

import com.per.user.dto.response.UserResponse;
import com.per.user.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
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
