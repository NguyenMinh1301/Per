package com.per.common.base;

import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    public ResponseEntity<String> fallback(Throwable throwable) {
        return ResponseEntity.status(429).body("Too many request");
    }
}
