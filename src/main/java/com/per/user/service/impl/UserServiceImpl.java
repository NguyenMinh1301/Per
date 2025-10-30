package com.per.user.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.user.dto.request.UserCreateRequest;
import com.per.user.dto.request.UserUpdateRequest;
import com.per.user.dto.response.UserResponse;
import com.per.user.entity.User;
import com.per.user.mapper.UserMapper;
import com.per.user.repository.RoleAdminRepository;
import com.per.user.repository.UserAdminRepository;
import com.per.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserAdminRepository userRepository;
    private final RoleAdminRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String query, Pageable pageable) {
        Page<User> page;

        if (query == null || query.isBlank()) {
            page = userRepository.findAllBy(pageable);
        } else {
            page = userRepository.search(query.trim(), pageable);
        }

        return PageResponse.from(page.map(UserMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        User user = findUser(id);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        validateUsernameUnique(request.getUsername(), null);
        validateEmailUnique(request.getEmail(), null);

        User user =
                User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .emailVerified(request.isEmailVerified())
                        .active(request.isActive())
                        .lastLoginAt(null)
                        .build();

        assignRoles(user, resolveRoles(request.getRoles()));
        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = findUser(id);

        if (request.getUsername() != null
                && !request.getUsername().equalsIgnoreCase(user.getUsername())) {
            validateUsernameUnique(request.getUsername(), id);
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            validateEmailUnique(request.getEmail(), id);
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null) {
            assignRoles(user, resolveRoles(request.getRoles()));
        }

        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    private void validateUsernameUnique(String username, UUID currentId) {
        if (username == null) {
            return;
        }
        userRepository
                .findByUsername(username)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(
                        existing -> {
                            throw new ApiException(ApiErrorCode.USER_USERNAME_CONFLICT);
                        });
    }

    private void validateEmailUnique(String email, UUID currentId) {
        if (email == null) {
            return;
        }
        userRepository
                .findByEmail(email)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(
                        existing -> {
                            throw new ApiException(ApiErrorCode.USER_EMAIL_CONFLICT);
                        });
    }

    private void assignRoles(User user, Set<Role> roles) {
        user.setRoles(new HashSet<>(roles));
    }

    private Set<Role> resolveRoles(Set<RoleType> requestedRoles) {
        Set<RoleType> effectiveRoles =
                (requestedRoles == null || requestedRoles.isEmpty())
                        ? Set.of(RoleType.USER)
                        : requestedRoles;

        return effectiveRoles.stream()
                .map(
                        roleType ->
                                roleRepository
                                        .findByName(roleType)
                                        .orElseThrow(
                                                () ->
                                                        new ApiException(
                                                                ApiErrorCode.BAD_REQUEST,
                                                                "Role "
                                                                        + roleType
                                                                        + " does not exist")))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private User findUser(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "User does not exist"));
    }
}
