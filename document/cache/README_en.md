Cache Module Overview
=====================

The cache module provides Redis-based caching infrastructure for high-traffic read operations. It implements the **Cache-Aside pattern with Post-Commit Eviction** to ensure data consistency between the cache and database, even under high concurrency.

Architecture
------------

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Read Flow                                   │
│  Client → Controller → Service (@Cacheable) → Redis → (miss) → DB   │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        Write Flow                                   │
│  Client → Controller → Service → DB → Commit → Evict Cache          │
└─────────────────────────────────────────────────────────────────────┘
```

Key Components
--------------

| File | Purpose |
| --- | --- |
| `CacheNames.java` | Centralized cache name constants (DRY principle). |
| `CacheConfig.java` | Redis cache manager with customized TTL per cache type. |
| `CacheEvictionHelper.java` | Transaction-safe cache eviction utility. |

Cache Strategy
--------------

### Cache-Aside Pattern

Read operations check the cache first. On cache miss, data is fetched from the database and stored in cache for subsequent reads.

```java
@Cacheable(value = CacheNames.PRODUCTS, key = "'list:' + (#query ?: 'all')", sync = true)
public PageResponse<ProductResponse> getProducts(String query, Pageable pageable) {
    // Only executes on cache miss
    return productRepository.findAll(pageable);
}
```

### Post-Commit Eviction

Write operations invalidate cache **after** the database transaction commits. This prevents race conditions where stale data could be cached.

**Problem with naive eviction:**
```
Thread A: Evict cache → [Thread B reads old DB → caches stale value] → Commit DB
Result: Cache contains stale data!
```

**Solution with post-commit eviction:**
```
Thread A: Start transaction → Modify DB → Commit → Evict cache
Result: Cache always reflects the latest committed state
```

Implementation:
```java
@Override
public ProductResponse createProduct(ProductCreateRequest request) {
    Product saved = productRepository.save(product);
    
    // Eviction is registered but only executes after commit
    cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
    
    return productMapper.toResponse(saved);
}
```

TTL Configuration
-----------------

Time-To-Live (TTL) defines how long cached data remains valid before automatic expiration. TTL acts as a safety net—even if eviction fails, stale data expires.

| Cache Type | TTL | Rationale |
| --- | --- | --- |
| Product list/detail | 10 minutes | Higher traffic, more frequent updates |
| Master data (Category, Brand, MadeIn) | 30 minutes | Rarely changed reference data |
| Default | 15 minutes | Fallback for undefined caches |

Configuration in `CacheConfig.java`:
```java
private static final Duration PRODUCT_TTL = Duration.ofMinutes(10);
private static final Duration MASTER_DATA_TTL = Duration.ofMinutes(30);
```

Cache Names Reference
---------------------

All cache names are defined in `CacheNames.java`:

| Constant | Value | Used By |
| --- | --- | --- |
| `PRODUCTS` | `"products"` | Product list queries |
| `PRODUCT` | `"product"` | Single product by ID |
| `CATEGORIES` | `"categories"` | Category list queries |
| `CATEGORY` | `"category"` | Single category by ID |
| `BRANDS` | `"brands"` | Brand list queries |
| `BRAND` | `"brand"` | Single brand by ID |
| `MADE_INS` | `"madeIns"` | MadeIn list queries |
| `MADE_IN` | `"madeIn"` | Single madeIn by ID |

Key Annotations
---------------

### @Cacheable

Applied to read methods. Spring checks cache before method execution.

| Parameter | Purpose |
| --- | --- |
| `value` | Cache name from `CacheNames` |
| `key` | SpEL expression for cache key |
| `sync = true` | Prevents cache stampede (single thread populates on miss) |

### Cache Key Design

Keys should be unique and include all parameters that affect the result:

```java
// List with pagination
key = "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize"
// Result: "list:perfume:p0:s10"

