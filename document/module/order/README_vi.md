Tổng Quan Module Order
======================

Module order snapshot mọi thứ rời khỏi cart: customer, shipping info, economic totals, và exact product/variant/quantity/price đang được mua. Payments, fulfilment, và refunds đều phụ thuộc vào data được frozen ở đây.

Trách Nhiệm
-----------

* Persist checkout snapshots (`Order` + `OrderItem`) trước khi hit payment gateway.
* Lưu immutable copy của price, SKU, và quantity tại checkout. Later adjustments nên tạo compensating records (credit notes) thay vì update các rows này.
* Cung cấp hooks cho:
  * `payment.order_id` – payments reconcile against orders.
  * Inventory rollback – nếu payment fail, chúng ta biết exactly variants nào cần restock.
  * Future fulfilment modules (shipping labels, invoices).

Schema Mapping
--------------

* **Order (`public."order"`)**
  * Mirror `com.per.order.entity.Order`.
  * Fields: `order_code` (9-digit random), totals (`subtotal_amount`, `discount_amount`, `shipping_fee`, `grand_total`), receiver details, `currency_code`, và timestamps.
  * Status đến từ `OrderStatus`:
    * `PENDING_PAYMENT` – vừa tạo; chờ payment.
    * `PAID` – payment thành công.
    * `CANCELLED` – user cancelled trước khi pay (qua return URL hoặc webhook).
    * `FAILED` – expired/không nhận được payment và scheduler đánh dấu failed.
* **OrderItem (`public.order_item`)**
  * Mirror `com.per.order.entity.OrderItem`.
  * Chứa FK đến `order`, `product`, và `product_variant`.
  * Stored columns bao gồm `product_name`, `variant_sku`, `quantity`, `unit_price`, `sub_total_amount` – tất cả immutable một khi saved.

Lifecycle
---------

1. **Checkout** (`CheckoutServiceImpl`)
   * Validate cart items, check stock, decrement `product_variant.stockQuantity`.
   * Tạo `Order` với `OrderItem` records và `PENDING_PAYMENT` status.
2. **Payment** (PayOS)
   * `Payment` reference order qua `order_id`.
   * Webhook/return endpoint update cả payment và order status; khi failure/cancel nó cũng gọi `OrderInventoryService.restoreStock`.
3. **Expiration** (`ExpiredPaymentScheduler`)
   * Chạy mỗi phút: tìm `Payment` rows với `status=PENDING` và `expired_at < now`, đánh dấu order/payment là `FAILED`, và restore stock.
4. **Fulfilment (future)**
   * Build additional tables keyed by `order.id` khi bạn introduce shipping, invoices, v.v.

Hướng Dẫn Maintenance
---------------------

* Không bao giờ mutate unit prices hoặc quantities trên existing `OrderItem` rows. Tạo adjustments/refunds riêng.
* Bất kỳ columns mới nào yêu cầu:
  1. Update entity builder defaults.
  2. Thêm Flyway migration (không edit V8 trong production).
  3. Populate value trong checkout logic.
* Sử dụng `OrderInventoryService` bất cứ khi nào bạn cần restore stock; không hand-roll variant updates ngoài nó để tránh inconsistencies.
* Khi thêm order APIs sau này, luôn filter theo authenticated user (trừ khi admin) và paginate results.

Testing
-------

* Unit tests có thể mock `OrderRepository` và `ProductVariantRepository` để verify totals và inventory adjustments xảy ra lần lượt.
* Integration tests nên cover:
  * Checkout success (order → payOS link).
  * Webhook success (order state → `PAID`).
  * Cancel/expiry (order state → `CANCELLED`/`FAILED` và inventory restored).

API Surface
-----------

Orders hiện không expose public REST endpoints. Nếu bạn cần:

* Sử dụng `/api/v1/orders` (GET list, GET detail theo `orderCode`).
* Luôn return `OrderStatus` và `PaymentStatus` để clients không cần cross-check ở nơi khác.
* Cho admin dashboards, include filters (`status`, `date range`, `user`) và pagination.
