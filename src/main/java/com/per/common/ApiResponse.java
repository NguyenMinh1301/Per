package com.per.common;

import java.time.Instant;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.ApiSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    @Builder.Default private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(ApiSuccessCode successCode, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(successCode.getCode())
                .message(successCode.getDefaultMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(ApiSuccessCode successCode) {
        return success(successCode, null);
    }

    public static <T> ApiResponse<T> success(ApiSuccessCode successCode, String message, T data) {
        String resolved =
                (message == null || message.isBlank()) ? successCode.getDefaultMessage() : message;
        return ApiResponse.<T>builder()
                .success(true)
                .code(successCode.getCode())
                .message(resolved)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ApiErrorCode errorCode, String message, T data) {
        String resolved =
                (message == null || message.isBlank()) ? errorCode.getDefaultMessage() : message;
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(resolved)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ApiErrorCode errorCode) {
        return failure(errorCode, errorCode.getDefaultMessage(), null);
    }

    public static ApiResponse<Void> failure(ApiException ex) {
        return failure(ex.getErrorCode(), ex.getMessage(), null);
    }
}
