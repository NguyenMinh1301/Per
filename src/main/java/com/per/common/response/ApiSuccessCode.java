package com.per.common.response;

import lombok.Getter;

@Getter
public enum ApiSuccessCode {
    // spotless:off

    // Auth
    AUTH_REGISTER_SUCCESS("AUTH_REGISTER_SUCCESS", "Registration completed successfully"),
    AUTH_LOGIN_SUCCESS("AUTH_LOGIN_SUCCESS", "Authentication successful"),
    AUTH_REFRESH_SUCCESS("AUTH_REFRESH_SUCCESS", "Token refresh successful"),
    AUTH_LOGOUT_SUCCESS("AUTH_LOGOUT_SUCCESS", "Logout completed successfully"),
    AUTH_INTROSPECT_SUCCESS("AUTH_INTROSPECT_SUCCESS", "Token introspection completed"),
    AUTH_VERIFY_SUCCESS("AUTH_VERIFY_SUCCESS", "Email verification successful"),
    AUTH_FORGOT_SUCCESS("AUTH_FORGOT_SUCCESS", "If the email exists, reset instructions have been sent"),
    AUTH_RESET_SUCCESS("AUTH_RESET_SUCCESS", "Password reset successful"),
    AUTH_RESET_TOKEN_VALID("AUTH_RESET_TOKEN_VALID", "Reset token validated, please provide a new password"),
    AUTH_ME_SUCCESS("AUTH_ME_SUCCESS", "Current user profile retrieved successfully"),

    // User
    USER_CREATE_SUCCESS("USER_CREATE_SUCCESS", "User created successfully"),
    USER_UPDATE_SUCCESS("USER_UPDATE_SUCCESS", "User updated successfully"),
    USER_DELETE_SUCCESS("USER_DELETE_SUCCESS", "User deleted successfully"),
    USER_FETCH_SUCCESS("USER_FETCH_SUCCESS", "User retrieved successfully"),
    USER_LIST_SUCCESS("USER_LIST_SUCCESS", "User list retrieved successfully"),
    USER_SEARCH_SUCCESS("USER_SEARCH_SUCCESS", "User search completed successfully"),

    // Brand
    BRAND_CREATE_SUCCESS("BRAND_CREATE_SUCCESS", "Brand has been created successfully"),
    BRAND_UPDATE_SUCCESS("BRAND_UPDATE_SUCCESS", "Brand has been updated successfully"),
    BRAND_DELETE_SUCCESS("BRAND_DELETE_SUCCESS", "Brand has been deleted successfully"),
    BRAND_FETCH_SUCCESS("BRAND_FETCH_SUCCESS", "Brand details retrieved successfully"),
    BRAND_LIST_SUCCESS("BRAND_LIST_SUCCESS", "Brand list retrieved successfully"),

    // Category
    CATEGORY_CREATE_SUCCESS("CATEGORY_CREATE_SUCCESS", "Category has been created successfully"),
    CATEGORY_UPDATE_SUCCESS("CATEGORY_UPDATE_SUCCESS", "Category has been updated successfully"),
    CATEGORY_DELETE_SUCCESS("CATEGORY_DELETE_SUCCESS", "Category has been deleted successfully"),
    CATEGORY_FETCH_SUCCESS("CATEGORY_FETCH_SUCCESS", "Category details retrieved successfully"),
    CATEGORY_LIST_SUCCESS("CATEGORY_LIST_SUCCESS", "Category list retrieved successfully"),

    // MadeIn
    MADEIN_CREATE_SUCCESS("MADEIN_CREATE_SUCCESS", "Made in has been created successfully"),
    MADEIN_UPDATE_SUCCESS("MADEIN_UPDATE_SUCCESS", "Made in has been updated successfully"),
    MADEIN_DELETE_SUCCESS("MADEIN_DELETE_SUCCESS", "Made in has been deleted successfully"),
    MADEIN_FETCH_SUCCESS("MADEIN_FETCH_SUCCESS", "Made in details retrieved successfully"),
    MADEIN_LIST_SUCCESS("MADEIN_LIST_SUCCESS", "Made in list retrieved successfully"),

    // Product
    PRODUCT_CREATE_SUCCESS("PRODUCT_CREATE_SUCCESS", "Product has been created successfully"),
    PRODUCT_UPDATE_SUCCESS("PRODUCT_UPDATE_SUCCESS", "Product has been updated successfully"),
    PRODUCT_DELETE_SUCCESS("PRODUCT_DELETE_SUCCESS", "Product has been deleted successfully"),
    PRODUCT_FETCH_SUCCESS("PRODUCT_FETCH_SUCCESS", "Product details retrieved successfully"),
    PRODUCT_LIST_SUCCESS("PRODUCT_LIST_SUCCESS", "Product list retrieved successfully"),
    PRODUCT_VARIANT_CREATE_SUCCESS("PRODUCT_VARIANT_CREATE_SUCCESS", "Product variant has been created successfully"),
    PRODUCT_VARIANT_UPDATE_SUCCESS("PRODUCT_VARIANT_UPDATE_SUCCESS", "Product variant has been updated successfully"),
    PRODUCT_VARIANT_DELETE_SUCCESS("PRODUCT_VARIANT_DELETE_SUCCESS", "Product variant has been deleted successfully"),

    // Cart
    CART_FETCH_SUCCESS("CART_FETCH_SUCCESS", "Cart retrieved successfully"),
    CART_ITEM_ADD_SUCCESS("CART_ITEM_ADD_SUCCESS", "Item added to cart successfully"),
    CART_ITEM_UPDATE_SUCCESS("CART_ITEM_UPDATE_SUCCESS", "Cart item updated successfully"),
    CART_ITEM_REMOVE_SUCCESS("CART_ITEM_REMOVE_SUCCESS", "Cart item removed successfully"),
    CART_CLEAR_SUCCESS("CART_CLEAR_SUCCESS", "Cart cleared successfully"),

    // Payment
    PAYMENT_CHECKOUT_SUCCESS("PAYMENT_CHECKOUT_SUCCESS", "Checkout link created successfully"),
    PAYMENT_WEBHOOK_SUCCESS("PAYMENT_WEBHOOK_SUCCESS", "Payment webhook processed successfully"),
    PAYMENT_RETURN_SUCCESS("PAYMENT_RETURN_SUCCESS", "Payment status retrieved successfully"),

    // AI Assistant
    RAG_CHAT_SUCCESS("RAG_CHAT_SUCCESS", "Assistant response generated successfully"),
    RAG_INDEX_SUCCESS("RAG_INDEX_SUCCESS", "Product catalog indexed successfully"),
    RAG_KNOWLEDGE_INDEX_SUCCESS("RAG_KNOWLEDGE_INDEX_SUCCESS", "Knowledge base indexed successfully"),
    RAG_KNOWLEDGE_DELETE_SUCCESS("RAG_KNOWLEDGE_DELETE_SUCCESS", "Knowledge base cleared successfully"),

    // Media
    MEDIA_UPLOAD_SUCCESS("MEDIA_UPLOAD_SUCCESS", "Media uploaded successfully"),
    MEDIA_UPLOAD_BATCH_SUCCESS("MEDIA_UPLOAD_BATCH_SUCCESS", "Media files uploaded successfully");

    // spotless:on
    private final String code;
    private final String defaultMessage;

    ApiSuccessCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
