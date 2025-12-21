Rate Limiting Overview
======================

The application uses **Resilience4j Rate Limiter** to protect public endpoints from abuse and ensure fair usage. Rate limiting restricts the number of requests a client can make within a specified time window.

Architecture
------------

```
┌──────────────────────────────────────────────────────────────────┐
│                     Request Flow                                  │
│  Client → Filter/Annotation → RateLimiter → Controller → Service │
│                     ↓ (if limit exceeded)                         │
│                 429 Too Many Requests                             │
└──────────────────────────────────────────────────────────────────┘
```

Two implementation approaches are used:

1. **Annotation-based** (`@RateLimiter`) - For controller methods
2. **Filter-based** (`SwaggerRateLimitFilter`) - For pre-controller interception

Key Components
--------------

| File | Purpose |
| --- | --- |
| `com/per/common/config/resilience4j/resilience4j-*.yml` | Rate limiter instance configurations |
| `SwaggerRateLimitFilter.java` | Servlet filter for Swagger UI rate limiting |
| `AuthController.java` | Uses `@RateLimiter` annotation |
| `MediaController.java` | Uses `@RateLimiter` + `@CircuitBreaker` |
| `BaseController.java` | Contains fallback methods for rate limit responses |

> **Note**: Resilience4j configuration is separated into dedicated files and imported into `application-*.yml` via `spring.config.import`.

Rate Limiter Instances
----------------------

### Authentication Endpoints

| Instance | Endpoints | Description |
| --- | --- | --- |
| `authStrict` | `/register`, `/login`, `/reset-password` | High-risk operations |
| `authModerate` | `/refresh`, `/introspect` | Token management |
| `authVeryStrict` | `/verify-email`, `/forgot-password` | Sensitive operations |

### Media Endpoints

| Instance | Endpoints | Description |
| --- | --- | --- |
| `mediaSingle` | `/upload` | Single file upload |
| `mediaMultipart` | `/upload/batch` | Batch file upload |

### Infrastructure

| Instance | Endpoints | Description |
| --- | --- | --- |
| `swagger` | `/api-docs/**`, `/swagger-ui/**` | API documentation UI |

Configuration Parameters
------------------------

Each rate limiter instance is configured with:

| Parameter | Description |
| --- | --- |
| `limitForPeriod` | Maximum number of permissions in the refresh period |
| `limitRefreshPeriod` | Duration after which permissions are refreshed |
| `timeoutDuration` | Maximum wait time for permission (0 = fail immediately) |
| `registerHealthIndicator` | Expose metrics to health endpoint |

Example configuration (values will vary based on server capacity):

```yaml
resilience4j:
  ratelimiter:
    instances:
      authStrict:
        limitForPeriod: <N>
        limitRefreshPeriod: <duration>
        timeoutDuration: <duration>
        registerHealthIndicator: true
```

Annotation-Based Rate Limiting
------------------------------

Applied directly to controller methods using `@RateLimiter`:

```java
@PostMapping("/login")
@RateLimiter(name = "authStrict", fallbackMethod = "rateLimit")
public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody SigninRequest request) {
    // Method implementation
}
```

Parameters:
- `name`: References the instance defined in configuration
- `fallbackMethod`: Method called when rate limit is exceeded

### Fallback Method

Defined in `BaseController`:

```java
public ResponseEntity<ApiResponse<Void>> rateLimit(Throwable ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.failure(ApiErrorCode.TOO_MANY_REQUESTS));
}
```

Filter-Based Rate Limiting
--------------------------

For non-annotated endpoints (like static resources), use servlet filters:

