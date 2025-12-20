Category Module Overview
========================

The category module manages product taxonomies (e.g., perfume families, collections). It mirrors the brand module in structure, providing CRUD APIs with pagination and query filtering while keeping category-specific validations encapsulated.

Responsibilities
----------------

* Create, update, list, and delete categories.
* Provide pageable search with optional text filtering.
* Enforce category name uniqueness.
* Map between REST payloads and the `Category` JPA entity.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | `CategoryController` exposing `/api/v1/categories` endpoints. |
| `dto.request` | `CategoryCreateRequest`, `CategoryUpdateRequest`. |
| `dto.response` | `CategoryResponse` DTO returned to clients. |
| `entity` | `Category` entity (see Flyway migration `V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper for DTO ↔ entity transformations. |
| `repository` | `CategoryRepository` with name uniqueness helpers and JPQL `search`. |
| `service` | `CategoryService` contract and `CategoryServiceImpl` implementation. |

Request Flow Summary
--------------------

1. Controller delegates to `CategoryService`.
2. Service resolves operations: load category, validate uniqueness, call mapper for partial updates, persist via `CategoryRepository`.
3. Mapper handles field-level ignores (IDs, timestamps) to keep persistence safe.
4. Repository search matches category name using case-insensitive `LIKE`.

API Contracts
-------------

### `GET /api/v1/categories`
* Optional `query` parameter (name match).
* Pageable with default `createdAt` desc.
* Success code: `CATEGORY_LIST_SUCCESS`.
* Returns `PageResponse<CategoryResponse>`.

### `GET /api/v1/categories/{id}`
* Success code: `CATEGORY_FETCH_SUCCESS`.
* Returns a single `CategoryResponse`.

### `POST /api/v1/categories`
* Body: `CategoryCreateRequest` (name required, optional description fields, image metadata, active flag).
* Success code: `CATEGORY_CREATE_SUCCESS`.
* Errors: `CATEGORY_NAME_CONFLICT` when name already exists.

### `PUT /api/v1/categories/{id}`
* Body: `CategoryUpdateRequest` (partial update).
* Success code: `CATEGORY_UPDATE_SUCCESS`.
* Unique name validation occurs if `name` provided.

### `DELETE /api/v1/categories/{id}`
* Deletes category; success code `CATEGORY_DELETE_SUCCESS`.

Error Handling
--------------

* `CATEGORY_NOT_FOUND` – invalid ID.
* `CATEGORY_NAME_CONFLICT` – duplicate name.
* `VALIDATION_ERROR` – surfaced when `@Valid` fails (handled globally).

Testing Notes
-------------

Focus on `CategoryServiceImpl`:
* Assert search delegates based on query presence.
* Validate name conflict detection on create/update.
* Verify partial updates preserve existing values when fields omitted.

Example test invocation:
```
mvn -Dtest=CategoryServiceImplTest test
```

Extending the Module
--------------------

* **Nested categories**: add parent/child relationship fields in the entity and adjust mapper/service logic.
* **Slugging/SEO**: populate the `slug` field from the name in the service layer (ensure uniqueness with dedicated repository method).
* **Localization**: create additional DTO/entity fields for translated names, descriptions, and adapt responses accordingly.

Caching
-------

Redis caching is enabled for read operations:

* `getCategories` and `getCategory` are annotated with `@Cacheable`.
* Cache names: `categories` (list), `category` (single item by ID).
* TTL: 30 minutes (master data).
* Write operations (create, update, delete) trigger post-commit cache eviction via `CacheEvictionHelper`.

See [Cache Module Documentation](../../cache/README.md) for details.

