Tổng Quan Module Made In
========================

Module made-in track fragrance origin metadata (country/region of manufacture). Nó chia sẻ cùng architectural pattern với brand và category modules, cung cấp CRUD, pagination, và validation tailored cho origin records.

Trách Nhiệm
-----------

* Duy trì `MadeIn` records (name, ISO code, region, description, imagery).
* Cung cấp pageable listing với optional text search.
* Enforce unique origin names.
* Cung cấp DTOs cho admin/back-office interfaces.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | `MadeInController` expose `/api/v1/made-in` endpoints. |
| `dto.request` | `MadeInCreateRequest`, `MadeInUpdateRequest`. |
| `dto.response` | `MadeInResponse`. |
| `entity` | `MadeIn` JPA entity backing table `made_id` (`V3__init_brand_category_product.sql`). |
| `mapper` | MapStruct mapper cho conversions. |
| `repository` | `MadeInRepository` với name uniqueness methods và `search`. |
| `service` | `MadeInService` contract và `MadeInServiceImpl` implementation. |

Tóm Tắt Request Flow
--------------------

* Controller ủy quyền cho `MadeInService`.
* Service thực hiện lookup, name uniqueness checks, mapping, và persistence.
* Repository `search` thực hiện case-insensitive matching trên `name`, `region`, và `isoCode`.
* Mapper đảm bảo created/updated entities ignore immutable fields (IDs, timestamps).

API Contracts
-------------

### `GET /api/v1/made-in`
* Optional `query` parameter.
* Pageable với default `createdAt` desc.
* Success code: `MADEIN_LIST_SUCCESS`.
* Trả về `PageResponse<MadeInResponse>`.

### `GET /api/v1/made-in/{id}`
* Success code: `MADEIN_FETCH_SUCCESS`.

### `POST /api/v1/made-in`
* Body: `MadeInCreateRequest` (name required, iso code/region optional, active flag).
* Success code: `MADEIN_CREATE_SUCCESS`.
* Errors: `MADEIN_NAME_CONFLICT` nếu duplicate.

### `PUT /api/v1/made-in/{id}`
* Body: `MadeInUpdateRequest` (partial update).
* Success code: `MADEIN_UPDATE_SUCCESS`.
* Unique name check triggered khi `name` được cung cấp.

### `DELETE /api/v1/made-in/{id}`
* Remove record; success code `MADEIN_DELETE_SUCCESS`.

Error Handling
--------------

* `MADEIN_NOT_FOUND` – missing ID.
* `MADEIN_NAME_CONFLICT` – duplicate origin name.
* Validation errors propagate qua global handler (ví dụ: missing required fields).

Ghi Chú Testing
---------------

Tests nên mock `MadeInRepository` và cover:
* Retrieval/search flows.
* Duplicate prevention khi create/update.
* Partial update path.

Ví dụ chạy:
```
mvn -Dtest=MadeInServiceImplTest test
```

Mở Rộng Module
--------------

* **Localized names**: thêm JSON/translation tables và extend DTOs accordingly.
* **Association counts**: cung cấp analytics về số products reference một origin (yêu cầu repository query/join).
* **Soft deletion**: leverage existing `isActive` flag để hide origins thay vì hard delete; adjust queries để filter theo active status.

Caching
-------

Redis caching được enable cho các read operations:

* `getMadeIns` và `getMadeIn` được annotate với `@Cacheable`.
* Cache names: `madeIns` (list), `madeIn` (single item theo ID).
* TTL: 30 phút (master data).
* Write operations (create, update, delete) trigger post-commit cache eviction qua `CacheEvictionHelper`.

Xem [Tài liệu Module Cache](../../cache/README_vi.md) để biết chi tiết.
