package com.per.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ApiErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "Invalid request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "Requested resource was not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "Access to this resource is forbidden", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS(
            "AUTH_INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    USER_USERNAME_CONFLICT(
            "USER_USERNAME_CONFLICT", "Username already exists", HttpStatus.CONFLICT),
    USER_EMAIL_CONFLICT("USER_EMAIL_CONFLICT", "Email already exists", HttpStatus.CONFLICT),
    BRAND_NAME_CONFLICT(
            "BRAND_NAME_CONFLICT",
            "A brand with the provided name already exists",
            HttpStatus.CONFLICT),
    BRAND_NOT_FOUND(
            "BRAND_NOT_FOUND", "The requested brand could not be found", HttpStatus.NOT_FOUND),
    MEDIA_FILE_REQUIRED("MEDIA_FILE_REQUIRED", "Media file is required", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_TOO_LARGE(
            "MEDIA_FILE_TOO_LARGE",
            "Media file exceeds the allowed size limit",
            HttpStatus.PAYLOAD_TOO_LARGE),
    MEDIA_UNSUPPORTED_TYPE(
            "MEDIA_UNSUPPORTED_TYPE", "Media type is not supported", HttpStatus.BAD_REQUEST),
    MEDIA_UPLOAD_FAILED(
            "MEDIA_UPLOAD_FAILED",
            "Unable to store media at this time",
            HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR(
            "INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ApiErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
