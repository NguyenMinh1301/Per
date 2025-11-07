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

2. **PayOS Return (`GET /api/v1/payments/payos/return?orderCode=...`)**
   * Used by the UI after PayOS redirects back.
   * Looks up the order and payment by `orderCode`, returning their latest statuses so the client can display success/failure messages.

3. **PayOS Webhook (`POST /api/v1/payments/payos/webhook`)**
   * Receives PayOS webhook JSON; the SDK verifies the signature.
   * The corresponding `Payment` is located via `orderCode`. Depending on `code` (PayOS success code `"00"`), the payment status becomes `PAID` or `FAILED`.
   * The linked `Order.status` is updated (`PAID`/`FAILED`).
   * A `PaymentTransaction` record is inserted for the webhook (idempotent by reference).

4. **Maintenance**
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
GET /api/v1/payments/payos/return?orderCode=123456789
```
Useful for the UI to display the latest `orderStatus` / `paymentStatus`.

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
  3. Verify `order` status equals payment status; the webhook handler updates both.

Extending the Module
--------------------

* **Partial capture/refund**: add new endpoints + service methods that call PayOS APIs (`cancelPaymentLink`, etc.) and update payment/order status accordingly.
* **Retry logic**: wrap PayOS SDK calls in a retryable service with exponential backoff (Spring Retry) to handle transient errors.
* **Admin dashboards**: add `/api/v1/payments` or `/api/v1/orders` read endpoints to filter by status/date.
* **Multi-gateway**: introduce a strategy layer so different providers implement a common interface; `CheckoutServiceImpl` would delegate based on gateway selection.
