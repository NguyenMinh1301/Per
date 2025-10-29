package com.per.common.response;

import lombok.Getter;

@Getter
public enum ApiSuccessCode {
    AUTH_REGISTER_SUCCESS("AUTH_REGISTER_SUCCESS", "Registration completed successfully"),
    AUTH_LOGIN_SUCCESS("AUTH_LOGIN_SUCCESS", "Authentication successful"),
    AUTH_REFRESH_SUCCESS("AUTH_REFRESH_SUCCESS", "Token refresh successful"),
    AUTH_LOGOUT_SUCCESS("AUTH_LOGOUT_SUCCESS", "Logout completed successfully"),
    AUTH_VERIFY_SUCCESS("AUTH_VERIFY_SUCCESS", "Email verification successful"),
    AUTH_FORGOT_SUCCESS(
            "AUTH_FORGOT_SUCCESS", "If the email exists, reset instructions have been sent"),
    AUTH_RESET_SUCCESS("AUTH_RESET_SUCCESS", "Password reset successful"),
    AUTH_RESET_TOKEN_VALID(
            "AUTH_RESET_TOKEN_VALID", "Reset token validated, please provide a new password"),
    AUTH_ME_SUCCESS("AUTH_ME_SUCCESS", "Current user profile retrieved successfully"),

    USER_CREATE_SUCCESS("USER_CREATE_SUCCESS", "User created successfully"),
    USER_UPDATE_SUCCESS("USER_UPDATE_SUCCESS", "User updated successfully"),
    USER_DELETE_SUCCESS("USER_DELETE_SUCCESS", "User deleted successfully"),
    USER_FETCH_SUCCESS("USER_FETCH_SUCCESS", "User retrieved successfully"),
    USER_LIST_SUCCESS("USER_LIST_SUCCESS", "User list retrieved successfully"),
    USER_SEARCH_SUCCESS("USER_SEARCH_SUCCESS", "User search completed successfully"),

    BRAND_CREATE_SUCCESS("BRAND_CREATE_SUCCESS", "Brand has been created successfully"),
    BRAND_UPDATE_SUCCESS("BRAND_UPDATE_SUCCESS", "Brand has been updated successfully"),
    BRAND_DELETE_SUCCESS("BRAND_DELETE_SUCCESS", "Brand has been deleted successfully"),
    BRAND_FETCH_SUCCESS("BRAND_FETCH_SUCCESS", "Brand details retrieved successfully"),
    BRAND_LIST_SUCCESS("BRAND_LIST_SUCCESS", "Brand list retrieved successfully"),

    MEDIA_UPLOAD_SUCCESS("MEDIA_UPLOAD_SUCCESS", "Media uploaded successfully"),
    MEDIA_UPLOAD_BATCH_SUCCESS("MEDIA_UPLOAD_BATCH_SUCCESS", "Media files uploaded successfully");

    private final String code;
    private final String defaultMessage;

    ApiSuccessCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
