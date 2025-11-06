Cart Module Overview
====================

This module encapsulates the shopping cart domain for the storefront. It provides REST endpoints for cart retrieval and cart item mutations, persists cart state in PostgreSQL, and reuses shared response/error contracts so the API stays uniform across modules.

Responsibilities
----------------

* Maintain a single active cart per authenticated user.
* Track cart line items (variant, quantity, pricing) and keep aggregate totals in sync.
* Enforce product/variant availability and stock constraints before accepting mutations.
* Expose idempotent HTTP endpoints for clients to fetch, add, update, remove, or clear cart items.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | REST controllers: `CartController` (read-only) and `CartItemController` (mutations). |
| `dto` | Request/response payloads (MapStruct-driven). |
| `entity` | JPA entities for `Cart` and `CartItem`. |
| `repository` | Spring Data repositories for cart aggregates and items. |
| `service` | Service contracts plus helper utilities (user resolution, cart recalculation). |
| `service.impl` | Concrete implementations for cart read and cart-item mutation workflows. |
| `helper` | `CartHelper` centralises current-user lookup, cart fetch/create, and totals recalculation. |
| `mapper` | `CartMapper` converts entities to outward-facing DTOs. |
| `enums` | Domain enumerations (`CartStatus`). |

Persistence Model
-----------------

* `Cart` (`entity/Cart.java`)  
  - UUID primary key, one-to-many with `CartItem`.  
  - Tracks aggregate fields (`totalItems`, `subtotalAmount`, `discountAmount`, `totalAmount`, `status`).  
  - Cascade and orphan removal are enabled so item edits propagate automatically.

* `CartItem` (`entity/CartItem.java`)  
  - UUID primary key, references `Cart`, `Product`, and `ProductVariant`.  
  - Stores `quantity`, `price`, `subTotalAmount` snapped at the time of mutation.

* Migration `V6__create_cart_tables.sql` creates the `cart` and `cart_item` tables, ensures a unique active cart per user, and enforces `(cart_id, variant_id)` uniqueness.

Request Flow Summary
--------------------

1. **Resolve Current User**  
   `CartHelper.requireCurrentUser()` inspects `SecurityContextHolder`, supports both `UserPrincipal` and username lookups via `UserRepository`, and throws `UNAUTHORIZED` when missing.

2. **Fetch/Create Active Cart**  
   `CartHelper.getOrCreateActiveCart(User)` loads the `ACTIVE` cart or creates one lazily when absent.

3. **Mutations** (`CartItemServiceImpl`)  
   - Validate variant existence and active status (`PRODUCT_VARIANT_NOT_FOUND`).  
   - Ensure parent product is active (`PRODUCT_NOT_FOUND`).  
   - Enforce stock availability via `assertStockAvailable` (`CART_ITEM_OUT_OF_STOCK`).  
   - Update or create `CartItem`, recompute item subtotal, and call `CartHelper.recalculateCart` to refresh cart aggregates.  
   - Persist via `CartRepository.save(cart)`; cascades handle item persistence.

4. **Read** (`CartServiceImpl`)  
   - Returns the active cart mapped to `CartResponse` through `CartMapper`.

API Contracts
-------------

All endpoints live under `/api/v1/cart` (see `ApiConstants.Cart`) and respond with the shared `ApiResponse` envelope.

### `GET /api/v1/cart`
* Controller: `CartController#getCart`.
* Success code: `CART_FETCH_SUCCESS`.
* Returns `CartResponse` with aggregated totals and an array of `CartItemResponse`.

### `POST /api/v1/cart/items`
* Controller: `CartItemController#addItem`.
* Request body: `CartItemCreateRequest { variantId, quantity }`.
* Validations: quantity ≥ 1, variant must exist/active, product active, requested quantity ≤ stock.
* Success code: `CART_ITEM_ADD_SUCCESS`.

### `PATCH /api/v1/cart/items/{itemId}`
* Controller: `CartItemController#updateItem`.
* Request body: `CartItemUpdateRequest { quantity }`.
* Enforces same validations as add; `itemId` must belong to current user.
* Success code: `CART_ITEM_UPDATE_SUCCESS`.

### `DELETE /api/v1/cart/items/{itemId}`
* Controller: `CartItemController#removeItem`.
* Removes the item, recalculates totals.
* Success code: `CART_ITEM_REMOVE_SUCCESS`.

### `DELETE /api/v1/cart/items`
* Controller: `CartItemController#clearCart`.
* Clears all items, resets aggregates to zero.
* Success code: `CART_CLEAR_SUCCESS`.

Error Codes
-----------

Defined in `ApiErrorCode`:

* `CART_NOT_FOUND` – reserved for future use where a cart lookup fails.
* `CART_ITEM_NOT_FOUND` – when an item ID is invalid or belongs to another user.
* `CART_ITEM_OUT_OF_STOCK` – quantity request exceeds variant stock.
* `PRODUCT_NOT_FOUND`, `PRODUCT_VARIANT_NOT_FOUND` – reused from product domain for inactive/missing items.
* Generic auth and validation errors are handled through the shared exception handler.

Testing
-------

Unit tests target the service layer:

* `CartServiceImplTest` verifies the active-cart retrieval flow (with helper interactions mocked).
* `CartItemServiceImplTest` covers add/update/remove/clear scenarios, stock validation, and unauthorized cases.

Execute module-specific tests via:

```
mvn -Dtest="CartItemServiceImplTest,CartServiceImplTest" test
```

Extending the Module
--------------------

* **Coupons / discounts**: introduce new fields on `Cart` and adjust `CartHelper.recalculateCart` to incorporate discount logic. Add validation services as needed.
* **Cart serialization for anonymous users**: extract current helper logic into an interface and provide an alternate implementation/builder for guest carts.
* **Order integration**: once checkout is introduced, add a service method to “lock” the cart (`CartStatus.CHECKOUT_LOCKED`) and expose it via a new controller endpoint.
* **Audit/History**: attach `@EntityListeners` or separate audit table to track cart mutations if required by the business.

Development Notes
-----------------

* Run `mvn spotless:apply` before committing to satisfy formatting checks.
* All new schema changes must be delivered through incremental Flyway migrations.
* Reuse `CartHelper` for any future services (e.g., order checkout) that need consistent cart resolution logic.
