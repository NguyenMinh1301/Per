Tổng Quan Module Authentication
================================

Tài liệu này giải thích các chi tiết nội bộ của module authentication để các maintainers tương lai có thể hiểu và mở rộng codebase một cách tự tin.

1. Trách Nhiệm
--------------

- Cung cấp authentication dựa trên JWT (access và refresh tokens).
- Cung cấp API introspection token nhẹ chỉ báo cáo liệu access token có active hay không.
- Quản lý các luồng đăng ký, đăng nhập, đăng xuất, xác minh email, quên/reset mật khẩu.
- Lưu refresh tokens trong Redis và verification/reset tokens trong PostgreSQL.
- Gửi emails cho xác minh và reset mật khẩu qua SMTP (xử lý bất đồng bộ qua Kafka).
- Expose endpoint `/me` để lấy thông tin profile người dùng hiện tại.

2. Các Packages Chính
---------------------

| Package | Mô tả |
| --- | --- |
| `com.per.auth.controller` | REST endpoints cho các luồng auth và `/me`. |
| `com.per.auth.service` | Business logic chính cho auth, mail, và quản lý token. |
| `com.per.auth.security` | JWT utilities, filter chain, và tích hợp user details với Spring Security. |
| `com.per.auth.service.token` | Abstractions cho refresh tokens (Redis) và single-use tokens được backup bởi DB. |
| `com.per.common` | Response envelope, error handling, và constants dùng chung. |

3. Tóm Tắt Request Flow
-----------------------

1. **Login (`POST /api/v1/auth/login`)**

   - `AuthController.login` ủy quyền cho `AuthService.login`.
   - Credentials được xác thực qua `AuthenticationManager`.
   - Cặp JWT được tạo bởi `JwtService`; refresh token được lưu trong Redis.
   - Response: `AuthTokenResponse` chỉ expose các token fields (thông tin user lấy qua `/me`).

2. **Authenticated Requests**

   - `JwtAuthenticationFilter` kiểm tra `Authorization` header.
   - Access token hợp lệ populate `SecurityContextHolder` (stateless session).

3. **Refresh (`POST /api/v1/auth/refresh`)**

   - Validate refresh JWT và Redis entry qua `RefreshTokenService`.
   - Thu hồi refresh token đã cung cấp và phát hành cặp mới (`AuthTokenResponse`).

4. **Introspect (`POST /api/v1/auth/introspect`)**

   - Nhận `IntrospectRequest { "accessToken": "<token>" }`.
   - `AuthService.introspect` xác minh signature, token type, user status, và expiration.
   - Response data là `{ "introspect": true|false }` đơn giản, message giải thích kết quả.

5. **Logout (`POST /api/v1/auth/logout`)**

   - Access token xác định principal.
   - Refresh token được xóa khỏi Redis nếu có.

6. **Email Verification**

   - `UserTokenService.create` phát hành token type `EMAIL_VERIFICATION` (TTL 24h).
   - `AuthService` publish `EmailEvent` đến Kafka topic `email-topic`.
   - `EmailConsumer` lắng nghe topic và gọi `MailService` để gửi email qua SMTP.
   - `AuthService.verifyEmail` consume token, đánh dấu `emailVerified = true`.

7. **Forgot/Reset Password**

   - `forgotPassword`: tùy chọn tạo token `PASSWORD_RESET` (TTL 15m) và gửi email.
   - `GET /reset-password?token=` validate token cho client UI.
   - `resetPassword`: consume token và update password BCrypt.

8. **Current Profile (`GET /api/v1/auth/me`)**
   - `MeServiceImpl` load user theo username từ security context và map thành `MeResponse`.

4. Vòng Đời Token
-----------------

- **Access Token**: 15 phút, type `ACCESS`, chứa user ID, roles, và issuer.
- **Refresh Token**: 30 ngày, type `REFRESH`, JTI ngẫu nhiên, lưu trong Redis cho per-device revocation.
- **User Tokens**: UUID tokens được backup bởi DB dùng cho verification và password reset. Single-use với TTL được enforce bởi `UserTokenServiceImpl`.

5. Persistence và Migrations
----------------------------

- Flyway migration `V1__init_schema.sql` tạo các tables (`users`, `roles`, `user_roles`, `user_tokens`) và seed roles (`ADMIN`, `USER`).
- Các thay đổi schema mới phải được thêm qua các migration files bổ sung. Không chỉnh sửa migrations hiện có.

6. Checklist Cấu Hình
---------------------

Định nghĩa các environment variables sau (xem `.env.example`):

- `JWT_SECRET`, `ACCESS_TOKEN_TTL`, `REFRESH_TOKEN_TTL`, `JWT_ISSUER`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` (tùy chọn)
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`

7. Mở Rộng Module
-----------------

- **Thêm public endpoints mới**: cập nhật danh sách permit trong `SecurityConfig` nếu chúng cần bypass authentication.
- **Thêm admin-only endpoints**: annotate với `@PreAuthorize("hasRole('ADMIN')")` hoặc expression phù hợp.
- **Giới thiệu token types mới**: mở rộng enum `TokenType` và xử lý creation, validation, consumption sử dụng `UserTokenService`.
- **Custom response codes**: đăng ký trong `ApiSuccessCode`/`ApiErrorCode` để giữ contracts nhất quán.
- **Sửa đổi nội dung email**: cập nhật `MailServiceImpl`. Giữ links được kiểm soát bởi `ApplicationProperties`.

8. Rate Limiting
----------------

Các authentication endpoints được bảo vệ bởi Resilience4j rate limiters để ngăn chặn brute-force attacks:

| Instance | Endpoints | Mô tả |
| --- | --- | --- |
| `authStrict` | `/register`, `/login`, `/reset-password` | Các thao tác rủi ro cao |
| `authModerate` | `/refresh`, `/introspect` | Quản lý token |
| `authVeryStrict` | `/verify-email`, `/forgot-password` | Các thao tác nhạy cảm |

Khi vượt rate limits, endpoints trả về `429 Too Many Requests`. Giá trị cấu hình phụ thuộc vào môi trường và nên được tune dựa trên capacity của server.

Xem [Tài liệu Rate Limiting](../../resilience-patterns/rate-limit/README_vi.md) để biết chi tiết.

9. Khuyến Nghị Testing
----------------------

- Unit test `AuthServiceImpl`, `JwtService`, và `UserTokenServiceImpl` với mocks cho repositories và clock.
- Sử dụng Testcontainers cho integration tests để cover các luồng login → refresh → logout với PostgreSQL và Redis.
- Xác minh các luồng email và reset sử dụng SMTP server đã cấu hình (ví dụ: Gmail).

10. Ghi Chú Vận Hành
--------------------

- Redis key pattern: `auth:refresh:<token>` với TTL bằng thời gian refresh token.
- Spotless enforce formatting; chạy `./mvnw spotless:apply` trước khi commit.
- Build validation: `./mvnw clean verify` (bỏ qua tests qua `-DskipTests` trong quá trình iteration local).
