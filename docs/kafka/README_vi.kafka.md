# Event-Driven Architecture (Kafka)

## 1. Tổng quan
Chúng tôi sử dụng **Apache Kafka** để tách biệt (decouple) các hoạt động có độ trễ cao hoặc có side-effect khỏi luồng yêu cầu người dùng chính. Điều này đảm bảo API vẫn nhanh và phản hồi tốt.

## 2. Topics & Events

| Topic | Event Class | Producer | Consumer | Mục đích |
| :--- | :--- | :--- | :--- | :--- |
| `product.index` | `ProductIndexEvent` | `ProductService` | `ProductIndexConsumer` | Đồng bộ thay đổi DB sang Elasticsearch. |
| `notification.email` | `SendEmailEvent` | `AuthService`, `OrderService` | `EmailNotificationListener` | Gửi email giao dịch (Welcome, OTP). |

## 3. Resilience & Reliability

### Retry Mechanism
Chúng tôi sử dụng **Blocking Retry** của Spring Kafka cho các lỗi tạm thời (ví dụ: Elasticsearch tạm thời không khả dụng).
-   **Attempts**: 3
-   **Backoff**: Exponential (1s -> 2s -> 4s)

### Dead Letter Queue (DLQ)
Nếu một event thất bại sau tất cả các lần thử lại, nó sẽ được chuyển đến **DLQ Topic** (ví dụ: `product.index-dlt`).
-   **Monitoring**: Cần định cấu hình cảnh báo trên các DLQ topics để thông báo cho dev về sự không nhất quán dữ liệu.
-   **Recovery**: Sử dụng script/tool chuyên dụng để replay tin nhắn từ DLQ sau khi sửa nguyên nhân gốc rễ.

## 4. Cấu hình
Được định nghĩa trong `KafkaConfig.java` và `KafkaTopicNames.java`.
-   **Serialization**: JSON (`JsonSerializer`) cho payloads.
-   **Consumer Groups**: Mỗi module xác định group riêng (ví dụ: `product-group`) để cho phép mở rộng độc lập.
