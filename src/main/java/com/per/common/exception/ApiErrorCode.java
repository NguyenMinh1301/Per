package com.per.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ApiErrorCode {
    // spotless:off

    // Common
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "Invalid request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "Requested resource was not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "Access to this resource is forbidden", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required", HttpStatus.UNAUTHORIZED),
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "The system is busy. Please try again in a few minutes.", HttpStatus.TOO_MANY_REQUESTS),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "External service temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),

    // Auth
    AUTH_INVALID_CREDENTIALS("BAD_REQUEST", "Invalid username or password", HttpStatus.UNAUTHORIZED),

    // User
    USER_USERNAME_CONFLICT("USER_USERNAME_CONFLICT", "Username already exists", HttpStatus.CONFLICT),
    USER_EMAIL_CONFLICT("USER_EMAIL_CONFLICT", "Email already exists", HttpStatus.CONFLICT),

    // Brand
    BRAND_NAME_CONFLICT("BRAND_NAME_CONFLICT", "A brand with the provided name already exists", HttpStatus.CONFLICT),
    BRAND_NOT_FOUND("BRAND_NOT_FOUND", "The requested brand could not be found", HttpStatus.NOT_FOUND),

    // Category
    CATEGORY_NAME_CONFLICT("CATEGORY_NAME_CONFLICT", "A category with the provided name already exists", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "The requested category could not be found", HttpStatus.NOT_FOUND),

    // MadeIn
    MADEIN_NAME_CONFLICT("MADEIN_NAME_CONFLICT", "A made in with the provided name already exists", HttpStatus.CONFLICT),
    MADEIN_NOT_FOUND("MADEIN_NOT_FOUND", "The requested made in could not be found", HttpStatus.NOT_FOUND),

    // Product
    PRODUCT_NAME_CONFLICT("PRODUCT_NAME_CONFLICT", "A product with the provided name already exists", HttpStatus.CONFLICT),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "The requested product could not be found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_SKU_CONFLICT("PRODUCT_VARIANT_SKU_CONFLICT", "A product variant with the provided SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_NOT_FOUND("PRODUCT_VARIANT_NOT_FOUND", "The requested product variant could not be found", HttpStatus.NOT_FOUND),

    // Cart
    CART_NOT_FOUND("CART_NOT_FOUND", "The requested cart could not be found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "The requested cart item could not be found", HttpStatus.NOT_FOUND),
    CART_ITEM_OUT_OF_STOCK("CART_ITEM_OUT_OF_STOCK", "Requested quantity exceeds available stock", HttpStatus.BAD_REQUEST),

    // Media
    MEDIA_FILE_REQUIRED("MEDIA_FILE_REQUIRED", "Media file is required", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_TOO_LARGE("MEDIA_FILE_TOO_LARGE", "Media file exceeds the allowed size limit", HttpStatus.PAYLOAD_TOO_LARGE),
    MEDIA_UNSUPPORTED_TYPE("MEDIA_UNSUPPORTED_TYPE", "Media type is not supported", HttpStatus.BAD_REQUEST),
    MEDIA_UPLOAD_FAILED("MEDIA_UPLOAD_FAILED", "Unable to store media at this time", HttpStatus.INTERNAL_SERVER_ERROR),

    // Payment
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "The requested order could not be found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Payment information could not be found", HttpStatus.NOT_FOUND),
    PAYMENT_GATEWAY_ERROR("PAYMENT_GATEWAY_ERROR", "Unable to create payment link at this time", HttpStatus.BAD_GATEWAY),
    PAYMENT_WEBHOOK_INVALID("PAYMENT_WEBHOOK_INVALID", "Invalid payment webhook payload", HttpStatus.BAD_REQUEST);

    // spotless:on
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
