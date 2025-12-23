Brand Module Overview
=====================

The brand module manages fragrance houses/labels that own products in the catalogue. It exposes CRUD APIs, enforces uniqueness rules, and provides pageable brand search while keeping response contracts aligned with the rest of the platform.

Responsibilities
----------------

* Register, update, and remove brands.
* Expose read/search endpoints with pagination, sorting, and optional text filtering.
* Guard against duplicate brand names at both create and update time.
* Map database entities to lightweight DTOs consumed by the storefront or back-office UI.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | `BrandController` REST endpoints under `/api/v1/brands`. |
| `dto.request` | DTOs for create/update operations (`BrandCreateRequest`, `BrandUpdateRequest`). |
| `dto.response` | Outbound DTO (`BrandResponse`) returned to clients. |
| `entity` | JPA entity `Brand` mapped to the `brand` table (migration `V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper converting between DTOs and entities. |
| `repository` | `BrandRepository` with Spring Data helpers and a JPQL search query. |
| `service` | Service contract plus `BrandServiceImpl` containing validation/business logic. |

Request Flow Summary
--------------------

1. **Controller layer** receives HTTP requests and delegates to `BrandService`. All responses are wrapped with `ApiResponse` and `ApiSuccessCode` (e.g., `BRAND_CREATE_SUCCESS`).
2. **Service layer** performs validation (name uniqueness), calls the mapper to populate entities, persists through `BrandRepository`, and maps the result back to `BrandResponse`.
3. **Repository layer** offers:
   * `existsByNameIgnoreCase` / `existsByNameIgnoreCaseAndIdNot` for conflict detection.
   * `search(query, pageable)` – JPQL query that matches name or website URL.
4. **Mapper layer** uses MapStruct to translate DTOs while ignoring immutable columns (IDs, timestamps).

API Contracts
-------------

### `GET /api/v1/brands`
* Query parameter `query` filters by name/website URL (case-insensitive).
* Supports pageable parameters (`page`, `size`, `sort`) with default `createdAt` desc.
* Success code: `BRAND_LIST_SUCCESS`.
* Response: `PageResponse<BrandResponse>`.

### `GET /api/v1/brands/{id}`
* Fetch single brand by UUID.
* Success code: `BRAND_FETCH_SUCCESS`.

### `POST /api/v1/brands`
* Body: `BrandCreateRequest` (`name` required, optional description, URL, founding info, active flag).
* Success code: `BRAND_CREATE_SUCCESS`.
* Validations: rejects duplicate names (`ApiErrorCode.BRAND_NAME_CONFLICT`).

### `PUT /api/v1/brands/{id}`
* Body: `BrandUpdateRequest` (all fields optional, partial update).
* Success code: `BRAND_UPDATE_SUCCESS`.
* Validations: name changes respect uniqueness check.

### `DELETE /api/v1/brands/{id}`
* Deletes brand and returns `BRAND_DELETE_SUCCESS`.
* Throws `BRAND_NOT_FOUND` for unknown IDs.

Error Handling
--------------

* `BRAND_NAME_CONFLICT` – duplicate name on create/update.
* `BRAND_NOT_FOUND` – invalid brand ID.
* Generic validation errors bubble up via `GlobalExceptionHandler`.

Testing Notes
-------------

Unit tests should focus on `BrandServiceImpl` by mocking `BrandRepository` and verifying:
* Search logic delegates correctly depending on `query`.
* Duplicate detection surfaces `ApiException`.
* Updates respect partial updates (only provided fields set).

Execute with:
```
mvn -Dtest=BrandServiceImplTest test
```
*(Add test class if not already present).*

Extending the Module
--------------------

* **Brand assets**: Add image uploads by extending DTO/entity, updating mapper, and referencing the media module.
* **Brand-localised data**: Introduce additional entity columns and extend `BrandResponse`.
* **Soft delete**: Add an `isActive` toggle (already present) and filter in `BrandRepository.search/findAll` as needed.

Caching
-------

Redis caching is enabled for read operations:

* `getBrands` and `getBrand` are annotated with `@Cacheable`.
* Cache names: `brands` (list), `brand` (single item by ID).
* TTL: 30 minutes (master data).
* Write operations (create, update, delete) trigger post-commit cache eviction via `CacheEvictionHelper`.

See [Cache Module Documentation](../../cache/README.md) for details.

