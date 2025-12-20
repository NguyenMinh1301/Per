Tổng Quan Module Cache
======================

Module cache cung cấp hạ tầng caching dựa trên Redis cho các thao tác đọc có lưu lượng cao. Module này triển khai **Cache-Aside pattern với Post-Commit Eviction** để đảm bảo tính nhất quán dữ liệu giữa cache và database, ngay cả trong điều kiện high concurrency.

Kiến Trúc
---------

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

Các Thành Phần Chính
--------------------

| File | Mục đích |
| --- | --- |
| `CacheNames.java` | Các hằng số tên cache tập trung (nguyên tắc DRY). |
| `CacheConfig.java` | Redis cache manager với TTL tùy chỉnh cho từng loại cache. |
| `CacheEvictionHelper.java` | Tiện ích eviction cache đảm bảo an toàn với transaction. |

Chiến Lược Cache
----------------

### Cache-Aside Pattern

Các thao tác đọc kiểm tra cache trước. Khi cache miss, dữ liệu được lấy từ database và lưu vào cache cho các lần đọc tiếp theo.

```java
@Cacheable(value = CacheNames.PRODUCTS, key = "'list:' + (#query ?: 'all')", sync = true)
public PageResponse<ProductResponse> getProducts(String query, Pageable pageable) {
    // Chỉ thực thi khi cache miss
    return productRepository.findAll(pageable);
}
```

### Post-Commit Eviction

Các thao tác ghi vô hiệu hóa cache **sau khi** database transaction commit. Điều này ngăn chặn race condition khi dữ liệu cũ có thể bị cache.

**Vấn đề với naive eviction:**
```
Thread A: Evict cache → [Thread B đọc DB cũ → cache giá trị cũ] → Commit DB
Kết quả: Cache chứa dữ liệu stale!
```

**Giải pháp với post-commit eviction:**
```
Thread A: Bắt đầu transaction → Sửa DB → Commit → Evict cache
Kết quả: Cache luôn phản ánh trạng thái committed mới nhất
```

Triển khai:
```java
@Override
public ProductResponse createProduct(ProductCreateRequest request) {
    Product saved = productRepository.save(product);
    
    // Eviction được đăng ký nhưng chỉ thực thi sau khi commit
    cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
    
    return productMapper.toResponse(saved);
}
```

Cấu Hình TTL
------------

Time-To-Live (TTL) định nghĩa thời gian dữ liệu cached có hiệu lực trước khi tự động hết hạn. TTL đóng vai trò như lưới an toàn - ngay cả khi eviction thất bại, dữ liệu stale vẫn hết hạn.

| Loại Cache | TTL | Lý do |
| --- | --- | --- |
| Product list/detail | 10 phút | Lưu lượng cao, cập nhật thường xuyên |
| Master data (Category, Brand, MadeIn) | 30 phút | Dữ liệu tham chiếu ít thay đổi |
| Mặc định | 15 phút | Fallback cho các cache chưa định nghĩa |

Cấu hình trong `CacheConfig.java`:
```java
private static final Duration PRODUCT_TTL = Duration.ofMinutes(10);
private static final Duration MASTER_DATA_TTL = Duration.ofMinutes(30);
```

Tham Chiếu Tên Cache
--------------------

Tất cả tên cache được định nghĩa trong `CacheNames.java`:

| Hằng số | Giá trị | Sử dụng bởi |
| --- | --- | --- |
| `PRODUCTS` | `"products"` | Truy vấn danh sách Product |
| `PRODUCT` | `"product"` | Product đơn theo ID |
| `CATEGORIES` | `"categories"` | Truy vấn danh sách Category |
| `CATEGORY` | `"category"` | Category đơn theo ID |
| `BRANDS` | `"brands"` | Truy vấn danh sách Brand |
| `BRAND` | `"brand"` | Brand đơn theo ID |
| `MADE_INS` | `"madeIns"` | Truy vấn danh sách MadeIn |
| `MADE_IN` | `"madeIn"` | MadeIn đơn theo ID |

Các Annotation Chính
--------------------

### @Cacheable

Áp dụng cho các method đọc. Spring kiểm tra cache trước khi thực thi method.

| Tham số | Mục đích |
| --- | --- |
| `value` | Tên cache từ `CacheNames` |
| `key` | Biểu thức SpEL cho cache key |
| `sync = true` | Ngăn cache stampede (một thread populate khi miss) |

### Thiết Kế Cache Key

Key nên là duy nhất và bao gồm tất cả tham số ảnh hưởng đến kết quả:

```java
// Danh sách với phân trang
key = "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize"
// Kết quả: "list:perfume:p0:s10"

// Item đơn theo ID
key = "#id"
// Kết quả: "550e8400-e29b-41d4-a716-446655440000"
```

API CacheEvictionHelper
-----------------------

### evictAfterCommit(String cacheName, Object key)

Evict một entry cụ thể sau khi transaction commit.

```java
cacheEvictionHelper.evictAfterCommit(CacheNames.PRODUCT, productId);
```

### evictAllAfterCommit(String cacheName)

Xóa tất cả entries trong một cache sau khi transaction commit.

```java
cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS);
```

### evictAllAfterCommit(String... cacheNames)

Xóa nhiều cache sau khi transaction commit.

```java
cacheEvictionHelper.evictAllAfterCommit(CacheNames.PRODUCTS, CacheNames.PRODUCT);
```

