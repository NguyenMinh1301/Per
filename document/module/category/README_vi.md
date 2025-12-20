Tổng Quan Module Category
=========================

Module category quản lý product taxonomies (ví dụ: perfume families, collections). Nó mirror brand module về cấu trúc, cung cấp CRUD APIs với pagination và query filtering trong khi giữ category-specific validations được encapsulated.

Trách Nhiệm
-----------

* Create, update, list, và delete categories.
* Cung cấp pageable search với optional text filtering.
* Enforce category name uniqueness.
* Map giữa REST payloads và `Category` JPA entity.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | `CategoryController` expose `/api/v1/categories` endpoints. |
| `dto.request` | `CategoryCreateRequest`, `CategoryUpdateRequest`. |
| `dto.response` | `CategoryResponse` DTO trả về cho clients. |
| `entity` | `Category` entity (xem Flyway migration `V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper cho DTO ↔ entity transformations. |
| `repository` | `CategoryRepository` với name uniqueness helpers và JPQL `search`. |
| `service` | `CategoryService` contract và `CategoryServiceImpl` implementation. |

Tóm Tắt Request Flow
--------------------

1. Controller ủy quyền cho `CategoryService`.
2. Service resolve operations: load category, validate uniqueness, gọi mapper cho partial updates, persist qua `CategoryRepository`.
3. Mapper xử lý field-level ignores (IDs, timestamps) để giữ persistence an toàn.
4. Repository search match category name sử dụng case-insensitive `LIKE`.

API Contracts
-------------

### `GET /api/v1/categories`
* Optional `query` parameter (name match).
* Pageable với default `createdAt` desc.
* Success code: `CATEGORY_LIST_SUCCESS`.
* Trả về `PageResponse<CategoryResponse>`.

### `GET /api/v1/categories/{id}`
* Success code: `CATEGORY_FETCH_SUCCESS`.
* Trả về single `CategoryResponse`.

### `POST /api/v1/categories`
* Body: `CategoryCreateRequest` (name required, optional description fields, image metadata, active flag).
* Success code: `CATEGORY_CREATE_SUCCESS`.
* Errors: `CATEGORY_NAME_CONFLICT` khi name đã tồn tại.

### `PUT /api/v1/categories/{id}`
* Body: `CategoryUpdateRequest` (partial update).
* Success code: `CATEGORY_UPDATE_SUCCESS`.
* Unique name validation xảy ra nếu `name` được cung cấp.

### `DELETE /api/v1/categories/{id}`
* Delete category; success code `CATEGORY_DELETE_SUCCESS`.

Error Handling
--------------

* `CATEGORY_NOT_FOUND` – invalid ID.
* `CATEGORY_NAME_CONFLICT` – duplicate name.
* `VALIDATION_ERROR` – surfaced khi `@Valid` fail (handled globally).

Ghi Chú Testing
---------------

Focus vào `CategoryServiceImpl`:
* Assert search delegates based on query presence.
* Validate name conflict detection khi create/update.
* Verify partial updates preserve existing values khi fields bị omit.

Ví dụ chạy test:
```
mvn -Dtest=CategoryServiceImplTest test
```

Mở Rộng Module
--------------

* **Nested categories**: thêm parent/child relationship fields trong entity và adjust mapper/service logic.
* **Slugging/SEO**: populate `slug` field từ name trong service layer (đảm bảo uniqueness với dedicated repository method).
* **Localization**: tạo các DTO/entity fields bổ sung cho translated names, descriptions, và adapt responses accordingly.

Caching
-------

Redis caching được enable cho các read operations:

* `getCategories` và `getCategory` được annotate với `@Cacheable`.
* Cache names: `categories` (list), `category` (single item theo ID).
* TTL: 30 phút (master data).
* Write operations (create, update, delete) trigger post-commit cache eviction qua `CacheEvictionHelper`.

Xem [Tài liệu Module Cache](../../cache/README_vi.md) để biết chi tiết.
