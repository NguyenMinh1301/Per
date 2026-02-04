# Resilience Patterns

## 1. Tổng quan
Trong một distributed modular monolith, lỗi trong một module hoặc dịch vụ bên ngoài không được phép lan rộng làm sập toàn bộ hệ thống. Chúng tôi áp dụng các resilience patterns tiêu chuẩn để đảm bảo sự ổn định.

## 2. Rate Limiting (`@RateLimiter`)

### Mục đích
Bảo vệ API khỏi lạm dụng và tấn công DDoS.
-   **Triển khai**: Resilience4j sử dụng Redis/In-memory.
-   **Phạm vi**: Áp dụng cho các endpoints công khai như `/auth/login` (để ngăn chặn brute force) và các API công khai chung.
-   **Cấu hình**: limit-refreshes mỗi 1 giây, tối đa 10 requests mỗi user (có thể tùy chỉnh trong `application.yml`).

## 3. Circuit Breaker (`@CircuitBreaker`)

### Mục đích
Fail fast khi một dịch vụ hạ nguồn gặp sự cố, ngăn ngừa cạn kiệt tài nguyên.
-   **Mục tiêu**:
    -   **PayOS**: Nếu cổng thanh toán hết thời gian chờ liên tục.
    -   **Cloudinary**: Nếu upload media thất bại liên tục.
    -   **AI Service (OpenAI/Qdrant)**: Nếu dịch vụ RAG ngừng hoạt động.

### Fallbacks
Khi một mạch (circuit) mở, chúng tôi cung cấp một **Fallback**:
-   **Search**: Nếu ES sập -> Fallback sang tìm kiếm DB cơ bản (truy vấn LIKE) hoặc trả về rỗng với cảnh báo "Service Degradation".
-   **AI Chat**: Nếu AI sập -> Trả về "Tôi hiện đang nghỉ ngơi, vui lòng duyệt danh mục thủ công."

## 4. Bulkhead
Cô lập các thread pools cho các tác vụ tiêu tốn tài nguyên khác nhau (ví dụ: Xử lý hình ảnh vs Gửi Email) để một pool đầy không chặn pool kia.
