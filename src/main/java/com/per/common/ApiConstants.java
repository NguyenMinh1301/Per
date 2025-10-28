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
    }

    public static final class Media {
        private Media() {}

        public static final String ROOT = API_V1 + "/media";
        public static final String UPLOAD = "/upload";
        public static final String UPLOAD_BATCH = "/upload/batch";
        public static final String UPLOAD_ENDPOINT = ROOT + UPLOAD;
        public static final String UPLOAD_BATCH_ENDPOINT = ROOT + UPLOAD_BATCH;
    }

    public static final class User {
        private User() {}

        public static final String ROOT = API_V1 + "/users";
    }
}
