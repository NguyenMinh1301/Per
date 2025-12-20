Tổng Quan Module Cart
=====================

Module này đóng gói domain giỏ hàng cho storefront. Nó cung cấp REST endpoints cho cart retrieval và cart item mutations, persist cart state trong PostgreSQL, và tái sử dụng các response/error contracts dùng chung để API nhất quán across modules.

Trách Nhiệm
-----------

* Duy trì một active cart duy nhất cho mỗi authenticated user.
* Track cart line items (variant, quantity, pricing) và giữ aggregate totals đồng bộ.
* Enforce product/variant availability và stock constraints trước khi chấp nhận mutations.
* Expose idempotent HTTP endpoints cho clients để fetch, add, update, remove, hoặc clear cart items.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | REST controllers: `CartController` (read-only) và `CartItemController` (mutations). |
| `dto` | Request/response payloads (MapStruct-driven). |
| `entity` | JPA entities cho `Cart` và `CartItem`. |
| `repository` | Spring Data repositories cho cart aggregates và items. |
| `service` | Service contracts cộng với helper utilities (user resolution, cart recalculation). |
| `service.impl` | Concrete implementations cho cart read và cart-item mutation workflows. |
| `helper` | `CartHelper` tập trung current-user lookup, cart fetch/create, và totals recalculation. |
| `mapper` | `CartMapper` chuyển đổi entities thành outward-facing DTOs. |
| `enums` | Domain enumerations (`CartStatus`). |

Persistence Model
-----------------

* `Cart` (`entity/Cart.java`)
  - UUID primary key, one-to-many với `CartItem`.
  - Track các aggregate fields (`totalItems`, `subtotalAmount`, `discountAmount`, `totalAmount`, `status`).
  - Cascade và orphan removal được enable nên item edits propagate tự động.

* `CartItem` (`entity/CartItem.java`)
  - UUID primary key, references `Cart`, `Product`, và `ProductVariant`.
  - Lưu `quantity`, `price`, `subTotalAmount` snapped tại thời điểm mutation.

* Migration `V6__create_cart_tables.sql` tạo các tables `cart` và `cart_item`, đảm bảo unique active cart cho mỗi user, và enforce `(cart_id, variant_id)` uniqueness.

Tóm Tắt Request Flow
--------------------

1. **Resolve Current User**
   `CartHelper.requireCurrentUser()` inspect `SecurityContextHolder`, hỗ trợ cả `UserPrincipal` và username lookups qua `UserRepository`, và throw `UNAUTHORIZED` khi missing.

2. **Fetch/Create Active Cart**
   `CartHelper.getOrCreateActiveCart(User)` load `ACTIVE` cart hoặc tạo mới lazily khi không có.

3. **Mutations** (`CartItemServiceImpl`)
   - Validate variant existence và active status (`PRODUCT_VARIANT_NOT_FOUND`).
   - Đảm bảo parent product active (`PRODUCT_NOT_FOUND`).
   - Enforce stock availability qua `assertStockAvailable` (`CART_ITEM_OUT_OF_STOCK`).
   - Update hoặc create `CartItem`, recompute item subtotal, và gọi `CartHelper.recalculateCart` để refresh cart aggregates.
   - Persist qua `CartRepository.save(cart)`; cascades xử lý item persistence.

4. **Read** (`CartServiceImpl`)
   - Trả về active cart được map thành `CartResponse` qua `CartMapper`.

API Contracts
-------------

Tất cả endpoints nằm dưới `/api/v1/cart` (xem `ApiConstants.Cart`) và respond với `ApiResponse` envelope dùng chung.

### `GET /api/v1/cart`
* Controller: `CartController#getCart`.
* Success code: `CART_FETCH_SUCCESS`.
* Trả về `CartResponse` với aggregated totals và array of `CartItemResponse`.

### `POST /api/v1/cart/items`
* Controller: `CartItemController#addItem`.
* Request body: `CartItemCreateRequest { variantId, quantity }`.
* Validations: quantity >= 1, variant phải exist/active, product active, requested quantity <= stock.
* Success code: `CART_ITEM_ADD_SUCCESS`.

### `PATCH /api/v1/cart/items/{itemId}`
* Controller: `CartItemController#updateItem`.
* Request body: `CartItemUpdateRequest { quantity }`.
* Enforce same validations như add; `itemId` phải thuộc về current user.
* Success code: `CART_ITEM_UPDATE_SUCCESS`.

### `DELETE /api/v1/cart/items/{itemId}`
* Controller: `CartItemController#removeItem`.
* Remove item, recalculate totals.
* Success code: `CART_ITEM_REMOVE_SUCCESS`.

### `DELETE /api/v1/cart/items`
* Controller: `CartItemController#clearCart`.
* Clear tất cả items, reset aggregates về zero.
* Success code: `CART_CLEAR_SUCCESS`.

Error Codes
-----------

Được định nghĩa trong `ApiErrorCode`:

* `CART_NOT_FOUND` – reserved cho future use khi cart lookup fail.
* `CART_ITEM_NOT_FOUND` – khi item ID invalid hoặc thuộc về user khác.
* `CART_ITEM_OUT_OF_STOCK` – quantity request vượt quá variant stock.
* `PRODUCT_NOT_FOUND`, `PRODUCT_VARIANT_NOT_FOUND` – reused từ product domain cho inactive/missing items.
* Generic auth và validation errors được xử lý qua shared exception handler.

Testing
-------

Unit tests target service layer:

* `CartServiceImplTest` verify active-cart retrieval flow (với helper interactions mocked).
* `CartItemServiceImplTest` cover add/update/remove/clear scenarios, stock validation, và unauthorized cases.

Chạy module-specific tests qua:

```
mvn -Dtest="CartItemServiceImplTest,CartServiceImplTest" test
```

Mở Rộng Module
--------------

* **Coupons / discounts**: giới thiệu các fields mới trên `Cart` và adjust `CartHelper.recalculateCart` để incorporate discount logic. Thêm validation services khi cần.
* **Cart serialization cho anonymous users**: extract current helper logic thành interface và cung cấp alternate implementation/builder cho guest carts.
* **Order integration**: khi checkout được giới thiệu, thêm service method để "lock" cart (`CartStatus.CHECKOUT_LOCKED`) và expose qua controller endpoint mới.
* **Audit/History**: attach `@EntityListeners` hoặc separate audit table để track cart mutations nếu business yêu cầu.

Ghi Chú Phát Triển
------------------

* Chạy `mvn spotless:apply` trước khi commit để satisfy formatting checks.
* Tất cả schema changes mới phải được deliver qua incremental Flyway migrations.
* Tái sử dụng `CartHelper` cho bất kỳ future services nào (ví dụ: order checkout) cần cart resolution logic nhất quán.
