Tổng Quan Module Payment
========================

Module này orchestrate checkout, tích hợp với PayOS, và track payment attempts, transactions, và gateway callbacks. Nó build trên cart và order modules: cart items được convert thành orders, PayOS payment link được generate, và webhooks update cả payment và order state.

Các Thành Phần Chính
--------------------

| Package | Mô tả |
| --- | --- |
| `com.per.payment.controller` | REST endpoints cho checkout, PayOS webhook, và return URL. |
| `com.per.payment.dto` | Request/response contracts (checkout request, checkout response, PayOS return response). |
| `com.per.payment.entity` | `Payment` và `PaymentTransaction` mapped đến Flyway V9 tables. |
| `com.per.payment.service` | `CheckoutService` (order creation + PayOS link) và `PayOsWebhookService`. |
| `com.per.payment.configuration` | `PayOsProperties` cho credentials/paths và `PayOsConfig` wiring official SDK. |
| `com.per.order` | Referenced bởi payment module để snapshot orders/line items. |

Workflow
--------

1. **Checkout (`POST /api/v1/payments/checkout`)**
   * Client gửi `CheckoutRequest` := optional `cartItemIds`, receiver name/phone/address, note.
   * `CheckoutServiceImpl` resolve authenticated user's cart qua `CartHelper`.
   * Selected cart items được validate (ownership và quantity) và corresponding product variants được fetch.
   * Stock được check và decrement; nếu bất kỳ variant nào thiếu inventory, call fail với `CART_ITEM_OUT_OF_STOCK`.
   * `Order` + `OrderItem` snapshot được persist sử dụng `OrderRepository` và `OrderCodeGenerator`.
   * Selected cart items được remove để giữ remainder intact.
   * `Payment` row được insert (status `PENDING`).
   * PayOS `PaymentData` được build (line items included) và `PayOS#createPaymentLink` được call.
   * Returned link metadata (payment link ID, checkout URL, expiration) được store trên `Payment`.
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
   * PayOS redirect shopper đến đây dù họ đã pay hay cancelled.
   * Query params chứa `orderCode`, `code`, `cancel`.
   * Controller return latest `orderStatus`/`paymentStatus` cho UI **và**:
     - Nếu `cancel=true` hoặc `code != "00"`, đánh dấu payment/order `CANCELLED` và restock qua `OrderInventoryService`.
     - Nếu `code="00"`, statuses vẫn untouched (webhook sẽ set chúng thành `PAID`).

3. **PayOS Webhook (`POST /api/v1/payments/payos/webhook`)**
   * Nhận PayOS webhook JSON; SDK verify signature.
   * `Payment` tương ứng được locate qua `orderCode`. Dựa vào `code` (PayOS success code `"00"`), payment status trở thành `PAID` hoặc `FAILED`.
   * Linked `Order.status` được update (`PAID`, `CANCELLED`, hoặc `FAILED`). Khi non-success, inventory được restore.
   * `PaymentTransaction` record được insert cho webhook (idempotent by reference).
   * PayOS đôi khi gửi "test pings" với dummy order codes; ta ignore chúng thay vì throw exceptions.

4. **Expiration Scheduler**
   * Chạy mỗi phút (`payment.expiration-check-interval`).
   * Tìm pending payments với `expired_at < now`, đánh dấu chúng `FAILED`, set order `FAILED`, và restore stock.

5. **Maintenance**
   * `PaymentStatus` enum bao gồm `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `EXPIRED`.
   * `PaymentTransactionStatus` track `SUCCEEDED`, `FAILED`, `PENDING`.
   * Khi thêm PayOS fields mới, update cả entity và `V9__create_payment_tables.sql` với Flyway migration mới.
   * Cho additional gateways hoặc flows, wrap mỗi gateway call trong service class riêng để `CheckoutServiceImpl` vẫn thin.

Configuration
-------------

`PayOsProperties` (loaded từ `application.yml` / `.env`):

| Property | Env Key | Mô tả |
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
  "cartItemIds": ["f65af584-...","b5cc..."],   // optional; omit để checkout entire cart
  "receiverName": "Nguyen Van A",
  "receiverPhone": "0988000222",
  "shippingAddress": "123 Nguyen Trai, Ha Noi",
  "note": "Giao giờ hành chính"
}
```
Response `data` là `CheckoutResponse` được mô tả trên (chứa `checkoutUrl` để redirect user).

### 2. PayOS Webhook (từ PayOS, test qua Postman nếu cần)
```
POST /api/v1/payments/payos/webhook
Content-Type: application/json
<Webhook payload từ PayOS sandbox>
```
Không có auth được add mặc định; nếu expose publicly, secure endpoint này (IP whitelist hoặc signature verification đã done bởi SDK).

### 3. PayOS Return
```
GET /payments/payos/return?orderCode=123456789&cancel=true
```
Response body chứa latest `orderStatus`/`paymentStatus`. Khi cancel/failure endpoint này cũng persist state và restock inventory.

Ghi Chú Vận Hành
----------------

* Luôn chạy Flyway migrations V8 (order tables) trước V9 (payment tables). Cho new environments chạy:
  ```
  ./mvnw flyway:migrate
  ```
* Nếu migrations bị thay đổi sau khi applied, sử dụng `flyway repair` một lần để realign checksums.
* Cho sandbox tests, cấu hình PayOS return/cancel URLs để point đến local tunnel (ngrok) hoặc staging host để webhook/return flow có thể validated end-to-end.
* Khi debug payment issues:
  1. Check `payment` table cho `status` và `checkout_url`.
  2. Inspect `payment_transaction` cho webhook idempotency.
  3. Verify `order` status mirror payment status; return/webhook/scheduler tất cả update cả hai sides.

Mở Rộng Module
--------------

* **Partial capture/refund**: thêm endpoints + service methods mới gọi PayOS APIs (`cancelPaymentLink`, v.v.) và update payment/order status accordingly.
* **Retry logic**: wrap PayOS SDK calls trong retryable service với exponential backoff (Spring Retry) để handle transient errors.
* **Admin dashboards**: thêm `/api/v1/payments` hoặc `/api/v1/orders` read endpoints để filter theo status/date.
* **Multi-gateway**: introduce strategy layer để different providers implement common interface; `CheckoutServiceImpl` sẽ delegate based on gateway selection.
