Circuit Breaker Overview
========================

The application uses **Resilience4j Circuit Breaker** to protect against cascading failures when calling external services. It prevents repeated calls to a failing service, allowing time for recovery.

Architecture
------------

```
┌───────────────────────────────────────────────────────────────────────┐
│                    Circuit Breaker States                             │
│                                                                       │
│  CLOSED ──(failure threshold)──► OPEN ──(wait duration)──► HALF_OPEN  │
│     ▲                              │                           │      │
│     │                              │                           │      │
│     └──────────(success)───────────┴───────(test calls)────────┘      │
└───────────────────────────────────────────────────────────────────────┘
```

| State | Behavior |
| --- | --- |
| **CLOSED** | Normal operation; requests pass through |
| **OPEN** | Requests fail immediately without calling the service |
| **HALF_OPEN** | Limited test requests allowed to check recovery |

Key Components
--------------

| File | Purpose |
| --- | --- |
| `application-*.yml` | Circuit breaker instance configurations |
| `MediaController.java` | Uses `@CircuitBreaker` for external uploads |
| `BaseController.java` | Contains fallback methods |

Current Usage
-------------

Circuit breaker is applied to **Media** endpoints that interact with external cloud storage:

| Instance | Endpoints | External Service |
| --- | --- | --- |
| `media` | `/upload`, `/upload/batch` | Cloudinary |

Configuration Parameters
------------------------

| Parameter | Description |
| --- | --- |
| `slidingWindowSize` | Number of calls used to calculate failure rate |
| `minimumNumberOfCalls` | Minimum calls before failure rate is calculated |
| `waitDurationInOpenState` | Duration circuit stays OPEN before transitioning to HALF_OPEN |
| `failureRateThreshold` | Failure percentage to trip the circuit (0-100) |
| `permittedNumberOfCallsInHalfOpenState` | Test calls allowed in HALF_OPEN state |
| `eventConsumerBufferSize` | Buffer size for event publishing |
| `registerHealthIndicator` | Expose metrics to health endpoint |

Example configuration:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      media:
        slidingWindowSize: <N>
        minimumNumberOfCalls: <N>
        waitDurationInOpenState: <duration>
        failureRateThreshold: <percentage>
        permittedNumberOfCallsInHalfOpenState: <N>
        registerHealthIndicator: true
```

Implementation
--------------

### Annotation-Based

```java
@PostMapping("/upload")
@CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(@RequestPart("file") MultipartFile file) {
    MediaUploadResponse response = mediaService.uploadSingle(file);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ApiSuccessCode.MEDIA_UPLOAD_SUCCESS, response));
}
```

### Fallback Method

When circuit is OPEN or call fails, fallback is invoked:

```java
public ResponseEntity<ApiResponse<Void>> circuitBreaker(Throwable ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.failure(ApiErrorCode.SERVICE_UNAVAILABLE));
}
```

**Important**: Fallback method signature must:
- Accept the same parameters as the original method (optional)
- Accept a `Throwable` as the last parameter
- Return the same type as the original method

Combining with Rate Limiter
---------------------------

For comprehensive protection, combine both patterns:

```java
@PostMapping("/upload")
@RateLimiter(name = "mediaSingle", fallbackMethod = "fallback")
@CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(@RequestPart("file") MultipartFile file) {
    // Implementation
}
```

Execution order:
1. **Rate Limiter** → Checks request quota
2. **Circuit Breaker** → Checks service health
3. **Method** → Executes if both allow

State Transition Flow
---------------------

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. CLOSED (normal)                                                  │
│    - All requests pass through                                      │
│    - Failures are recorded in sliding window                        │
│    - When failure rate >= threshold → transition to OPEN            │
├─────────────────────────────────────────────────────────────────────┤
│ 2. OPEN (circuit tripped)                                           │
│    - All requests fail immediately with fallback                    │
│    - No calls to external service                                   │
│    - After waitDuration → transition to HALF_OPEN                   │
├─────────────────────────────────────────────────────────────────────┤
│ 3. HALF_OPEN (testing recovery)                                     │
│    - Limited test calls allowed                                     │
│    - If test calls succeed → transition to CLOSED                   │
│    - If test calls fail → transition back to OPEN                   │
└─────────────────────────────────────────────────────────────────────┘
```

