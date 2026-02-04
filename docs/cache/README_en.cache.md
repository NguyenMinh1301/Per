# Caching Strategy (Redis)

## 1. Overview
The **Cache Module** leverages **Redis** to improve read performance and reduce load on the primary database (PostgreSQL). It is implemented using Spring Boot's Caching Abstraction (`@Cacheable`).

## 2. Configuration (`CacheConfig`)

We use a custom `RedisCacheManager` with specific Time-to-Live (TTL) settings for different data types.

| Cache Name | TTL | Description |
| :--- | :--- | :--- |
| `products`, `product` | 10 Minutes | High-traffic catalog data. Shorter TTL ensures price/stock updates propagate reasonably fast. |
| `categories`, `brands` | 30 Minutes | Master data that rarely changes. |
| `Default` | 15 Minutes | Any other cached data. |

## 3. Serialization
We use **JSON Serialization** (`GenericJackson2JsonRedisSerializer`) instead of Java's native binary serialization.
-   **Why?**: JSON is human-readable (easier debugging in `redis-cli`) and language-agnostic.
-   **Details**: Includes `JavaTimeModule` to correctly handle `Instant` and `LocalDateTime`.

## 4. Auth Token Blacklist
Beyond `@Cacheable`, Redis is used to manage **JWT Refresh Tokens** and potentially blacklist compromised Access Tokens.
-   **Key Pattern**: `auth:refresh_token:{username}:{tokenId}`
-   **TTL**: Matches the refresh token validity (e.g., 14 days).

## 5. Operations
To clear the cache manually (e.g., after a bulk DB update):
```bash
redis-cli FLUSHDB
```
Or use the Actuator endpoint if exposed (currently disabled for security).
