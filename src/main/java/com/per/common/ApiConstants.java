package com.per.common;

public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_V1 = "/api/v1";

    public static final class Auth {
        private Auth() {}

        public static final String ROOT = API_V1 + "/auth";
        public static final String REGISTER = ROOT + "/register";
        public static final String LOGIN = ROOT + "/login";
        public static final String LOGOUT = ROOT + "/logout";
        public static final String REFRESH = ROOT + "/refresh";
        public static final String VERIFY_EMAIL = ROOT + "/verify-email";
        public static final String FORGOT_PASSWORD = ROOT + "/forgot-password";
        public static final String RESET_PASSWORD = ROOT + "/reset-password";
        public static final String ME = ROOT + "/me";
    }

    public static final class Media {
        private Media() {}

        public static final String ROOT = API_V1 + "/media";
        public static final String UPLOAD = "/upload";
        public static final String UPLOAD_BATCH = "/upload/batch";
    }

    public static final class Brand {
        private Brand() {}

        public static final String ROOT = API_V1 + "/brands";
        public static final String DETAILS = ROOT + "/{id}";
    }

    public static final class Category {
        private Category() {}

        public static final String ROOT = API_V1 + "/categories";
        public static final String DETAILS = ROOT + "/{id}";
    }

    public static final class MadeIn {
        private MadeIn() {}

        public static final String ROOT = API_V1 + "/made-in";
        public static final String DETAILS = ROOT + "/{id}";
    }

    public static final class Product {
        private Product() {}

        public static final String ROOT = API_V1 + "/products";
        public static final String DETAILS = "/{id}";
    }

    public static final class ProductVariant {
        private ProductVariant() {}

        public static final String ROOT = Product.ROOT + "/{productId}/variants";
        public static final String DETAILS = "/{variantId}";
    }

    public static final class User {
        private User() {}

        public static final String ROOT = API_V1 + "/users";
        public static final String SEARCH = ROOT + "/search";
        public static final String GET = ROOT + "/{id}";
        public static final String CREATE = ROOT;
        public static final String UPDATE = ROOT + "/{id}";
        public static final String DELETE = ROOT + "/{id}";
    }
}