Hướng Dẫn Eviction
------------------

| Thao tác | Cần Evict |
| --- | --- |
| Create | Chỉ list cache (item mới chưa có trong detail cache) |
| Update | List cache + item cache cụ thể |
| Delete | List cache + item cache cụ thể |

Ví dụ cho update:
```java
@Override
public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
    Category saved = categoryRepository.save(category);
    
    cacheEvictionHelper.evictAllAfterCommit(CacheNames.CATEGORIES);
    cacheEvictionHelper.evictAfterCommit(CacheNames.CATEGORY, id);
    
    return categoryMapper.toResponse(saved);
}
```

Định Dạng Dữ Liệu Redis
-----------------------

Giá trị cache được serialize dưới dạng JSON với thông tin type cho polymorphic deserialization:

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

Yêu cầu cho DTOs:
- Phải có `@NoArgsConstructor` cho Jackson deserialization
- Các kiểu `java.time.*` được hỗ trợ qua `JavaTimeModule`

Debug và Monitoring
-------------------

### Bật Cache Logging

Thêm vào `application.yml`:
```yaml
logging:
  level:
    org.springframework.cache: TRACE
    org.springframework.data.redis: DEBUG
```

### Lệnh Redis CLI

```bash
# Kết nối Redis
docker exec -it redis redis-cli

# Liệt kê tất cả cache keys
KEYS *

# Xem cache entry cụ thể
GET "products::list:all:p0:s10"

# Kiểm tra TTL còn lại
TTL "products::list:all:p0:s10"

# Xóa tất cả caches
FLUSHALL
```

### Xác Minh Hành Vi Cache

1. Bật SQL logging (`spring.jpa.show-sql: true`)
2. Gọi `GET /products` → quan sát SQL query (cache miss)
3. Gọi `GET /products` lần nữa → không có SQL query (cache hit)
4. Gọi `POST /products` → tạo product
5. Gọi `GET /products` → quan sát SQL query (cache đã bị evict)

Testing
-------

Unit tests mock `CacheEvictionHelper` để xác minh eviction được gọi:

```java
@Mock private CacheEvictionHelper cacheEvictionHelper;

@Test
void shouldEvictCacheOnCreate() {
    productService.createProduct(request);
    
    verify(cacheEvictionHelper).evictAllAfterCommit(CacheNames.PRODUCTS, CacheNames.PRODUCT);
}
```

Chạy các tests liên quan đến cache:
```bash
mvn -Dtest=ProductServiceImplTest,CategoryServiceImplTest,BrandServiceImplTest,MadeInServiceImplTest test
```

Xử Lý Sự Cố
-----------

### SerializationException: Java 8 date/time not supported

**Nguyên nhân:** DTO chứa `Instant`/`LocalDateTime` mà không có cấu hình Jackson phù hợp.

**Giải pháp:** `CacheConfig.redisObjectMapper()` đăng ký `JavaTimeModule`. Đảm bảo ObjectMapper sử dụng cấu hình này.

### Cannot construct instance (no default constructor)

**Nguyên nhân:** DTO thiếu no-arg constructor cho Jackson deserialization.

**Giải pháp:** Thêm `@NoArgsConstructor` vào DTO class.

### Unrecognized field "active"

**Nguyên nhân:** Field `isActive` kiểu boolean primitive tạo getter `isActive()`, mà Jackson serialize thành `"active"`.

**Giải pháp:** Sử dụng `Boolean isActive` (wrapper type) thay vì `boolean isActive`, sẽ tạo getter `getIsActive()`.

### Dữ Liệu Stale Sau Update

**Nguyên nhân:** Cache eviction không được đăng ký hoặc transaction rollback.

**Giải pháp:**
1. Xác minh `cacheEvictionHelper.evict*AfterCommit()` được gọi trong service method
2. Kiểm tra transaction đang active (`@Transactional` có mặt)
3. Xác minh không có exception gây rollback trước khi commit

Mở Rộng Module
--------------

### Thêm Cache cho Service Mới

1. Thêm cache name constants vào `CacheNames.java`:
   ```java
   public static final String MY_ENTITIES = "myEntities";
   public static final String MY_ENTITY = "myEntity";
   ```

2. Cấu hình TTL trong `CacheConfig.cacheManager()`:
   ```java
   .withCacheConfiguration(CacheNames.MY_ENTITIES, createCacheConfig(MASTER_DATA_TTL))
   ```

3. Thêm `@Cacheable` vào các read methods:
   ```java
   @Cacheable(value = CacheNames.MY_ENTITIES, key = "'list:all'", sync = true)
   public List<MyEntity> getAll() { ... }
   ```

4. Thêm eviction vào các write methods:
   ```java
   cacheEvictionHelper.evictAllAfterCommit(CacheNames.MY_ENTITIES);
   ```

5. Thêm `@Mock CacheEvictionHelper` vào unit tests.

### Cân Nhắc Distributed Cache

Cho triển khai multi-instance:
- Redis đã cung cấp distributed caching
- Tất cả instances chia sẻ cùng cache
- Eviction trên một instance xóa cho tất cả

### Cache Warming

Cho dữ liệu quan trọng, cân nhắc pre-populate cache khi startup:
```java
@EventListener(ApplicationReadyEvent.class)
public void warmCache() {
    categoryService.getCategories(null, PageRequest.of(0, 100));
    brandService.getBrands(null, PageRequest.of(0, 100));
}
```
