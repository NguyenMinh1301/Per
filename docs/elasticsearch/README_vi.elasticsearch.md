# Search Engine (Elasticsearch)

## 1. Tổng quan
**Elasticsearch** đóng vai trò là **Read Model** trong kiến trúc CQRS (Command Query Responsibility Segregation) của chúng tôi. Nó cho phép tìm kiếm full-text, lọc và tổng hợp hiệu suất cao mà PostgreSQL không thể xử lý hiệu quả.

## 2. Indexing Strategy

### Dual-Write Problem
Chúng tôi **không** ghi vào Elasticsearch trong cùng một transaction với PostgreSQL. Thay vào đó, chúng tôi sử dụng **Eventual Consistency** qua Kafka.
1.  **Write**: `ProductService` lưu vào Postgres (Transactional).
2.  **Publish**: `ProductService` xuất bản `ProductIndexEvent`.
3.  **Consume**: `ProductIndexConsumer` nhận event và cập nhật Elasticsearch.

### Re-indexing
Trong trường hợp dữ liệu bị lệch/hỏng (drift/corruption), chúng tôi cung cấp **Reindex API** (chỉ Admin) để:
1.  Duyệt qua tất cả các bản ghi trong PostgreSQL.
2.  Bulk upserts chúng vào Elasticsearch.

## 3. Querying
Chúng tôi sử dụng `ProductSearchRepository` của Spring Data Elasticsearch cho các truy vấn cơ bản và `ElasticsearchClient` cho các tổng hợp phức tạp.

### Analyzers
*   **Standard Analyzer**: Sử dụng cho khớp chính xác (IDs, Keys).
*   **Vietnamese Analyzer** (Tùy chọn): Có thể được cấu hình để tokenization văn bản tiếng Việt tốt hơn.

## 4. Document Structure
Chúng tôi làm phẳng (flatten) dữ liệu quan hệ thành một tài liệu JSON duy nhất để tăng tốc độ.
*   **Product Index**: Chứa tên `Brand`, tên `Category`, và nguồn gốc `MadeIn` lồng nhau để tránh các thao tác tương đương join tại thời điểm truy vấn.
