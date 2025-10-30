package com.per.user.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;
import com.per.user.dto.request.UserCreateRequest;
import com.per.user.dto.request.UserUpdateRequest;
import com.per.user.dto.response.UserResponse;

public interface UserService {

    PageResponse<UserResponse> getUsers(String query, Pageable pageable);

    UserResponse getUser(UUID id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UUID id, UserUpdateRequest request);

    void deleteUser(UUID id);
}
