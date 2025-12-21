package com.per.common.base;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.per.common.exception.ApiErrorCode;
import com.per.common.response.ApiResponse;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

public abstract class BaseController {

    /**
     * Fallback method for rate limiter. Only handles RequestNotPermitted exceptions. Business
     * exceptions will propagate to GlobalExceptionHandler.
     */
    public ResponseEntity<ApiResponse<Void>> rateLimit(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.failure(ApiErrorCode.TOO_MANY_REQUESTS));
    }

    /**
     * Fallback method for circuit breaker. Only handles CallNotPermittedException. Business
     * exceptions will propagate to GlobalExceptionHandler.
     */
    public ResponseEntity<ApiResponse<Void>> circuitBreaker(CallNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(ApiErrorCode.SERVICE_UNAVAILABLE));
    }
}
