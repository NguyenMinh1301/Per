package com.per.common.base;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.per.common.exception.ApiErrorCode;
import com.per.common.response.ApiResponse;

public abstract class BaseController {

    public ResponseEntity<ApiResponse<Void>> rateLimit(Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.failure(ApiErrorCode.TOO_MANY_REQUESTS));
    }

    public ResponseEntity<ApiResponse<Void>> circuitBreaker(Throwable throwable) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(ApiErrorCode.SERVICE_UNAVAILABLE));
    }
}
