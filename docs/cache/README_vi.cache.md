# Caching Strategy (Redis)

## 1. Tổng quan
**Cache Module** tận dụng **Redis** để cải thiện hiệu suất đọc và giảm tải cho cơ sở dữ liệu chính (PostgreSQL). Nó được triển khai bằng cách sử dụng Caching Abstraction của Spring Boot (`@Cacheable`).

## 2. Cấu hình (`CacheConfig`)

Chúng tôi sử dụng `RedisCacheManager` tùy chỉnh với các cài đặt Time-to-Live (TTL) cụ thể cho các loại dữ liệu khác nhau.

| Cache Name | TTL | Mô tả |
| :--- | :--- | :--- |
| `products`, `product` | 10 Phút | Dữ liệu danh mục lưu lượng cao. TTL ngắn đảm bảo cập nhật giá/tồn kho lan truyền nhanh hợp lý. |
| `categories`, `brands` | 30 Phút | Dữ liệu chính (Master data) hiếm khi thay đổi. |
| `Default` | 15 Phút | Bất kỳ dữ liệu cache nào khác. |

## 3. Serialization
Chúng tôi sử dụng **JSON Serialization** (`GenericJackson2JsonRedisSerializer`) thay vì native binary serialization của Java.
-   **Tại sao?**: JSON dễ đọc (dễ debug trong `redis-cli`) và không phụ thuộc ngôn ngữ.
-   **Chi tiết**: Bao gồm `JavaTimeModule` để xử lý chính xác `Instant` và `LocalDateTime`.

## 4. Auth Token Blacklist
Ngoài `@Cacheable`, Redis được sử dụng để quản lý **JWT Refresh Tokens** và có thể là blacklist các Access Tokens bị xâm phạm.
-   **Key Pattern**: `auth:refresh_token:{username}:{tokenId}`
-   **TTL**: Khớp với hiệu lực của refresh token (ví dụ: 14 ngày).

## 5. Operations
Để xóa cache thủ công (ví dụ: sau khi cập nhật DB hàng loạt):
```bash
redis-cli FLUSHDB
```
Hoặc sử dụng endpoint Actuator nếu được hiển thị (hiện đang tắt vì lý do bảo mật).
