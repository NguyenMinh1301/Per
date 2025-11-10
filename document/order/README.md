Order Module Overview
=====================

The order module snapshots everything that leaves the cart: customer, shipping info, economic totals, and the exact product/variant/quantity/price being purchased. Payments, fulfilment, and refunds all depend on the data frozen here.

Responsibilities
----------------

* Persist checkout snapshots (`Order` + `OrderItem`) before hitting the payment gateway.
* Store an immutable copy of the price, SKU, and quantity at checkout. Later adjustments should create compensating records (credit notes) rather than updating these rows.
* Provide hooks for:
  * `payment.order_id` – payments reconcile against orders.
  * Inventory rollback – if a payment fails, we know exactly which variants to restock.
  * Future fulfilment modules (shipping labels, invoices).

Schema Mapping
--------------

* **Order (`public."order"`)**
  * Mirrors `com.per.order.entity.Order`.
  * Fields: `order_code` (9-digit random), totals (`subtotal_amount`, `discount_amount`, `shipping_fee`, `grand_total`), receiver details, `currency_code`, and timestamps.
  * Status comes from `OrderStatus`:
    * `PENDING_PAYMENT` – just created; waiting for payment.
    * `PAID` – payment succeeded.
    * `CANCELLED` – user cancelled before paying (either via return URL or webhook).
    * `FAILED` – expired/no payment received and scheduler marked it as failed.
* **OrderItem (`public.order_item`)**
  * Mirrors `com.per.order.entity.OrderItem`.
  * Contains FK to `order`, `product`, and `product_variant`.
  * Stored columns include `product_name`, `variant_sku`, `quantity`, `unit_price`, `sub_total_amount` – all immutable once saved.

Lifecycle
---------

1. **Checkout** (`CheckoutServiceImpl`)
   * Validates cart items, checks stock, decrements `product_variant.stockQuantity`.
   * Creates an `Order` with `OrderItem` records and `PENDING_PAYMENT` status.
2. **Payment** (PayOS)
   * `Payment` references the order via `order_id`.
   * Webhook/return endpoint updates both payment and order status; on failure/cancel it also calls `OrderInventoryService.restoreStock`.
3. **Expiration** (`ExpiredPaymentScheduler`)
   * Runs every minute: finds `Payment` rows with `status=PENDING` and `expired_at < now`, marks order/payment as `FAILED`, and restores stock.
4. **Fulfilment (future)**
   * Build additional tables keyed by `order.id` when you introduce shipping, invoices, etc.

Maintenance Guidelines
----------------------

* Never mutate unit prices or quantities on existing `OrderItem` rows. Create adjustments/refunds separately.
* Any new columns require:
  1. Updating the entity builder defaults.
  2. Adding a Flyway migration (don’t edit V8 in production).
  3. Populating the value in checkout logic.
* Use `OrderInventoryService` whenever you need to restore stock; do not hand-roll variant updates outside it to avoid inconsistencies.
* When adding order APIs later, always filter by authenticated user (unless admin) and paginate results.

Testing
-------

* Unit tests can mock `OrderRepository` and `ProductVariantRepository` to verify that totals and inventory adjustments happen in turn.
* Integration tests should cover:
  * Checkout success (order → payOS link).
  * Webhook success (order state → `PAID`).
  * Cancel/expiry (order state → `CANCELLED`/`FAILED` and inventory restored).

API Surface
-----------

Orders currently don’t expose public REST endpoints. If you need them:

* Use `/api/v1/orders` (GET list, GET detail by `orderCode`).
* Always return the `OrderStatus` and `PaymentStatus` so clients don’t need to cross-check elsewhere.
* For admin dashboards, include filters (`status`, `date range`, `user`) and pagination.