### SwaggerRateLimitFilter

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SwaggerRateLimitFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     FilterChain filterChain) {
        String path = request.getRequestURI();

        if (path.startsWith("/per/api-docs") || path.startsWith("/per/swagger-ui")) {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("swagger");
            
            if (!rateLimiter.acquirePermission()) {
                handleRateLimitExceeded(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

Key points:
- Uses `RateLimiterRegistry` to obtain limiter instances
- Calls `acquirePermission()` to check availability
- Returns 429 response if limit exceeded

Error Response
--------------

When rate limit is exceeded, the API returns:

```json
{
  "success": false,
  "code": "TOO_MANY_REQUESTS",
  "message": "Too many requests. Please try again later.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

HTTP Status: `429 Too Many Requests`

Combining with Circuit Breaker
------------------------------

For external service calls, combine rate limiting with circuit breaker:

```java
@PostMapping("/upload")
@RateLimiter(name = "mediaSingle", fallbackMethod = "fallback")
@CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(@RequestPart("file") MultipartFile file) {
    // Upload to external service (e.g., Cloudinary)
}
```

Order of execution:
1. Rate Limiter checks request quota
2. If allowed, Circuit Breaker checks service health
3. If healthy, request proceeds to controller

See [Circuit Breaker documentation](../circuit-breaker/README.md) for details.

Monitoring
----------

### Health Endpoint

Rate limiter metrics are exposed via Spring Actuator:

```bash
GET /actuator/health
```

Response includes rate limiter status when `registerHealthIndicator: true`.

### Available Metrics

- Available permissions
- Number of waiting threads
- Metrics per instance

Testing Rate Limits
-------------------

### Manual Testing

```bash
# Rapid-fire requests to trigger rate limit
for i in {1..50}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/per/auth/login
done
```

Expected: Initial requests return `200`, then `429` when limit exceeded.

### Integration Testing

```java
@Test
void shouldReturnTooManyRequestsWhenRateLimitExceeded() {
    for (int i = 0; i < LIMIT + 1; i++) {
        mockMvc.perform(post("/api/v1/auth/login").content(validRequest));
    }
    
    mockMvc.perform(post("/api/v1/auth/login").content(validRequest))
           .andExpect(status().isTooManyRequests());
}
```

Best Practices
--------------

1. **Tune limits based on monitoring** - Start conservative, adjust based on actual usage patterns
2. **Use different tiers** - Strict for sensitive ops, moderate for normal ops
3. **Set appropriate timeouts** - 0 for immediate fail, higher for queuing
4. **Combine patterns** - Use with Circuit Breaker for external calls
5. **Document limits** - Include in API documentation for consumers

Extending Rate Limiting
-----------------------

### Adding a New Rate Limiter

1. Define instance in `resilience4j-dev.yml` and `resilience4j-prod.yml`:
   ```yaml
   # File: src/main/java/com/per/common/config/resilience4j/resilience4j-dev.yml
   resilience4j:
     ratelimiter:
       instances:
         myNewLimiter:
           limitForPeriod: <N>
           limitRefreshPeriod: <duration>
           timeoutDuration: <duration>
           registerHealthIndicator: true
   ```

2. Apply to controller method:
   ```java
   @RateLimiter(name = "myNewLimiter", fallbackMethod = "rateLimit")
   public ResponseEntity<?> myEndpoint() {
       // Implementation
   }
   ```

### Custom Key Resolution

By default, rate limiting is global per instance. For per-user limiting:

```java
@RateLimiter(name = "userSpecific")
public ResponseEntity<?> userEndpoint(@AuthenticationPrincipal UserDetails user) {
    // Key resolver would need custom configuration
}
```

Requires custom `RateLimiterConfigCustomizer` implementation.

Troubleshooting
---------------

### Rate Limit Not Working

1. Verify instance name matches between config and annotation
2. Check that fallback method signature matches (accepts `Throwable`)
3. Ensure `@EnableRetry` or AOP is not interfering

### Too Aggressive Limiting

1. Increase `limitForPeriod`
2. Decrease `limitRefreshPeriod`
3. Add `timeoutDuration` to allow queuing

### Swagger UI Completely Blocked

The Swagger filter applies to all resources including CSS/JS. Ensure `limitForPeriod` accounts for all static assets loaded by the UI.
