Tổng Quan Circuit Breaker
=========================

Ứng dụng sử dụng **Resilience4j Circuit Breaker** để bảo vệ chống lại cascading failures khi gọi các external services. Nó ngăn chặn các lời gọi lặp lại đến service đang lỗi, cho phép thời gian để phục hồi.

Kiến Trúc
---------

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

| Trạng thái | Hành vi |
| --- | --- |
| **CLOSED** | Hoạt động bình thường; requests được chuyển qua |
| **OPEN** | Requests fail ngay lập tức mà không gọi service |
| **HALF_OPEN** | Cho phép giới hạn test requests để kiểm tra phục hồi |

Các Thành Phần Chính
--------------------

| File | Mục đích |
| --- | --- |
| `resilience4j/resilience4j-*.yml` | Cấu hình các circuit breaker instances |
| `MediaController.java` | Sử dụng `@CircuitBreaker` cho external uploads |
| `BaseController.java` | Chứa các fallback methods |

> **Lưu ý**: Config resilience4j được tách ra file riêng và import vào `application-*.yml` qua `spring.config.import`.

Sử Dụng Hiện Tại
----------------

Circuit breaker được áp dụng cho các **Media** endpoints tương tác với external cloud storage:

| Instance | Endpoints | External Service |
| --- | --- | --- |
| `media` | `/upload`, `/upload/batch` | Cloudinary |

Các Tham Số Cấu Hình
--------------------

| Tham số | Mô tả |
| --- | --- |
| `slidingWindowSize` | Số lượng calls được sử dụng để tính failure rate |
| `minimumNumberOfCalls` | Số calls tối thiểu trước khi tính failure rate |
| `waitDurationInOpenState` | Thời gian circuit ở trạng thái OPEN trước khi chuyển sang HALF_OPEN |
| `failureRateThreshold` | Phần trăm failure để trip circuit (0-100) |
| `permittedNumberOfCallsInHalfOpenState` | Số test calls được phép trong trạng thái HALF_OPEN |
| `eventConsumerBufferSize` | Buffer size cho event publishing |
| `registerHealthIndicator` | Expose metrics ra health endpoint |

Ví dụ cấu hình:

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

Triển Khai
----------

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

Khi circuit OPEN, fallback được gọi. Fallback chỉ xử lý exception `CallNotPermittedException` từ Resilience4j. Các exceptions khác (ví dụ: business logic errors) được propagate đến `GlobalExceptionHandler`.

```java
public ResponseEntity<ApiResponse<Void>> circuitBreaker(CallNotPermittedException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.failure(ApiErrorCode.SERVICE_UNAVAILABLE));
}
```

**Quan trọng**: Fallback method signature phải:
- Chấp nhận các tham số giống với method gốc (tùy chọn)
- Chấp nhận exception type cụ thể của Resilience4j (`CallNotPermittedException`) là tham số cuối
- Trả về kiểu giống với method gốc

Kết Hợp Với Rate Limiter
------------------------

Để bảo vệ toàn diện, kết hợp cả hai patterns:

```java
@PostMapping("/upload")
@RateLimiter(name = "mediaSingle", fallbackMethod = "fallback")
@CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(@RequestPart("file") MultipartFile file) {
    // Implementation
}
```

Thứ tự thực thi:
1. **Rate Limiter** → Kiểm tra request quota
2. **Circuit Breaker** → Kiểm tra service health
3. **Method** → Thực thi nếu cả hai cho phép

