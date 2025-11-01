package com.per.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.user.dto.request.UserCreateRequest;
import com.per.user.dto.request.UserUpdateRequest;
import com.per.user.dto.response.UserResponse;
import com.per.user.entity.User;
import com.per.user.repository.RoleAdminRepository;
import com.per.user.repository.UserAdminRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserAdminRepository userRepository;

    @Mock
    private RoleAdminRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userRole = new Role(RoleType.USER, "User role");
        adminRole = new Role(RoleType.ADMIN, "Admin role");

        testUser =
                User.builder()
                        .id(userId)
                        .username("testuser")
                        .email("test@example.com")
                        .password("encoded_password")
                        .firstName("Test")
                        .lastName("User")
                        .emailVerified(true)
                        .active(true)
                        .build();

        testUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    @Nested
    @DisplayName("Get Users Tests")
    class GetUsersTests {

        @Test
        @DisplayName("Should return paginated users when query is null")
        void shouldReturnPaginatedUsersWhenQueryIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(java.util.List.of(testUser));

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> result = userService.getUsers(null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");

            verify(userRepository).findAll(pageable);
            verify(userRepository, never()).search(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return searched users when query is provided")
        void shouldReturnSearchedUsersWhenQueryIsProvided() {
            // Given
            String query = "test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(java.util.List.of(testUser));

            when(userRepository.search(query, pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> result = userService.getUsers(query, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(userRepository).search(query, pageable);
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should return user by id")
        void shouldReturnUserById() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            UserResponse result = userService.getUser(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("testuser");

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUser(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.NOT_FOUND);

            verify(userRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            UserCreateRequest request =
                    UserCreateRequest.builder()
                            .username("newuser")
                            .email("new@example.com")
                            .password("password123")
                            .firstName("New")
                            .lastName("User")
                            .emailVerified(false)
                            .active(true)
                            .roles(Set.of(RoleType.USER))
                            .build();

            User newUser =
                    User.builder()
                            .id(UUID.randomUUID())
                            .username("newuser")
                            .email("new@example.com")
                            .password("encoded_password")
                            .firstName("New")
                            .lastName("User")
                            .emailVerified(false)
                            .active(true)
                            .build();

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(newUser);

            // When
            UserResponse result = userService.createUser(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getEmail()).isEqualTo("new@example.com");

            verify(userRepository).findByUsername("newuser");
            verify(userRepository).findByEmail("new@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should assign default USER role when roles not provided")
        void shouldAssignDefaultUserRoleWhenRolesNotProvided() {
            // Given
            UserCreateRequest request =
                    UserCreateRequest.builder()
                            .username("newuser")
                            .email("new@example.com")
                            .password("password123")
                            .roles(null)
                            .build();

            User newUser =
                    User.builder()
                            .id(UUID.randomUUID())
                            .username("newuser")
                            .email("new@example.com")
                            .password("encoded_password")
                            .build();

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(newUser);

            // When
            UserResponse result = userService.createUser(request);

            // Then
            assertThat(result).isNotNull();
            verify(roleRepository).findByName(RoleType.USER);
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            UserCreateRequest request =
                    UserCreateRequest.builder()
                            .username("existinguser")
                            .email("new@example.com")
                            .password("password123")
                            .build();

            User existingUser =
                    User.builder()
                            .id(UUID.randomUUID())
                            .username("existinguser")
                            .email("existing@example.com")
                            .build();

            when(userRepository.findByUsername("existinguser"))
                    .thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.USER_USERNAME_CONFLICT);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            UserCreateRequest request =
                    UserCreateRequest.builder()
                            .username("newuser")
                            .email("existing@example.com")
                            .password("password123")
                            .build();

            User existingUser =
                    User.builder()
                            .id(UUID.randomUUID())
                            .username("existinguser")
                            .email("existing@example.com")
                            .build();

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("existing@example.com"))
                    .thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.USER_EMAIL_CONFLICT);

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            UserUpdateRequest request =
                    UserUpdateRequest.builder()
                            .username("updateduser")
                            .email("updated@example.com")
                            .firstName("Updated")
                            .lastName("User")
                            .emailVerified(true)
                            .active(true)
                            .roles(Set.of(RoleType.ADMIN))
                            .build();

            User updatedUser =
                    User.builder()
                            .id(userId)
                            .username("updateduser")
                            .email("updated@example.com")
                            .password("encoded_password")
                            .firstName("Updated")
                            .lastName("User")
                            .emailVerified(true)
                            .active(true)
                            .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleType.ADMIN)).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            // When
            UserResponse result = userService.updateUser(userId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("updateduser");
            assertThat(result.getEmail()).isEqualTo("updated@example.com");

            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update password when provided")
        void shouldUpdatePasswordWhenProvided() {
            // Given
            UserUpdateRequest request =
                    UserUpdateRequest.builder().password("newpassword123").build();

            User updatedUser =
                    User.builder()
                            .id(userId)
                            .username("testuser")
                            .email("test@example.com")
                            .password("new_encoded_password")
                            .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newpassword123")).thenReturn("new_encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            // When
            UserResponse result = userService.updateUser(userId, request);

            // Then
            assertThat(result).isNotNull();
            verify(passwordEncoder).encode("newpassword123");
        }

        @Test
        @DisplayName("Should throw exception when updated username already exists")
        void shouldThrowExceptionWhenUpdatedUsernameExists() {
            // Given
            UserUpdateRequest request =
                    UserUpdateRequest.builder().username("existinguser").build();

            User existingUser =
                    User.builder()
                            .id(UUID.randomUUID())
                            .username("existinguser")
                            .email("existing@example.com")
                            .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("existinguser"))
                    .thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(userId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.USER_USERNAME_CONFLICT);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).findById(userId);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.NOT_FOUND);

            verify(userRepository, never()).delete(any(User.class));
        }
    }
}

