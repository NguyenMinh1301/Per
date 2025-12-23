Tìm Kiếm Sản Phẩm với Elasticsearch
====================================

Ứng dụng sử dụng Elasticsearch cho tìm kiếm sản phẩm nâng cao với full-text search, fuzzy matching, và multi-field filtering.

Kiến Trúc
---------

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Kiến Trúc Tìm Kiếm                                   │
│                                                                             │
│  User ──► GET /products/search ──► ProductSearchService ──► Elasticsearch   │
│                                                                             │
│  ProductServiceImpl ──► Kafka(product-index-topic) ──► ProductIndexConsumer │
│       (create/update/delete)                              │                 │
│                                                           ▼                 │
│                                                    Elasticsearch            │
│                                                    (đồng bộ document)       │
└─────────────────────────────────────────────────────────────────────────────┘
```

Tính Năng Chính
---------------

| Tính năng | Mô tả |
| --- | --- |
| **Multi-field Search** | Tìm kiếm trong name, description, brand, category |
| **Fuzzy Matching** | Tìm kết quả dù có lỗi chính tả (vd: "savge" → "Sauvage") |
| **Relevance Scoring** | Kết quả sắp xếp theo độ liên quan với field boosting |
| **Filters** | Brand, category, gender, fragrance family, sillage, longevity, seasonality, occasion, khoảng giá |
| **Async Sync** | Dữ liệu đồng bộ qua Kafka cho eventual consistency |

Các Thành Phần Chính
--------------------

| File | Mục đích |
| --- | --- |
| `ProductDocument.java` | Elasticsearch document mapping |
| `ProductSearchRepository.java` | Spring Data ES repository |
| `ProductDocumentMapper.java` | Chuyển đổi Entity sang Document |
| `ProductSearchService.java` | Interface service tìm kiếm |
| `ProductSearchServiceImpl.java` | Triển khai tìm kiếm với fuzzy queries |
| `ProductIndexEvent.java` | Kafka event cho đồng bộ index |
| `ProductIndexConsumer.java` | Kafka consumer đồng bộ ES |

API Endpoints
-------------

### Tìm Kiếm Sản Phẩm

```
GET /products/search
```

**Tham số Query:**

| Tham số | Kiểu | Mô tả |
| --- | --- | --- |
| `query` | String | Full-text search (hỗ trợ fuzzy) |
| `brandId` | UUID | Lọc theo thương hiệu |
| `categoryId` | UUID | Lọc theo danh mục |
| `gender` | Enum | MALE, FEMALE, UNISEX |
| `fragranceFamily` | Enum | WOODY, FLORAL, ORIENTAL, v.v. |
| `sillage` | Enum | SOFT, LIGHT, MODERATE, STRONG, HEAVY |
| `longevity` | Enum | SHORT, MODERATE, LONG_LASTING, VERY_LONG_LASTING |
| `seasonality` | Enum | SPRING, SUMMER, FALL, WINTER, ALL_SEASONS |
| `occasion` | Enum | DAILY, EVENING, FORMAL, CASUAL, PARTY |
| `minPrice` | BigDecimal | Giá tối thiểu |
| `maxPrice` | BigDecimal | Giá tối đa |
| `isActive` | Boolean | Trạng thái active (mặc định: true) |
| `page` | Integer | Số trang (mặc định: 0) |
| `size` | Integer | Kích thước trang (mặc định: 20) |

**Ví dụ Request:**

```bash
# Tìm kiếm text đơn giản
curl "/products/search?query=dior"

# Tìm kiếm fuzzy (chịu lỗi chính tả)
curl "/products/search?query=savge"   # tìm được "Sauvage"

# Tìm kiếm kết hợp filters
curl "/products/search?query=eau&gender=MALE&minPrice=100000&maxPrice=2000000"

