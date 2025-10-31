package com.per.common.util;

import java.util.Optional;

import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;

public final class ApiPreconditions {

    private ApiPreconditions() {}

    public static <T> T checkFound(Optional<T> value, ApiErrorCode errorCode) {
        return value.orElseThrow(() -> new ApiException(errorCode));
    }

    public static <T> T checkFound(Optional<T> value, ApiErrorCode errorCode, String message) {
        return value.orElseThrow(() -> new ApiException(errorCode, message));
    }

    public static void checkArgument(boolean condition, ApiErrorCode errorCode, String message) {
        if (!condition) {
            throw new ApiException(errorCode, message);
        }
    }

    public static void checkState(boolean condition, ApiErrorCode errorCode, String message) {
        if (!condition) {
            throw new ApiException(errorCode, message);
        }
    }

    public static void checkUnique(boolean exists, ApiErrorCode errorCode, String message) {
        if (exists) {
            throw new ApiException(errorCode, message);
        }
    }
}
