package com.per.user.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiConstants;
import com.per.common.base.BaseController;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;
import com.per.user.dto.request.UserCreateRequest;
import com.per.user.dto.request.UserUpdateRequest;
import com.per.user.dto.response.UserResponse;
import com.per.user.service.UserService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.User.ROOT)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User", description = "User Management APIs")
public class UserController extends BaseController {

    private final UserService userService;

    @GetMapping(ApiConstants.User.SEARCH)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<UserResponse> response = userService.getUsers(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.USER_SEARCH_SUCCESS, response));
    }

    @GetMapping(ApiConstants.User.GET)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") UUID id) {
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.USER_FETCH_SUCCESS, response));
    }

    @PostMapping(ApiConstants.User.CREATE)
    @RateLimiter(name = "lowTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.USER_CREATE_SUCCESS, response));
    }

    @PutMapping(ApiConstants.User.UPDATE)
    @RateLimiter(name = "lowTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") UUID id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.USER_UPDATE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.User.DELETE)
    @RateLimiter(name = "lowTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.USER_DELETE_SUCCESS));
    }
}
