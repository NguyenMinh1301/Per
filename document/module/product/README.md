Product Module Overview
=======================

The product module powers the merch catalogue for perfumes. It manages product master data, variant inventory/pricing, and exposes REST endpoints for admins to create, update, and curate the assortment. Products depend on related reference data (brand, category, made-in) and enforce business rules around uniqueness and stock configuration.

Responsibilities
----------------

* CRUD operations for products and their variants.
* Provide pageable search, filtered by product name.
* Maintain relationships to brand, category, and origin entities.
* Validate product name uniqueness and variant SKU uniqueness.
* Aggregate variant data into product detail responses.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | `ProductController` (`/api/v1/products`) and `ProductVariantController` (`/api/v1/products/{productId}/variants`). |
| `dto.request` | Create/update payloads for products and variants (supports bulk add/update/delete on update). |
| `dto.response` | `ProductResponse`, `ProductDetailResponse`, `ProductVariantResponse`. |
| `entity` | `Product` and `ProductVariant` entities declared in Flyway migration `V3__init_brand_category_product.sql`. |
| `enums` | Domain enumerations (fragrance family, gender, sillage, etc.). |
| `mapper` | MapStruct mappers converting between DTOs and entities. |
| `repository` | Spring Data repositories with additional lookup helpers (`findByNameContainingIgnoreCase`, `findByIdAndProductId`, SKU existence checks). |
| `service` | `ProductService` interface and `ProductServiceImpl` implementation containing orchestration logic. |

Request Flow Summary
--------------------

1. **Product Listing (`GET /products`)**
   * Controller delegates to `productService.getProducts`.
   * Service queries `ProductRepository` using optional search text.
   * Results mapped to `ProductResponse` and wrapped in `PageResponse`.

2. **Product Detail (`GET /products/{id}`)**
   * Service loads product via `findProduct`, fetches variants, and builds a `ProductDetailResponse` with variant DTOs.

3. **Create Product (`POST /products`)**
   * Validates brand/category/made-in references exist.
   * Asserts product name uniqueness.
   * Maps request to `Product` entity and persists.
   * Optionally persists initial variants (batch create with duplicate SKU guard).

4. **Update Product (`PUT /products/{id}`)**
   * Resolves optional association changes (brand/category/made-in).
   * Handles product field updates through mapper.
   * Applies variant mutations via `variantsToAdd`, `variantsToUpdate`, `variantsToDelete`.

5. **Delete Product (`DELETE /products/{id}`)**
   * Removes associated variants then deletes the product.

6. **Variant Endpoints (`/products/{productId}/variants`)**
   * Add/update/delete operations reuse validation helpers in `ProductServiceImpl` to ensure SKU uniqueness and correct product association.

Validations
-----------

* `PRODUCT_NAME_CONFLICT` – when a product name already exists (case-insensitive).
* `PRODUCT_VARIANT_SKU_CONFLICT` – duplicate SKU either in payload or persisted store.
* `PRODUCT_NOT_FOUND`, `PRODUCT_VARIANT_NOT_FOUND` – invalid references.
* `BRAND_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `MADEIN_NOT_FOUND` – missing dependencies.
* Default setters ensure variant currency (`VND`) and stock fields are non-null.

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

`ProductServiceImplTest` exercises the service layer with mocks:
* Retrieval & pagination logic.
* Create/update workflows, including association checks and variant mutations.
* Error handling for name/SKU conflicts and missing dependencies.

Run module-specific tests with:
```
mvn -Dtest=ProductServiceImplTest test
```

Extending the Module
--------------------

* **Inventory integration**: augment `ProductVariant` with stock reservation data or integrate with external inventory APIs.
* **Pricing tiers**: add multi-currency pricing or promotional fields; adjust DTOs/mappers accordingly.
* **Search facets**: expose more filters (brand, category, gender) by extending repository queries and controller parameters.
* **Media associations**: link variant images or galleries using the media module.
* **Soft delete**: respect `isActive` flags for both product and variants, filtering results while retaining history.

Caching
-------

Redis caching is enabled for read operations:

* `getProducts` and `getProduct` are annotated with `@Cacheable`.
* Cache names: `products` (list), `product` (single item by ID).
* TTL: 10 minutes (product data changes more frequently than master data).
* Write operations (create, update, delete) and variant modifications trigger post-commit cache eviction via `CacheEvictionHelper`.

See [Cache Module Documentation](../../cache/README.md) for details.

