# Domain Module: Quản Lý Tài Liệu Đa Phương Tiện (Media)

## 1. Tổng Quan

**Mô đun Media** cung cấp một sự trừu tượng hóa tập trung để xử lý các tài sản kỹ thuật số (hình ảnh, video). Nó tách biệt (decouple) các miền nghiệp vụ cốt lõi (Sản phẩm, Thương hiệu) khỏi các chi tiết triển khai cụ thể của nhà cung cấp lưu trữ bên dưới (**Cloudinary**).

---

## 2. Kiến Trúc

Mô đun hoạt động như một proxy lưu trữ chuyên dụng, xử lý luồng upload và lưu trữ metadata.

### 2.1 Mẫu Tích Hợp

```mermaid
graph LR
    Client -->|Multipart File| MediaService
    MediaService -->|Stream| Cloudinary[(Cloudinary)]
    Cloudinary -->|Metadata (URL/ID)| MediaService
    MediaService -->|Lưu trữ| DB[(PostgreSQL)]
    MediaService -->|Public ID/URL| Client
```

### 2.2 Mô Hình Thực Thể

Thực thể `MediaAsset` đóng vai trò là sổ đăng ký cục bộ cho các tài sản từ xa.

*   `id` (UUID): Tham chiếu nội bộ.
*   `publicId` (String): Định danh duy nhất của Cloudinary.
*   `secureUrl` (String): URL phân phối qua HTTPS.
*   `resourceType`: Loại tài sản (IMAGE/VIDEO).

---

## 3. Logic Nghiệp Vụ & Bất Biến

### 3.1 Quy Tắc Validate

1.  **Ràng Buộc Tệp Tin**:
    *   **Kích Thước**: Tối đa **10MB** mỗi tệp (`MEDIA_FILE_TOO_LARGE`).
    *   **Loại**: Phải khớp với MIME types `image/*` hoặc `video/*` (`MEDIA_UNSUPPORTED_TYPE`).
2.  **Ngăn Chặn Trùng Lặp**: Hiện tại được xử lý bởi chính sách chống trùng lặp của Cloudinary hoặc chính sách ghi đè đơn giản dựa trên cấu hình đặt tên.

### 3.2 Khả Năng Phục Hồi (Resilience)

Quy trình upload được bảo vệ bởi các mẫu **Circuit Breaker**.

*   **Instance**: `media`
*   **Hành Vi**: Nếu độ trễ API Cloudinary tăng đột biến hoặc trả về lỗi, mạch sẽ ngắt (open) để ngăn chặn cạn kiệt thread pool, báo lỗi nhanh với `SERVICE_UNAVAILABLE`.

---

## 4. Đặc Tả API

Tiền tố: `/api/v1/media`

### 4.1 Thao Tác Upload

#### Upload Đơn Lẻ
`POST /upload`
**Header**: `Content-Type: multipart/form-data`
**Body**: `file` (Binary)
**Response**: `MediaUploadResponse` chứa `secureUrl` có thể sử dụng được.

#### Upload Hàng Loạt
`POST /upload/batch`
**Body**: `files` (Mảng Binary)
**Response**: Danh sách `MediaUploadResponse`.

---

## 5. Tham Chiếu Triển Khai

### 5.1 Trừu Tượng Hóa Cloudinary

Tất cả logic cụ thể của nhà cung cấp được đóng gói trong `MediaServiceImpl`.

```java
@CircuitBreaker(name = "media")
public MediaUploadResponse uploadSingle(MultipartFile file) {
    // 1. Validate
    validateFile(file);
    
    // 2. Upload lên Cloudinary
    Map result = cloudinary.uploader().upload(file.getBytes(), options);
    
    // 3. Persist Metadata
    MediaAsset asset = mapToEntity(result);
    repository.save(asset);
    
    return mapper.toResponse(asset);
}
```

### 5.2 Cấu Hình

Được định nghĩa qua `CloudinaryProperties`.

| Thuộc Tính | Biến Môi Trường |
| :--- | :--- |
| `cloud-name` | `CLOUDINARY_CLOUD_NAME` |
| `api-key` | `CLOUDINARY_API_KEY` |
| `api-secret` | `CLOUDINARY_API_SECRET` |

---

## 6. Mở Rộng Tương Lai

*   **Biến Đổi (Transformations)**: Expose các tham số API để yêu cầu thay đổi kích thước/cắt ảnh trực tiếp thông qua cú pháp URL của Cloudinary.
*   **Direct Upload**: Tạo các tham số upload có chữ ký để cho phép frontend upload trực tiếp lên Cloudinary (tiết kiệm băng thông backend) và sau đó callback để lưu metadata.
