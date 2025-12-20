Tổng Quan Kafka Messaging
=========================

Ứng dụng sử dụng Apache Kafka cho giao tiếp bất đồng bộ theo hướng sự kiện. Hiện tại, Kafka xử lý việc gửi email, tách biệt luồng xác thực khỏi độ trễ SMTP.

Kiến Trúc
---------

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Email Sending Flow                               │
│                                                                      │
│  AuthService ──► KafkaTemplate.send() ──► [email-topic] ──►          │
│                                            EmailConsumer ──►         │
│                                            MailService ──► SMTP      │
└──────────────────────────────────────────────────────────────────────┘
```

Lợi ích của async email:
* Đăng ký/reset mật khẩu trả về ngay lập tức
* Lỗi SMTP không chặn API responses
* Retry có thể được xử lý bởi Kafka consumer

Các Thành Phần Chính
--------------------

| File | Mục đích |
| --- | --- |
| `KafkaConfig.java` | Cấu hình producer và consumer factory |
| `EmailEvent.java` | Payload sự kiện cho email messages |
| `EmailConsumer.java` | Kafka listener xử lý email events |
| `AuthServiceImpl.java` | Producer publish email events |

Topics và Consumer Groups
-------------------------

| Topic | Consumer Group | Mục đích |
| --- | --- | --- |
| `email-topic` | `email-group` | Hàng đợi gửi email |

Cấu Hình
--------

Kết nối Kafka được cấu hình qua Spring Boot properties:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Cấu Hình Producer

```java
@Bean
public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
}
```

### Cấu Hình Consumer

```java
@Bean
public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "email-group");
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    return new DefaultKafkaConsumerFactory<>(configProps);
}
```

Định Dạng Event
---------------

### EmailEvent

```java
public class EmailEvent {
    private String to;      // Địa chỉ email người nhận
    private String subject; // Tiêu đề email
    private String content; // Nội dung email HTML
}
```

Được serialize dưới dạng JSON khi gửi đến Kafka.

Publish Events
--------------

Events được publish sử dụng `KafkaTemplate`:

```java
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private void sendVerificationEmail(User user, String token) {
        String subject = "Verify your email";
        String content = buildVerificationEmailContent(token);
        kafkaTemplate.send("email-topic", new EmailEvent(user.getEmail(), subject, content));
    }
    
    private void sendPasswordResetEmail(User user, String token) {
        String subject = "Reset your password";
        String content = buildPasswordResetEmailContent(token);
        kafkaTemplate.send("email-topic", new EmailEvent(user.getEmail(), subject, content));
    }
}
```

Consume Events
--------------

`EmailConsumer` lắng nghe topic và ủy quyền cho `MailService`:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final MailService mailService;

    @KafkaListener(topics = "email-topic", groupId = "email-group")
    public void consume(EmailEvent event) {
        log.info("Received email event: {}", event);
        mailService.sendEmail(event.getTo(), event.getSubject(), event.getContent());
    }
}
```

Các Use Cases Hiện Tại
----------------------

| Luồng | Event Được Publish | Trigger |
| --- | --- | --- |
| Đăng ký người dùng | Email xác minh | Sau khi tạo user |
| Quên mật khẩu | Link reset mật khẩu | Khi yêu cầu quên mật khẩu |

Xử Lý Lỗi
---------

Triển khai hiện tại log lỗi nhưng không implement retry logic. Cho production:

* Cấu hình `spring.kafka.consumer.enable-auto-commit: false`
* Thêm error handler vào `ConcurrentKafkaListenerContainerFactory`
* Cân nhắc dead letter topic cho các messages thất bại

Ghi Chú Phát Triển
------------------

### Phát Triển Local

Đảm bảo Kafka đang chạy locally hoặc qua Docker:

```bash
# Sử dụng Docker Compose
docker-compose up -d kafka zookeeper
```

### Biến Môi Trường

| Biến | Mặc định | Mô tả |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Địa chỉ Kafka broker |

### Testing

Cho unit tests, mock `KafkaTemplate` để xác minh events được publish:

```java
@Mock
private KafkaTemplate<String, Object> kafkaTemplate;

@Test
void shouldPublishEmailEventOnRegistration() {
    authService.register(request);
    
    verify(kafkaTemplate).send(eq("email-topic"), any(EmailEvent.class));
}
```

Cho integration tests, sử dụng embedded Kafka hoặc Testcontainers.

Mở Rộng Sử Dụng Kafka
---------------------

### Thêm Event Type Mới

1. Tạo event class trong `com.per.common.event`:
   ```java
   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   public class OrderCreatedEvent {
       private UUID orderId;
       private UUID userId;
       private BigDecimal amount;
   }
   ```

2. Tạo consumer trong module liên quan:
   ```java
   @Component
   @RequiredArgsConstructor
   public class OrderEventConsumer {
       
       @KafkaListener(topics = "order-topic", groupId = "order-group")
       public void consume(OrderCreatedEvent event) {
           // Xử lý event
       }
   }
   ```

3. Publish từ service:
   ```java
   kafkaTemplate.send("order-topic", orderCreatedEvent);
   ```

### Thêm Consumer Group Mới

Cập nhật `KafkaConfig.consumerFactory()` hoặc tạo factory riêng cho các cấu hình group khác nhau.

### Quản Lý Topic

Cho production, tạo topics một cách tường minh với partitioning phù hợp:

```bash
kafka-topics.sh --create --topic email-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

Monitoring
----------

Kafka metrics có thể được expose qua Spring Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,kafka
```

Theo dõi consumer lag để đảm bảo messages được xử lý kịp thời.
