Kafka Messaging Overview
========================

The application uses Apache Kafka for asynchronous event-driven communication. Currently, Kafka handles email delivery, decoupling the authentication flow from SMTP latency.

Architecture
------------

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Email Sending Flow                               │
│                                                                      │
│  AuthService ──► KafkaTemplate.send() ──► [email-topic] ──►          │
│                                            EmailConsumer ──►         │
│                                            MailService ──► SMTP      │
└──────────────────────────────────────────────────────────────────────┘
```

Benefits of async email:
* User registration/password reset returns immediately
* SMTP failures do not block API responses
* Retries can be handled by Kafka consumer

Key Components
--------------

| File | Purpose |
| --- | --- |
| `KafkaConfig.java` | Producer and consumer factory configuration |
| `KafkaTopicNames.java` | Centralized topic name constants |
| `EmailEvent.java` | Event payload for email messages |
| `EmailConsumer.java` | Kafka listener with retry and DLQ support |
| `AuthServiceImpl.java` | Producer that publishes email events |

Topics and Consumer Groups
--------------------------

| Topic | Consumer Group | Purpose |
| --- | --- | --- |
| `email-topic` | `email-group` | Email delivery queue |
| `email-topic-dlt` | `email-group` | Dead Letter Topic for failed emails |

Configuration
-------------

Kafka connection is configured via Spring Boot properties:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Producer Configuration

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

### Consumer Configuration

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

Event Format
------------

### EmailEvent

```java
public class EmailEvent {
    private String to;      // Recipient email address
    private String subject; // Email subject line
    private String content; // HTML email body
}
```

Serialized as JSON when sent to Kafka.

Publishing Events
-----------------

Events are published using `KafkaTemplate`:

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

Consuming Events
----------------

The `EmailConsumer` listens to the topic and delegates to `MailService`:

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

Current Use Cases
-----------------

| Flow | Event Published | Trigger |
| --- | --- | --- |
| User Registration | Email verification | After user creation |
| Forgot Password | Password reset link | On forgot password request |

Error Handling and Dead Letter Queue (DLQ)
-------------------------------------------

The application implements retry with exponential backoff and Dead Letter Queue:

### Retry Mechanism

```java
@RetryableTopic(
    attempts = "4",  // 1 initial + 3 retries
    backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "email-topic", groupId = "email-group")
public void consume(EmailEvent event) {
    mailService.sendEmail(event.getTo(), event.getSubject(), event.getContent());
}
```

| Attempt | Delay |
| --- | --- |
| 1 (initial) | 0s |
| 2 (retry 1) | 1s |
| 3 (retry 2) | 2s |
| 4 (retry 3) | 4s |
| DLT | after 4th failure |

### Dead Letter Queue

Messages failing after all retries are sent to `email-topic-dlt`:

```java
@DltHandler
public void handleDlt(EmailEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.error("Email failed permanently: {}, Topic: {}", event.getTo(), topic);
}
```

### Monitoring DLQ via Kafka UI

1. Access Kafka UI at `http://localhost:8090`
2. Navigate to Topics → `email-topic-dlt`
3. Review failed messages for manual processing

Development Notes
-----------------

### Local Development

Ensure Kafka is running locally or via Docker (KRaft mode, no Zookeeper required):

```bash
# Using Docker Compose
docker compose up -d kafka
```

### Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |

### Testing

For unit tests, mock `KafkaTemplate` to verify events are published:

```java
@Mock
private KafkaTemplate<String, Object> kafkaTemplate;

@Test
void shouldPublishEmailEventOnRegistration() {
    authService.register(request);
    
    verify(kafkaTemplate).send(eq("email-topic"), any(EmailEvent.class));
}
```

For integration tests, use embedded Kafka or Testcontainers.

Extending Kafka Usage
---------------------

### Adding a New Event Type

1. Create event class in `com.per.common.event`:
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

2. Create consumer in relevant module:
   ```java
   @Component
   @RequiredArgsConstructor
   public class OrderEventConsumer {
       
       @KafkaListener(topics = "order-topic", groupId = "order-group")
       public void consume(OrderCreatedEvent event) {
           // Handle event
       }
   }
   ```

3. Publish from service:
   ```java
   kafkaTemplate.send("order-topic", orderCreatedEvent);
   ```

### Adding New Consumer Group

Update `KafkaConfig.consumerFactory()` or create separate factory for different group configurations.

### Topic Management

For production, create topics explicitly with proper partitioning:

```bash
kafka-topics.sh --create --topic email-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

Monitoring
----------

Kafka metrics can be exposed via Spring Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,kafka
```

Monitor consumer lag to ensure messages are processed timely.
