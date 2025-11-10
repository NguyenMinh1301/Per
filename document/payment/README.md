Payment Module Overview
=======================

This module orchestrates checkout, integrates with PayOS, and keeps track of payment attempts, transactions, and gateway callbacks. It builds on the cart and order modules: cart items are converted into orders, a PayOS payment link is generated, and webhooks update both payment and order state.

Key Components
--------------

| Package | Description |
| --- | --- |
| `com.per.payment.controller` | REST endpoints for checkout, PayOS webhook, and return URL. |
| `com.per.payment.dto` | Request/response contracts (checkout request, checkout response, PayOS return response). |
| `com.per.payment.entity` | `Payment` and `PaymentTransaction` mapped to Flyway V9 tables. |
| `com.per.payment.service` | `CheckoutService` (order creation + PayOS link) and `PayOsWebhookService`. |
| `com.per.payment.configuration` | `PayOsProperties` for credentials/paths and `PayOsConfig` wiring the official SDK. |
| `com.per.order` | Referenced by the payment module to snapshot orders/line items. |

Workflow
--------

1. **Checkout (`POST /api/v1/payments/checkout`)**
   * Client sends `CheckoutRequest` := optional `cartItemIds`, receiver name/phone/address, note.
   * `CheckoutServiceImpl` resolves the authenticated user’s cart via `CartHelper`.
   * Selected cart items are validated (ownership and quantity) and corresponding product variants are fetched.
   * Stock is checked and decremented; if any variant lacks inventory, the call fails with `CART_ITEM_OUT_OF_STOCK`.
   * An `Order` + `OrderItem` snapshot is persisted using `OrderRepository` and `OrderCodeGenerator`.
   * Selected cart items are removed to keep the remainder intact.
   * A `Payment` row is inserted (status `PENDING`).
   * PayOS `PaymentData` is built (line items included) and `PayOS#createPaymentLink` is called.
   * Returned link metadata (payment link ID, checkout URL, expiration) is stored on the `Payment`.
   * Response payload:
     ```json
     {
       "orderId": "... UUID ...",
       "orderCode": 123456789,
       "orderStatus": "PENDING_PAYMENT",
       "paymentId": "... UUID ...",
       "paymentStatus": "PENDING",
       "paymentLinkId": "payos-link-id",
       "checkoutUrl": "https://pay.payos.vn/...",
       "amount": 123000.00
     }
     ```

2. **PayOS Return (`GET /payments/payos/return`)**
   * PayOS redirects the shopper here whether they paid or cancelled.
   * Query params contain `orderCode`, `code`, `cancel`.
   * Controller returns the latest `orderStatus`/`paymentStatus` to the UI **and**:
     - If `cancel=true` or `code != "00"`, mark payment/order `CANCELLED` and restock via `OrderInventoryService`.
     - If `code="00"`, statuses remain untouched (webhook will set them to `PAID`).

3. **PayOS Webhook (`POST /api/v1/payments/payos/webhook`)**
   * Receives PayOS webhook JSON; the SDK verifies the signature.
   * The corresponding `Payment` is located via `orderCode`. Depending on `code` (PayOS success code `"00"`), the payment status becomes `PAID` or `FAILED`.
   * The linked `Order.status` is updated (`PAID`, `CANCELLED`, or `FAILED`). On non-success, inventory is restored.
   * A `PaymentTransaction` record is inserted for the webhook (idempotent by reference).
   * PayOS sometimes sends “test pings” with dummy order codes; we now ignore those instead of throwing exceptions.

4. **Expiration Scheduler**
   * Runs every minute (`payment.expiration-check-interval`).
   * Finds pending payments with `expired_at < now`, marks them `FAILED`, sets order `FAILED`, and restores stock.

5. **Maintenance**
   * `PaymentStatus` enum includes `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `EXPIRED`.
   * `PaymentTransactionStatus` tracks `SUCCEEDED`, `FAILED`, `PENDING`.
   * When adding new PayOS fields, update both the entity and `V9__create_payment_tables.sql` with a new Flyway migration.
   * For additional gateways or flows, wrap each gateway call in its own service class so `CheckoutServiceImpl` remains thin.

Configuration
-------------

`PayOsProperties` (loaded from `application.yml` / `.env`):

| Property | Env Key | Description |
| --- | --- | --- |
| `payos.client-id` | `PAY_OS_CLIENT_ID` | PayOS client ID |
| `payos.api-key` | `PAY_OS_API_KEY` | API key |
| `payos.checksum-key` | `PAY_OS_CHECKSUM_KEY` | Webhook signature secret |
| `payos.webhook-path` | `PAY_OS_WEBHOOK_PATH` (default `/api/v1/payments/payos/webhook`) | Internal webhook URL |
| `payos.return-path` | `PAY_OS_RETURN_PATH` (default `/payments/payos/return`) | Relative return URL |
| `payos.cancel-path` | `PAY_OS_CANCEL_PATH` (default `/payments/payos/return`) | Relative cancel URL (same handler) |

API Reference (Postman Prep)
----------------------------

### 1. Create Checkout Link
```
POST /api/v1/payments/checkout
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "cartItemIds": ["f65af584-...","b5cc..."],   // optional; omit to checkout entire cart
  "receiverName": "Nguyen Van A",
  "receiverPhone": "0988000222",
  "shippingAddress": "123 Nguyen Trai, Ha Noi",
  "note": "Giao giờ hành chính"
}
```
Response `data` is the `CheckoutResponse` described above (contains `checkoutUrl` to redirect the user).

### 2. PayOS Webhook (from PayOS, test via Postman if needed)
```
POST /api/v1/payments/payos/webhook
Content-Type: application/json
<Webhook payload from PayOS sandbox>
```
No auth is added by default; if exposing publicly, secure this endpoint (IP whitelist or signature verification already done by SDK).

### 3. PayOS Return
```
GET /payments/payos/return?orderCode=123456789&cancel=true
```
Response body contains the latest `orderStatus`/`paymentStatus`. On cancel/failure this endpoint also persists the state and restocks inventory.

Operational Notes
-----------------

* Always run Flyway migrations V8 (order tables) before V9 (payment tables). For new environments run:
  ```
  ./mvnw flyway:migrate
  ```
* If migrations were changed after being applied, use `flyway repair` once to realign checksums.
* For sandbox tests, configure PayOS return/cancel URLs to point to your local tunnel (ngrok) or staging host so the webhook/return flow can be validated end-to-end.
* When debugging payment issues:
  1. Check `payment` table for `status` and `checkout_url`.
  2. Inspect `payment_transaction` for webhook idempotency.
  3. Verify `order` status mirrors payment status; return/webhook/scheduler all update both sides.

Extending the Module
--------------------

* **Partial capture/refund**: add new endpoints + service methods that call PayOS APIs (`cancelPaymentLink`, etc.) and update payment/order status accordingly.
* **Retry logic**: wrap PayOS SDK calls in a retryable service with exponential backoff (Spring Retry) to handle transient errors.
* **Admin dashboards**: add `/api/v1/payments` or `/api/v1/orders` read endpoints to filter by status/date.
* **Multi-gateway**: introduce a strategy layer so different providers implement a common interface; `CheckoutServiceImpl` would delegate based on gateway selection.