Luồng Chuyển Đổi Trạng Thái
---------------------------

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. CLOSED (bình thường)                                             │
│    - Tất cả requests được chuyển qua                                │
│    - Failures được ghi lại trong sliding window                     │
│    - Khi failure rate >= threshold → chuyển sang OPEN               │
├─────────────────────────────────────────────────────────────────────┤
│ 2. OPEN (circuit tripped)                                           │
│    - Tất cả requests fail ngay với fallback                         │
│    - Không có calls đến external service                            │
│    - Sau waitDuration → chuyển sang HALF_OPEN                       │
├─────────────────────────────────────────────────────────────────────┤
│ 3. HALF_OPEN (testing recovery)                                     │
│    - Cho phép giới hạn test calls                                   │
│    - Nếu test calls thành công → chuyển sang CLOSED                 │
│    - Nếu test calls fail → chuyển về OPEN                           │
└─────────────────────────────────────────────────────────────────────┘
```

Error Response
--------------

Khi circuit OPEN:

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

Bao gồm circuit breaker state khi `registerHealthIndicator: true`.

### Circuit Breaker Events

Các events được publish để monitoring:
- `CircuitBreakerOnSuccess`
- `CircuitBreakerOnError`
- `CircuitBreakerOnStateTransition`

Use Cases
---------

### Khi Nào Sử Dụng Circuit Breaker

| Kịch bản | Khuyến nghị |
| --- | --- |
| External API calls (Cloudinary, Stripe, v.v.) | Có |
| Database queries | Thường không |
| Internal service calls | Tùy thuộc |
| File system operations | Không |

### Tại Sao Media Sử Dụng Circuit Breaker

Module Media upload files lên Cloudinary (external service):

1. **Network issues** có thể gây timeouts
2. **Cloudinary outages** sẽ fail tất cả uploads
3. **Rate limiting bởi Cloudinary** có thể gây cascading failures

Circuit breaker ngăn chặn:
- Lãng phí tài nguyên cho các requests chắc chắn fail
- Overwhelm service đang gặp khó khăn
- Trải nghiệm người dùng kém do long timeouts

Testing
-------

### Giả Lập Circuit Open

```java
@Test
void shouldOpenCircuitAfterFailures() {
    // Cấu hình mock để fail
    when(cloudinaryService.upload(any())).thenThrow(new RuntimeException("Cloudinary down"));
    
    // Thực hiện đủ calls để trip circuit
    for (int i = 0; i < MINIMUM_CALLS; i++) {
        assertThrows(RuntimeException.class, () -> mediaService.upload(file));
    }
    
    // Xác minh circuit đã open (fallback được gọi mà không gọi service)
    // Call tiếp theo sẽ fail fast
}
```

### Integration Testing

```bash
# Khởi động application với Cloudinary disabled/unavailable
# Thực hiện nhiều upload requests
# Quan sát circuit opening sau failure threshold
# Chờ waitDuration
# Quan sát half-open test requests
```

Best Practices
--------------

1. **Cấu hình thresholds phù hợp** - Dựa trên service SLA và tolerance
2. **Sử dụng meaningful fallbacks** - Trả về cached data, default values, hoặc graceful errors
3. **Monitor state transitions** - Alert khi circuit opening để cảnh báo sớm
4. **Đặt wait duration hợp lý** - Cân bằng giữa quick recovery và overwhelming service
5. **Kết hợp với timeouts** - Ngăn chặn long-hanging requests

Mở Rộng Circuit Breaker
-----------------------

### Thêm Circuit Breaker cho Service Mới

1. Thêm cấu hình instance trong file `resilience4j-dev.yml` và `resilience4j-prod.yml`:
   ```yaml
   # File: src/main/resources/resilience4j/resilience4j-dev.yml
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

2. Áp dụng annotation:
   ```java
   @CircuitBreaker(name = "newExternalService", fallbackMethod = "handleCircuitOpen")
   public Response callExternalService() {
       // Implementation
   }
   
   public Response handleCircuitOpen(Throwable ex) {
       // Fallback logic
   }
   ```

### Xử Lý Exception Tùy Chỉnh

Cấu hình exceptions nào được tính là failures:

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

Xử Lý Sự Cố
-----------

### Circuit Không Open

1. Xác minh `minimumNumberOfCalls` đã đạt
2. Kiểm tra `slidingWindowSize` phù hợp
3. Đảm bảo exceptions đang được recorded (không bị ignored)

### Circuit Open Quá Nhanh

1. Tăng `failureRateThreshold`
2. Tăng `slidingWindowSize` để có nhiều samples hơn
3. Xem xét liệu transient errors có nên bị ignored

### Fallback Không Được Gọi

1. Kiểm tra fallback method signature khớp
2. Xác minh method ở cùng class hoặc accessible
3. Đảm bảo Spring AOP proxy không bị bypassed

### Service Không Bao Giờ Recover

1. Giảm `waitDurationInOpenState`
2. Tăng `permittedNumberOfCallsInHalfOpenState`
3. Kiểm tra nếu external service cần can thiệp thủ công

Tài Liệu Liên Quan
------------------

- [Rate Limiting](../rate-limit/README_vi.md) - Quản lý request quota
- [Media Module](../../module/media/README_vi.md) - Chức năng upload file
