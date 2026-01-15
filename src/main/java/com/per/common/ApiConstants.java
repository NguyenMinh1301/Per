package com.per.common;

public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_VERSION
            // = "/api/v1";
            = "/per";

    public static final class Auth {
        private Auth() {}

        public static final String ROOT = API_VERSION + "/auth";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String REFRESH = "/refresh";
        public static final String INTROSPECT = "/introspect";
        public static final String VERIFY_EMAIL = "/verify-email";
        public static final String FORGOT_PASSWORD = "/forgot-password";
        public static final String RESET_PASSWORD = "/reset-password";
        public static final String ME = "/me";
    }

    public static final class Media {
        private Media() {}

        public static final String ROOT = API_VERSION + "/media";
        public static final String UPLOAD = "/upload";
        public static final String UPLOAD_BATCH = "/upload/batch";
    }

    public static final class Brand {
        private Brand() {}

        public static final String ROOT = API_VERSION + "/brands";
        public static final String LIST = "/list";
        public static final String DETAIL = "/detail/{id}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update/{id}";
        public static final String DELETE = "/delete/{id}";
        public static final String SEARCH = "/search";
        public static final String REINDEX = "/reindex";
    }

    public static final class Category {
        private Category() {}

        public static final String ROOT = API_VERSION + "/categories";
        public static final String LIST = "/list";
        public static final String DETAIL = "/detail/{id}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update/{id}";
        public static final String DELETE = "/delete/{id}";
        public static final String SEARCH = "/search";
        public static final String REINDEX = "/reindex";
    }

    public static final class MadeIn {
        private MadeIn() {}

        public static final String ROOT = API_VERSION + "/made-in";
        public static final String LIST = "/list";
        public static final String DETAIL = "/detail/{id}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update/{id}";
        public static final String DELETE = "/delete/{id}";
        public static final String SEARCH = "/search";
        public static final String REINDEX = "/reindex";
    }

    public static final class Product {
        private Product() {}

        public static final String ROOT = API_VERSION + "/products";
        public static final String LIST = "/list";
        public static final String DETAIL = "/detail/{id}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update/{id}";
        public static final String DELETE = "/delete/{id}";
        public static final String SEARCH = "/search";
        public static final String REINDEX = "/reindex";
    }

    public static final class ProductVariant {
        private ProductVariant() {}

        public static final String ROOT = Product.ROOT + "/{productId}/variants";
        public static final String LIST = "/list";
        public static final String DETAIL = "/detail/{variantId}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update/{variantId}";
        public static final String DELETE = "/delete/{variantId}";
    }

    public static final class Payment {
        private Payment() {}

        public static final String ROOT = API_VERSION + "/payments";
        public static final String CHECKOUT = "/checkout";
        public static final String WEBHOOK = API_VERSION + "/payments/payos/webhook";
        public static final String RETURN = "/payments/payos/return";
        public static final String CANCEL = "/payments/payos/return";
    }

    public static final class Cart {
        private Cart() {}

        public static final String ROOT = API_VERSION + "/cart";
        public static final String ITEMS = "/items";
        public static final String ITEM_DETAILS = "/items/{itemId}";
    }

    public static final class Rag {
        private Rag() {}

        public static final String ROOT = API_VERSION + "/rag";
        public static final String CHAT = "/chat";
        public static final String CHAT_STREAM = "/chat/stream";
        public static final String INDEX = "/index";
        public static final String INDEX_KNOWLEDGE = "/index/knowledge";
        public static final String DELETE_KNOWLEDGE = "/knowledge";
        public static final String KNOWLEDGE_STATUS = "/knowledge/status";
    }

    public static final class User {
        private User() {}

        public static final String ROOT = API_VERSION + "/users";
        public static final String SEARCH = "/search";
        public static final String GET = "/{id}";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/{id}";
        public static final String DELETE = "/{id}";
    }
}
