Tổng Quan Rate Limiting
=======================

Ứng dụng sử dụng **Resilience4j Rate Limiter** để bảo vệ các public endpoints khỏi lạm dụng và đảm bảo sử dụng công bằng. Rate limiting giới hạn số lượng requests mà client có thể thực hiện trong một khoảng thời gian xác định.

Kiến Trúc
---------

```
┌──────────────────────────────────────────────────────────────────┐
│                     Request Flow                                  │
│  Client → Filter/Annotation → RateLimiter → Controller → Service │
│                     ↓ (nếu vượt giới hạn)                         │
│                 429 Too Many Requests                             │
└──────────────────────────────────────────────────────────────────┘
```

Hai cách tiếp cận triển khai được sử dụng:

1. **Annotation-based** (`@RateLimiter`) - Cho controller methods
2. **Filter-based** (`SwaggerRateLimitFilter`) - Cho pre-controller interception

Các Thành Phần Chính
--------------------

| File | Mục đích |
| --- | --- |
| `resilience4j/resilience4j-*.yml` | Cấu hình các rate limiter instances |
| `SwaggerRateLimitFilter.java` | Servlet filter cho rate limiting Swagger UI |
| `AuthController.java` | Sử dụng `@RateLimiter` annotation |
| `MediaController.java` | Sử dụng `@RateLimiter` + `@CircuitBreaker` |
| `BaseController.java` | Chứa các fallback methods cho rate limit responses |

> **Lưu ý**: Config resilience4j được tách ra file riêng và import vào `application-*.yml` qua `spring.config.import`.

Các Rate Limiter Instances
--------------------------

### Authentication Endpoints

| Instance | Endpoints | Mô tả |
| --- | --- | --- |
| `authStrict` | `/register`, `/login`, `/reset-password` | Các thao tác rủi ro cao |
| `authModerate` | `/refresh`, `/introspect` | Quản lý token |
| `authVeryStrict` | `/verify-email`, `/forgot-password` | Các thao tác nhạy cảm |

### Media Endpoints

| Instance | Endpoints | Mô tả |
| --- | --- | --- |
| `mediaSingle` | `/upload` | Upload file đơn |
| `mediaMultipart` | `/upload/batch` | Upload hàng loạt |

### Infrastructure

| Instance | Endpoints | Mô tả |
| --- | --- | --- |
| `swagger` | `/api-docs/**`, `/swagger-ui/**` | API documentation UI |

Các Tham Số Cấu Hình
--------------------

Mỗi rate limiter instance được cấu hình với:

| Tham số | Mô tả |
| --- | --- |
| `limitForPeriod` | Số lượng permissions tối đa trong refresh period |
| `limitRefreshPeriod` | Thời gian sau đó permissions được refresh |
| `timeoutDuration` | Thời gian chờ tối đa cho permission (0 = fail ngay) |
| `registerHealthIndicator` | Expose metrics ra health endpoint |

Ví dụ cấu hình (giá trị sẽ thay đổi dựa trên capacity của server):

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

Áp dụng trực tiếp vào controller methods sử dụng `@RateLimiter`:

```java
@PostMapping("/login")
@RateLimiter(name = "authStrict", fallbackMethod = "rateLimit")
public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody SigninRequest request) {
    // Method implementation
}
```

Các tham số:
- `name`: Tham chiếu đến instance được định nghĩa trong cấu hình
- `fallbackMethod`: Method được gọi khi vượt rate limit

### Fallback Method

Được định nghĩa trong `BaseController`:

```java
public ResponseEntity<ApiResponse<Void>> rateLimit(Throwable ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.failure(ApiErrorCode.TOO_MANY_REQUESTS));
}
```

Filter-Based Rate Limiting
--------------------------

Cho các endpoints không có annotation (như static resources), sử dụng servlet filters:

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

