package com.per.common.base;

import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    public ResponseEntity<String> rateLimit(Throwable throwable) {
        return ResponseEntity.status(429).body("Too many request");
    }

    public ResponseEntity<String> circuitBreaker(Throwable throwable) {
        return ResponseEntity.status(503).body("Service Unavailable");
    }
}
