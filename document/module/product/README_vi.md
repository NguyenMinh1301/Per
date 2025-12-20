Tổng Quan Module Product
========================

Module product powers merch catalogue cho perfumes. Nó quản lý product master data, variant inventory/pricing, và expose REST endpoints cho admins để create, update, và curate assortment. Products phụ thuộc vào related reference data (brand, category, made-in) và enforce business rules xung quanh uniqueness và stock configuration.

Trách Nhiệm
-----------

* CRUD operations cho products và variants của chúng.
* Cung cấp pageable search, filtered theo product name.
* Duy trì relationships đến brand, category, và origin entities.
* Validate product name uniqueness và variant SKU uniqueness.
* Aggregate variant data vào product detail responses.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | `ProductController` (`/api/v1/products`) và `ProductVariantController` (`/api/v1/products/{productId}/variants`). |
| `dto.request` | Create/update payloads cho products và variants (hỗ trợ bulk add/update/delete on update). |
| `dto.response` | `ProductResponse`, `ProductDetailResponse`, `ProductVariantResponse`. |
| `entity` | `Product` và `ProductVariant` entities declared trong Flyway migration `V3__init_brand_category_product.sql`. |
| `enums` | Domain enumerations (fragrance family, gender, sillage, v.v.). |
| `mapper` | MapStruct mappers convert giữa DTOs và entities. |
| `repository` | Spring Data repositories với additional lookup helpers (`findByNameContainingIgnoreCase`, `findByIdAndProductId`, SKU existence checks). |
| `service` | `ProductService` interface và `ProductServiceImpl` implementation chứa orchestration logic. |

Tóm Tắt Request Flow
--------------------

1. **Product Listing (`GET /products`)**
   * Controller ủy quyền cho `productService.getProducts`.
   * Service query `ProductRepository` sử dụng optional search text.
   * Results mapped thành `ProductResponse` và wrapped trong `PageResponse`.

2. **Product Detail (`GET /products/{id}`)**
   * Service load product qua `findProduct`, fetch variants, và build `ProductDetailResponse` với variant DTOs.

3. **Create Product (`POST /products`)**
   * Validate brand/category/made-in references exist.
   * Assert product name uniqueness.
   * Map request thành `Product` entity và persist.
   * Optionally persist initial variants (batch create với duplicate SKU guard).

4. **Update Product (`PUT /products/{id}`)**
   * Resolve optional association changes (brand/category/made-in).
   * Handle product field updates qua mapper.
   * Apply variant mutations qua `variantsToAdd`, `variantsToUpdate`, `variantsToDelete`.

5. **Delete Product (`DELETE /products/{id}`)**
   * Remove associated variants rồi delete product.

6. **Variant Endpoints (`/products/{productId}/variants`)**
   * Add/update/delete operations reuse validation helpers trong `ProductServiceImpl` để ensure SKU uniqueness và correct product association.

Validations
-----------

* `PRODUCT_NAME_CONFLICT` – khi product name đã exists (case-insensitive).
* `PRODUCT_VARIANT_SKU_CONFLICT` – duplicate SKU hoặc trong payload hoặc persisted store.
* `PRODUCT_NOT_FOUND`, `PRODUCT_VARIANT_NOT_FOUND` – invalid references.
* `BRAND_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `MADEIN_NOT_FOUND` – missing dependencies.
* Default setters đảm bảo variant currency (`VND`) và stock fields là non-null.

API Contracts (Success Codes)
-----------------------------

| Endpoint | Success Code |
| --- | --- |
| `GET /api/v1/products` | `PRODUCT_LIST_SUCCESS` |
| `GET /api/v1/products/{id}` | `PRODUCT_FETCH_SUCCESS` |
| `POST /api/v1/products` | `PRODUCT_CREATE_SUCCESS` |
| `PUT /api/v1/products/{id}` | `PRODUCT_UPDATE_SUCCESS` |
| `DELETE /api/v1/products/{id}` | `PRODUCT_DELETE_SUCCESS` |
| `POST /api/v1/products/{productId}/variants` | `PRODUCT_VARIANT_CREATE_SUCCESS` |
| `PUT /api/v1/products/{productId}/variants/{variantId}` | `PRODUCT_VARIANT_UPDATE_SUCCESS` |
| `DELETE /api/v1/products/{productId}/variants/{variantId}` | `PRODUCT_VARIANT_DELETE_SUCCESS` |

Testing
-------

`ProductServiceImplTest` exercises service layer với mocks:
* Retrieval & pagination logic.
* Create/update workflows, bao gồm association checks và variant mutations.
* Error handling cho name/SKU conflicts và missing dependencies.

Chạy module-specific tests với:
```
mvn -Dtest=ProductServiceImplTest test
```

Mở Rộng Module
--------------

* **Inventory integration**: augment `ProductVariant` với stock reservation data hoặc integrate với external inventory APIs.
* **Pricing tiers**: thêm multi-currency pricing hoặc promotional fields; adjust DTOs/mappers accordingly.
* **Search facets**: expose thêm filters (brand, category, gender) bằng cách extend repository queries và controller parameters.
* **Media associations**: link variant images hoặc galleries sử dụng media module.
* **Soft delete**: respect `isActive` flags cho cả product và variants, filtering results trong khi retain history.

Caching
-------

Redis caching được enable cho các read operations:

* `getProducts` và `getProduct` được annotate với `@Cacheable`.
* Cache names: `products` (list), `product` (single item theo ID).
* TTL: 10 phút (product data thay đổi thường xuyên hơn master data).
* Write operations (create, update, delete) và variant modifications trigger post-commit cache eviction qua `CacheEvictionHelper`.

Xem [Tài liệu Module Cache](../../cache/README_vi.md) để biết chi tiết.