Các điểm chính:
- Sử dụng `RateLimiterRegistry` để lấy các limiter instances
- Gọi `acquirePermission()` để kiểm tra availability
- Trả về 429 response nếu vượt giới hạn

Error Response
--------------

Khi vượt rate limit, API trả về:

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

Kết Hợp Với Circuit Breaker
---------------------------

Cho các external service calls, kết hợp rate limiting với circuit breaker:

```java
@PostMapping("/upload")
@RateLimiter(name = "mediaSingle", fallbackMethod = "fallback")
@CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(@RequestPart("file") MultipartFile file) {
    // Upload đến external service (ví dụ: Cloudinary)
}
```

Thứ tự thực thi:
1. Rate Limiter kiểm tra request quota
2. Nếu được phép, Circuit Breaker kiểm tra service health
3. Nếu healthy, request tiến hành đến controller

Xem [tài liệu Circuit Breaker](../circuit-breaker/README_vi.md) để biết chi tiết.

Monitoring
----------

### Health Endpoint

Rate limiter metrics được expose qua Spring Actuator:

```bash
GET /actuator/health
```

Response bao gồm rate limiter status khi `registerHealthIndicator: true`.

### Các Metrics Có Sẵn

- Available permissions
- Số thread đang chờ
- Metrics theo instance

Testing Rate Limits
-------------------

### Testing Thủ Công

```bash
# Gửi requests liên tục để trigger rate limit
for i in {1..50}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/per/auth/login
done
```

Kết quả mong đợi: Các requests ban đầu trả về `200`, sau đó `429` khi vượt giới hạn.

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

1. **Tune limits dựa trên monitoring** - Bắt đầu conservative, điều chỉnh dựa trên actual usage patterns
2. **Sử dụng các tiers khác nhau** - Strict cho sensitive ops, moderate cho normal ops
3. **Đặt timeouts phù hợp** - 0 cho immediate fail, cao hơn cho queuing
4. **Kết hợp các patterns** - Sử dụng với Circuit Breaker cho external calls
5. **Document limits** - Bao gồm trong API documentation cho consumers

Mở Rộng Rate Limiting
---------------------

### Thêm Rate Limiter Mới

1. Định nghĩa instance trong file `resilience4j-dev.yml` và `resilience4j-prod.yml`:
   ```yaml
   # File: src/main/resources/resilience4j/resilience4j-dev.yml
   resilience4j:
     ratelimiter:
       instances:
         myNewLimiter:
           limitForPeriod: <N>
           limitRefreshPeriod: <duration>
           timeoutDuration: <duration>
           registerHealthIndicator: true
   ```

2. Áp dụng vào controller method:
   ```java
   @RateLimiter(name = "myNewLimiter", fallbackMethod = "rateLimit")
   public ResponseEntity<?> myEndpoint() {
       // Implementation
   }
   ```

### Custom Key Resolution

Mặc định, rate limiting là global cho mỗi instance. Cho per-user limiting:

```java
@RateLimiter(name = "userSpecific")
public ResponseEntity<?> userEndpoint(@AuthenticationPrincipal UserDetails user) {
    // Key resolver cần custom configuration
}
```

Yêu cầu triển khai `RateLimiterConfigCustomizer` tùy chỉnh.

Xử Lý Sự Cố
-----------

### Rate Limit Không Hoạt Động

1. Xác minh instance name khớp giữa config và annotation
2. Kiểm tra fallback method signature khớp (chấp nhận `Throwable`)
3. Đảm bảo `@EnableRetry` hoặc AOP không interfering

### Limiting Quá Aggressive

1. Tăng `limitForPeriod`
2. Giảm `limitRefreshPeriod`
3. Thêm `timeoutDuration` để cho phép queuing

### Swagger UI Bị Block Hoàn Toàn

Swagger filter áp dụng cho tất cả resources bao gồm CSS/JS. Đảm bảo `limitForPeriod` tính đến tất cả static assets được load bởi UI.
