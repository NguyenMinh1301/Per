Made In Module Overview
=======================

The made-in module tracks fragrance origin metadata (country/region of manufacture). It shares the same architectural pattern as brand and category modules, offering CRUD, pagination, and validation tailored to origin records.

Responsibilities
----------------

* Maintain `MadeIn` records (name, ISO code, region, description, imagery).
* Provide pageable listing with optional text search.
* Enforce unique origin names.
* Supply DTOs for admin/back-office interfaces.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | `MadeInController` exposing `/api/v1/made-in` endpoints. |
| `dto.request` | `MadeInCreateRequest`, `MadeInUpdateRequest`. |
| `dto.response` | `MadeInResponse`. |
| `entity` | `MadeIn` JPA entity backing the `made_id` table (`V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper for conversions. |
| `repository` | `MadeInRepository` with name uniqueness methods and `search`. |
| `service` | `MadeInService` contract and `MadeInServiceImpl` implementation. |

Request Flow Summary
--------------------

* Controller delegates to `MadeInService`.
* Service performs lookup, name uniqueness checks, mapping, and persistence.
* Repository `search` performs case-insensitive matching on `name`, `region`, and `isoCode`.
* Mapper ensures created/updated entities ignore immutable fields (IDs, timestamps).

API Contracts
-------------

### `GET /api/v1/made-in`
* Optional `query` parameter.
* Pageable with default `createdAt` desc.
* Success code: `MADEIN_LIST_SUCCESS`.
* Returns `PageResponse<MadeInResponse>`.

### `GET /api/v1/made-in/{id}`
* Success code: `MADEIN_FETCH_SUCCESS`.

### `POST /api/v1/made-in`
* Body: `MadeInCreateRequest` (name required, iso code/region optional, active flag).
* Success code: `MADEIN_CREATE_SUCCESS`.
* Errors: `MADEIN_NAME_CONFLICT` if duplicate.

### `PUT /api/v1/made-in/{id}`
* Body: `MadeInUpdateRequest` (partial update).
* Success code: `MADEIN_UPDATE_SUCCESS`.
* Unique name check triggered when `name` provided.

### `DELETE /api/v1/made-in/{id}`
* Removes record; success code `MADEIN_DELETE_SUCCESS`.

Error Handling
--------------

* `MADEIN_NOT_FOUND` – missing ID.
* `MADEIN_NAME_CONFLICT` – duplicate origin name.
* Validation errors propagate via the global handler (e.g., missing required fields).

Testing Notes
-------------

Tests should mock `MadeInRepository` and cover:
* Retrieval/search flows.
* Duplicate prevention on create/update.
* Partial update path.

Invocation example:
```
mvn -Dtest=MadeInServiceImplTest test
```

Extending the Module
--------------------

* **Localized names**: add JSON/translation tables and extend DTOs accordingly.
* **Association counts**: provide analytics for how many products reference an origin (requires repository query/join).
* **Soft deletion**: leverage existing `isActive` flag to hide origins instead of hard delete; adjust queries to filter by active status.
* **Caching**: if origins are rarely updated, enable caching on read endpoints to reduce DB load.