Error Response
--------------

When circuit is OPEN:

```json
{
  "success": false,
  "code": "SERVICE_UNAVAILABLE",
  "message": "External service temporarily unavailable. Please try again later.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

HTTP Status: `503 Service Unavailable`

Monitoring
----------

### Health Endpoint

```bash
GET /actuator/health
```

Includes circuit breaker state when `registerHealthIndicator: true`.

### Circuit Breaker Events

Events published for monitoring:
- `CircuitBreakerOnSuccess`
- `CircuitBreakerOnError`
- `CircuitBreakerOnStateTransition`

Use Cases
---------

### When to Use Circuit Breaker

| Scenario | Recommended |
| --- | --- |
| External API calls (Cloudinary, Stripe, etc.) | Yes |
| Database queries | Usually no |
| Internal service calls | Depends |
| File system operations | No |

### Why Media Uses Circuit Breaker

The Media module uploads files to Cloudinary (external service):

1. **Network issues** can cause timeouts
2. **Cloudinary outages** would fail all uploads
3. **Rate limiting by Cloudinary** could cause cascading failures

Circuit breaker prevents:
- Wasting resources on doomed requests
- Overwhelming a struggling service
- Poor user experience from long timeouts

Testing
-------

### Simulating Circuit Open

```java
@Test
void shouldOpenCircuitAfterFailures() {
    // Configure mock to fail
    when(cloudinaryService.upload(any())).thenThrow(new RuntimeException("Cloudinary down"));
    
    // Make enough calls to trip circuit
    for (int i = 0; i < MINIMUM_CALLS; i++) {
        assertThrows(RuntimeException.class, () -> mediaService.upload(file));
    }
    
    // Verify circuit is open (fallback invoked without calling service)
    // Next call should fail fast
}
```

### Integration Testing

```bash
# Start application with Cloudinary disabled/unavailable
# Make multiple upload requests
# Observe circuit opening after failure threshold
# Wait for waitDuration
# Observe half-open test requests
```

Best Practices
--------------

1. **Configure appropriate thresholds** - Based on service SLA and tolerance
2. **Use meaningful fallbacks** - Return cached data, default values, or graceful errors
3. **Monitor state transitions** - Alert on circuit opening for early warning
4. **Set reasonable wait duration** - Balance between quick recovery and overwhelming service
5. **Combine with timeouts** - Prevent long-hanging requests

Extending Circuit Breaker
-------------------------

### Adding Circuit Breaker to New Service

1. Add instance configuration:
   ```yaml
   resilience4j:
     circuitbreaker:
       instances:
         newExternalService:
           slidingWindowSize: <N>
           minimumNumberOfCalls: <N>
           waitDurationInOpenState: <duration>
           failureRateThreshold: <percentage>
           permittedNumberOfCallsInHalfOpenState: <N>
           registerHealthIndicator: true
   ```

2. Apply annotation:
   ```java
   @CircuitBreaker(name = "newExternalService", fallbackMethod = "handleCircuitOpen")
   public Response callExternalService() {
       // Implementation
   }
   
   public Response handleCircuitOpen(Throwable ex) {
       // Fallback logic
   }
   ```

### Custom Exception Handling

Configure which exceptions count as failures:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      media:
        recordExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignoreExceptions:
          - com.per.common.exception.ValidationException
```

Troubleshooting
---------------

### Circuit Not Opening

1. Verify `minimumNumberOfCalls` is met
2. Check `slidingWindowSize` is appropriate
3. Ensure exceptions are being recorded (not ignored)

### Circuit Opening Too Quickly

1. Increase `failureRateThreshold`
2. Increase `slidingWindowSize` for more samples
3. Review if transient errors should be ignored

### Fallback Not Invoked

1. Check fallback method signature matches
2. Verify method is in same class or accessible
3. Ensure Spring AOP proxy is not bypassed

### Service Never Recovers

1. Decrease `waitDurationInOpenState`
2. Increase `permittedNumberOfCallsInHalfOpenState`
3. Check if external service requires manual intervention

Related Documentation
---------------------

- [Rate Limiting](../rate-limit/README.md) - Request quota management
- [Media Module](../../module/media/README.md) - File upload functionality
