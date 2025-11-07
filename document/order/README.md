Order Module Overview
=====================

The order module captures every checkout snapshot so payments can be reconciled independently of the cart. It stores the shopper, shipping contact, monetary totals, and immutable line items (product + variant references with the price at the time of purchase).

Responsibilities
----------------

* Persist orders generated from checkout (currently via `CheckoutServiceImpl`).
* Keep a full snapshot of receiver information, totals, and each variant being purchased.
* Provide relational hooks for payments (`payment.order_id` FK) and inventory (each line item links back to `product`/`product_variant`).
* Serve as the single source of truth when reconciling PayOS webhooks or fulfilling shipments.

Database Mapping
----------------

* `order`
  * Columns mirror `com.per.order.entity.Order` (totals, receiver fields, status, timestamps).
  * `order_code` is a random 9-digit long generated via `OrderCodeGenerator`.
  * `status` uses `OrderStatus` enum (`PENDING_PAYMENT`, `PAID`, `FAILED`, `CANCELLED`).
* `order_item`
  * Mirrors `com.per.order.entity.OrderItem`.
  * Keeps foreign keys to `product` and `product_variant` so the back office can trace SKUs.
  * Stores `product_name`, `variant_sku`, `quantity`, and `unit_price` at checkout time.

Flow Summary
------------

1. **Checkout (Payment module)**
   * Cart items selected by the shopper are validated, stock is locked by decrementing variant inventory, and `Order` + `OrderItem` rows are inserted.
   * Order totals are recomputed from the cart snapshot and saved atomically with the items.
2. **Payment**
   * `Payment` references the `Order` via `order_id`.
   * PayOS returns update both the `Payment` and the associated `Order.status`.
3. **Fulfilment**
   * Future features can attach shipping or invoice data by adding tables keyed by `order.id`.

Maintenance Guidelines
----------------------

* Never mutate historical line item fields (name, price, quantity). If a correction is required, write a compensating record or create a new order.
* When adding new order-level fields (e.g., coupon, shipping method), update:
  1. `Order` entity + builder defaults.
  2. `V8__create_order_tables.sql` (and create a new migration for production environments).
  3. The checkout workflow that populates the entity.
* Use `OrderRepository` for transactional operations; avoid loading eager relationships unless necessary (items are LAZY by default).
* If you introduce APIs for retrieving orders, enforce authorization (user scopes) and paging to avoid returning large item lists at once.

Testing Tips
------------

* When unit testing checkout logic, mock `OrderRepository` and assert that `Order` snapshots include the expected totals.
* Integration tests should verify that stock decrements happen before order persistence to avoid overselling.

API Readiness
-------------

Currently there are no public REST endpoints for orders. When you add them:

* Namespace under `/api/v1/orders`.
* Prefer read-only projections (summary list, detail by code) that do not expose internal IDs.
* Use `OrderStatus` in responses so the front end can display payment state without querying the payment module.