// Single item by ID
key = "#id"
// Result: "550e8400-e29b-41d4-a716-446655440000"
```

CacheEvictionHelper API
-----------------------

### evictAfterCommit(String cacheName, Object key)

Evicts a specific entry after transaction commit.

```java
cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, productId);
```

### evictAllAfterCommit(String cacheName)

Clears all entries in a cache after transaction commit.

```java
cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
```

### evictAllAfterCommit(String... cacheNames)

Clears multiple caches after transaction commit.

```java
cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS, CacheNames.PRODUCT);
```

Eviction Guidelines
-------------------

| Operation | What to Evict |
| --- | --- |
| Create | List cache only (new item not in any detail cache) |
| Update | List cache + specific item cache |
| Delete | List cache + specific item cache |

Example for update:
```java
@Override
public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
    Category saved = categoryRepository.save(category);
    
    cacheEvictionHelper.evictAllAfterCommit(CacheNames.CATEGORIES);
    cacheEvictionHelper.evictAfterCommit(CacheNames.CATEGORY, id);
    
    return categoryMapper.toResponse(saved);
}
```

Redis Data Format
-----------------

Cache values are serialized as JSON with type information for polymorphic deserialization:

```json
{
  "@class": "com.per.common.response.PageResponse",
  "content": [
    ["com.per.product.dto.response.ProductResponse", {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Sauvage",
      "createdAt": "2024-01-15T10:30:00Z"
    }]
  ],
  "page": 0,
  "size": 10
}
```

Requirements for DTOs:
- Must have `@NoArgsConstructor` for Jackson deserialization
- `java.time.*` types supported via `JavaTimeModule`

Debugging and Monitoring
------------------------

### Enable Cache Logging

Add to `application.yml`:
```yaml
logging:
  level:
    org.springframework.cache: TRACE
    org.springframework.data.redis: DEBUG
```

### Redis CLI Commands

```bash
# Connect to Redis
docker exec -it redis redis-cli

# List all cache keys
KEYS *

# View specific cache entry
GET "products::list:all:p0:s10"

# Check TTL remaining
TTL "products::list:all:p0:s10"

# Clear all caches
FLUSHALL
```

### Verify Cache Behavior

1. Enable SQL logging (`spring.jpa.show-sql: true`)
2. Call `GET /products` → observe SQL query (cache miss)
3. Call `GET /products` again → no SQL query (cache hit)
4. Call `POST /products` → creates product
5. Call `GET /products` → observe SQL query (cache was evicted)

Testing
-------

Unit tests mock `CacheEvictionHelper` to verify eviction is called:

```java
@Mock private CacheEvictionHelper cacheEvictionHelper;

@Test
void shouldEvictCacheOnCreate() {
    productService.createProduct(request);
    
    verify(cacheEvictionHelper).evictAllAfterCommit(CacheNames.PRODUCTS, CacheNames.PRODUCT);
}
```

Run cache-related tests:
```bash
mvn -Dtest=ProductServiceImplTest,CategoryServiceImplTest,BrandServiceImplTest,MadeInServiceImplTest test
```

Troubleshooting
---------------

### SerializationException: Java 8 date/time not supported

**Cause:** DTO contains `Instant`/`LocalDateTime` without proper Jackson configuration.

**Solution:** `CacheConfig.redisObjectMapper()` registers `JavaTimeModule`. Ensure your ObjectMapper uses this configuration.

### Cannot construct instance (no default constructor)

**Cause:** DTO lacks no-arg constructor for Jackson deserialization.

**Solution:** Add `@NoArgsConstructor` to the DTO class.

### Unrecognized field "active"

**Cause:** Boolean field `isActive` with primitive type generates `isActive()` getter, which Jackson serializes as `"active"`.

**Solution:** Use `Boolean isActive` (wrapper type) instead of `boolean isActive`, which generates `getIsActive()`.

### Stale Data After Update

**Cause:** Cache eviction not registered or transaction rollback.

**Solution:**
1. Verify `cacheEvictionHelper.evict*AfterCommit()` is called in service method
2. Check transaction is active (`@Transactional` present)
3. Verify no exception causes rollback before commit

Extending the Module
--------------------

### Adding Cache to a New Service

1. Add cache name constants to `CacheNames.java`:
   ```java
   public static final String MY_ENTITIES = "myEntities";
   public static final String MY_ENTITY = "myEntity";
   ```

2. Configure TTL in `CacheConfig.cacheManager()`:
   ```java
   .withCacheConfiguration(CacheNames.MY_ENTITIES, createCacheConfig(MASTER_DATA_TTL))
   ```

3. Add `@Cacheable` to read methods:
   ```java
   @Cacheable(value = CacheNames.MY_ENTITIES, key = "'list:all'", sync = true)
   public List<MyEntity> getAll() { ... }
   ```

4. Add eviction to write methods:
   ```java
   cacheEvictionHelper.evictAllAfterCommit(CacheNames.MY_ENTITIES);
   ```

5. Add `@Mock CacheEvictionHelper` to unit tests.

### Distributed Cache Considerations

For multi-instance deployments:
- Redis already provides distributed caching
- All instances share the same cache
- Eviction on one instance clears for all

### Cache Warming

For critical data, consider pre-populating cache on startup:
```java
@EventListener(ApplicationReadyEvent.class)
public void warmCache() {
    categoryService.getCategories(null, PageRequest.of(0, 100));
    brandService.getBrands(null, PageRequest.of(0, 100));
}
```
