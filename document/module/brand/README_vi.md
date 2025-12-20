Tổng Quan Module Brand
======================

Module brand quản lý các fragrance houses/labels sở hữu sản phẩm trong catalogue. Nó expose các CRUD APIs, enforce uniqueness rules, và cung cấp tìm kiếm brand có phân trang trong khi giữ response contracts aligned với phần còn lại của platform.

Trách Nhiệm
-----------

* Đăng ký, cập nhật, và xóa brands.
* Expose các read/search endpoints với pagination, sorting, và optional text filtering.
* Guard chống duplicate brand names tại thời điểm create và update.
* Map database entities thành lightweight DTOs được consume bởi storefront hoặc back-office UI.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | `BrandController` REST endpoints dưới `/api/v1/brands`. |
| `dto.request` | DTOs cho create/update operations (`BrandCreateRequest`, `BrandUpdateRequest`). |
| `dto.response` | Outbound DTO (`BrandResponse`) trả về cho clients. |
| `entity` | JPA entity `Brand` mapped đến table `brand` (migration `V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper chuyển đổi giữa DTOs và entities. |
| `repository` | `BrandRepository` với Spring Data helpers và JPQL search query. |
| `service` | Service contract cộng với `BrandServiceImpl` chứa validation/business logic. |

Tóm Tắt Request Flow
--------------------

1. **Controller layer** nhận HTTP requests và ủy quyền cho `BrandService`. Tất cả responses được wrap với `ApiResponse` và `ApiSuccessCode` (ví dụ: `BRAND_CREATE_SUCCESS`).
2. **Service layer** thực hiện validation (name uniqueness), gọi mapper để populate entities, persist qua `BrandRepository`, và map kết quả về `BrandResponse`.
3. **Repository layer** cung cấp:
   * `existsByNameIgnoreCase` / `existsByNameIgnoreCaseAndIdNot` cho conflict detection.
   * `search(query, pageable)` – JPQL query match name hoặc website URL.
4. **Mapper layer** sử dụng MapStruct để translate DTOs trong khi ignore immutable columns (IDs, timestamps).

API Contracts
-------------

### `GET /api/v1/brands`
* Query parameter `query` filter theo name/website URL (case-insensitive).
* Hỗ trợ pageable parameters (`page`, `size`, `sort`) với default `createdAt` desc.
* Success code: `BRAND_LIST_SUCCESS`.
* Response: `PageResponse<BrandResponse>`.

### `GET /api/v1/brands/{id}`
* Fetch single brand theo UUID.
* Success code: `BRAND_FETCH_SUCCESS`.

### `POST /api/v1/brands`
* Body: `BrandCreateRequest` (`name` required, optional description, URL, founding info, active flag).
* Success code: `BRAND_CREATE_SUCCESS`.
* Validations: reject duplicate names (`ApiErrorCode.BRAND_NAME_CONFLICT`).

### `PUT /api/v1/brands/{id}`
* Body: `BrandUpdateRequest` (tất cả fields optional, partial update).
* Success code: `BRAND_UPDATE_SUCCESS`.
* Validations: thay đổi name respect uniqueness check.

### `DELETE /api/v1/brands/{id}`
* Xóa brand và trả về `BRAND_DELETE_SUCCESS`.
* Throws `BRAND_NOT_FOUND` cho unknown IDs.

Error Handling
--------------

* `BRAND_NAME_CONFLICT` – duplicate name khi create/update.
* `BRAND_NOT_FOUND` – invalid brand ID.
* Generic validation errors bubble up qua `GlobalExceptionHandler`.

Ghi Chú Testing
---------------

Unit tests nên focus vào `BrandServiceImpl` bằng cách mock `BrandRepository` và verify:
* Search logic delegates chính xác dựa vào `query`.
* Duplicate detection surface `ApiException`.
* Updates respect partial updates (chỉ các fields được cung cấp mới được set).

Chạy với:
```
mvn -Dtest=BrandServiceImplTest test
```
*(Thêm test class nếu chưa có).*

Mở Rộng Module
--------------

* **Brand assets**: Thêm image uploads bằng cách extend DTO/entity, update mapper, và reference media module.
* **Brand-localised data**: Giới thiệu các entity columns bổ sung và extend `BrandResponse`.
* **Soft delete**: Thêm toggle `isActive` (đã có) và filter trong `BrandRepository.search/findAll` khi cần.

Caching
-------

Redis caching được enable cho các read operations:

* `getBrands` và `getBrand` được annotate với `@Cacheable`.
* Cache names: `brands` (list), `brand` (single item theo ID).
* TTL: 30 phút (master data).
* Write operations (create, update, delete) trigger post-commit cache eviction qua `CacheEvictionHelper`.

Xem [Tài liệu Module Cache](../../cache/README_vi.md) để biết chi tiết.
