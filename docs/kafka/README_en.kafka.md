# Event-Driven Architecture (Kafka)

## 1. Overview
We use **Apache Kafka** to decouple high-latency or side-effect operations from the main user request flow. This ensures the API remains fast and responsive.

## 2. Topics & Events

| Topic | Event Class | Producer | Consumer | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `product.index` | `ProductIndexEvent` | `ProductService` | `ProductIndexConsumer` | Syncs DB changes to Elasticsearch. |
| `notification.email` | `SendEmailEvent` | `AuthService`, `OrderService` | `EmailNotificationListener` | Sends transactional emails (Welcome, OTP). |

## 3. Resilience & Reliability

### Retry Mechanism
We use Spring Kafka's **Blocking Retry** for transient failures (e.g., Elasticsearch temporary unavailability).
-   **Attempts**: 3
-   **Backoff**: Exponential (1s -> 2s -> 4s)

### Dead Letter Queue (DLQ)
If an event fails after all retries, it is moved to a **DLQ Topic** (e.g., `product.index-dlt`).
-   **Monitoring**: Alerts should be configured on DLQ topics to notify devs of data inconsistency.
-   **Recovery**: Use a specialized script/tool to replay messages from DLQ after fixing the root cause.

## 4. Configuration
Defined in `KafkaConfig.java` and `KafkaTopicNames.java`.
-   **Serialization**: JSON (`JsonSerializer`) for payloads.
-   **Consumer Groups**: Each module defines its own group (e.g., `product-group`) to allow independent scaling.