# Chỉ filter (không có text search)
curl "/products/search?brandId=xxx&fragranceFamily=WOODY"
```

**Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Dior Sauvage",
        "shortDescription": "Một composition tươi mới đột phá...",
        "brandName": "Dior",
        "categoryName": "Eau de Parfum",
        "gender": "MALE",
        "minPrice": 2500000,
        "maxPrice": 4500000,
        "imageUrl": "https://..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

### Reindex Tất Cả Sản Phẩm

```
POST /products/reindex
```

Thao tác admin để xây dựng lại Elasticsearch index từ PostgreSQL.

```bash
curl -X POST "/products/reindex"
```

Độ Liên Quan Tìm Kiếm
---------------------

Kết quả được xếp hạng theo điểm liên quan với field boosting:

| Field | Boost | Độ ưu tiên |
| --- | --- | --- |
| `name` | 3x | Cao nhất |
| `brandName` | 2x | Cao |
| `shortDescription` | 2x | Cao |
| `categoryName` | 1x | Bình thường |
| `description` | 1x | Bình thường |

**Fuzzy Matching:**

Tìm kiếm sử dụng `fuzziness: AUTO` cho phép:
- Từ 1-2 ký tự: phải khớp chính xác
- Từ 3-5 ký tự: cho phép 1 lỗi
- Từ 6+ ký tự: cho phép 2 lỗi

Ví dụ:
- `dior` → "Dior", "DIOR"
- `savge` → "Sauvage"
- `bluu` → "Bleu"

Đồng Bộ Dữ Liệu
---------------

Sản phẩm được đồng bộ sang Elasticsearch qua Kafka events:

### Luồng Event

```
ProductServiceImpl ──► KafkaTemplate.send() ──► [product-index-topic]
                                                        │
                                                        ▼
                                               ProductIndexConsumer
                                                        │
                                                        ▼
                                               ProductSearchRepository.save()
```

### Events

| Action | Khi nào | Kết quả |
| --- | --- | --- |
| `INDEX` | Sản phẩm được tạo/cập nhật | Document được index |
| `DELETE` | Sản phẩm bị xóa | Document bị xóa |

### Retry & DLQ

Index events sử dụng cùng pattern retry như email:
- 3 lần retry với exponential backoff (1s, 2s, 4s)
- Events thất bại vào `product-index-topic-dlt`

Cấu Hình
--------

### application.yml

```yaml
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_URI:http://localhost:9200}
```

### Biến Môi Trường

| Biến | Mặc định | Mô tả |
| --- | --- | --- |
| `ELASTICSEARCH_URI` | `http://localhost:9200` | URL Elasticsearch server |

Ghi Chú Phát Triển
------------------

### Phát Triển Local

Khởi động Elasticsearch qua Docker Compose:

```bash
docker compose up -d elasticsearch
```

### Initial Indexing

Sau khi khởi động ứng dụng, gọi reindex để populate ES:

```bash
curl -X POST http://localhost:8080/products/reindex
```

### Monitoring

Truy cập Kibana tại `http://localhost:5601` để:
- Xem product index
- Phân tích search queries
- Debug relevance scoring

### Testing Search

```bash
# Test fuzzy matching
curl "/products/search?query=savge"

# Test filters
curl "/products/search?gender=MALE&fragranceFamily=WOODY"

# Test combined
curl "/products/search?query=fresh&gender=UNISEX&minPrice=500000"
```

Mở Rộng Search
--------------

### Thêm Fields Có Thể Tìm Kiếm

1. Thêm field vào `ProductDocument.java`:
   ```java
   @Field(type = FieldType.Text, analyzer = "standard")
   private String newField;
   ```

2. Cập nhật `ProductDocumentMapper.java` để map field mới

3. Thêm field vào search query trong `ProductSearchServiceImpl.java`:
   ```java
   .fields("name^3", "newField^2", ...)
   ```

4. Reindex tất cả sản phẩm

### Thêm Filters Mới

1. Thêm field vào `ProductSearchRequest.java`

2. Thêm logic filter trong `ProductSearchServiceImpl.buildSearchQuery()`:
   ```java
   if (request.getNewFilter() != null) {
       boolQuery.filter(f -> f.term(t -> t.field("newField").value(...)));
   }
   ```
