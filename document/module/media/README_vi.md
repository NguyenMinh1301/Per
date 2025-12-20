Tổng Quan Module Media
======================

Module này đóng gói tất cả media storage concerns cho platform. Nó cung cấp Cloudinary-backed persistence layer, HTTP endpoints để upload media files, và database model capturing Cloudinary metadata. Mục tiêu là abstract Cloudinary-specific logic khỏi các domains khác (ví dụ: product) để các features có thể đơn giản lưu `secureUrl` được trả về bởi module này.

Các Thành Phần Chính
--------------------

* `CloudinaryProperties` (`config/CloudinaryProperties.java`)
  Bind đến các `cloudinary.*` properties được expose qua `.env` (cloud name, API key/secret, optional folder). Validation trên required fields đảm bảo startup fail fast nếu credentials missing.

* `CloudinaryConfig` (`config/CloudinaryConfig.java`)
  Tạo singleton `Cloudinary` client được cấu hình với properties trên. Tất cả Cloudinary interactions đi qua bean này.

* `MediaAsset` (`entity/MediaAsset.java`)
  JPA entity mirror metadata mà Cloudinary trả về cho uploaded resource. Primary key là generated UUID; `publicId` là unique để protect chống duplicate persistence. Flyway migration `V2__create_media_assets.sql` tạo table và indices tương ứng.

* `MediaAssetRepository` (`repository/MediaAssetRepository.java`)
  Simple Spring Data repository cho persisting và querying media assets. Hiện expose `existsByPublicId` cho potential duplicate handling.

* `MediaUploadResponse` (`dto/response/MediaUploadResponse.java`)
  Outbound DTO trả về cho API consumers. Nó mirror persisted entity để clients có thể immediately work với URL, dimensions, bytes, v.v.

* `MediaMapper` (`mapper/MediaMapper.java`)
  Transform `MediaAsset` entities thành `MediaUploadResponse` DTOs. Centralising mapping giúp dễ adjust response ở một nơi.

* `MediaService` (`service/MediaService.java`)
  Contract cho media operations. Hiện expose `uploadSingle` và `uploadBatch` để cover single/multi file flows.

* `MediaServiceImpl` (`service/impl/MediaServiceImpl.java`)
  Implement upload logic:
  - Validate inputs (presence, size < 10 MB, content type image/video).
  - Resolve Cloudinary upload options (resource type, folder, filename behaviour).
  - Stream bytes đến Cloudinary và map response thành `MediaAsset`.
  - Save entity và return mapped DTO.
  - Throw `ApiException` với error codes (`MEDIA_FILE_REQUIRED`, `MEDIA_FILE_TOO_LARGE`, `MEDIA_UNSUPPORTED_TYPE`, `MEDIA_UPLOAD_FAILED`) để global exception handler có thể build consistent API responses.

* `MediaController` (`controller/MediaController.java`)
  Expose HTTP endpoints dưới `/api/v1/media` (xem API contracts bên dưới). Mỗi endpoint ủy quyền cho `MediaService` và wrap responses sử dụng `ApiResponse` envelope dùng chung và `ApiSuccessCode`.

API Contracts
-------------

Tất cả responses sử dụng cấu trúc chung:
```
{
  "success": true|false,
  "code": "MEDIA_UPLOAD_SUCCESS",
  "message": "Media uploaded successfully",
  "data": { ... dto payload ... },
  "timestamp": "2025-10-29T03:15:42.123Z"
}
```

Endpoints
~~~~~~~~~

### Upload single file
* **Method & Path:** `POST /api/v1/media/upload`
* **Consumes:** `multipart/form-data`
* **Part name:** `file`
* **Success code:** `MEDIA_UPLOAD_SUCCESS`
* **Payload (`data`):**
  - `id`: UUID của stored asset
  - `publicId`: Cloudinary public identifier
  - `secureUrl` / `url`: sử dụng `secureUrl` cho HTTPS delivery
  - `resourceType`: `image` hoặc `video`
  - `bytes`, `width`, `height`, `duration` (nếu có)
  - Additional metadata: `originalFilename`, `format`, `mimeType`, `etag`, `signature`, `version`, `cloudCreatedAt`, `createdAt`, `updatedAt`

### Upload multiple files
* **Method & Path:** `POST /api/v1/media/upload/batch`
* **Consumes:** `multipart/form-data`
* **Part name:** `files` (repeat cho mỗi file)
* **Success code:** `MEDIA_UPLOAD_BATCH_SUCCESS`
* **Payload (`data`):** array của cùng objects được trả về bởi single upload endpoint, theo upload order.

Error Handling
--------------

Validation failures result in `success=false` responses với `code` set thành relevant `ApiErrorCode`:
* `MEDIA_FILE_REQUIRED`: missing multipart part hoặc empty file.
* `MEDIA_FILE_TOO_LARGE`: file vượt quá 10 MB.
* `MEDIA_UNSUPPORTED_TYPE`: MIME type không bắt đầu với `image/` hoặc `video/`.
* `MEDIA_UPLOAD_FAILED`: Cloudinary threw error hoặc metadata incomplete trong quá trình persistence.

Mở Rộng Module
--------------

1. **Thêm metadata fields mới:**
   - Update `MediaAsset` với các columns mới.
   - Tạo Flyway migration để alter table `media_assets`.
   - Extend `MediaMapper` và `MediaUploadResponse` để expose data.

2. **Hỗ trợ delete operations:**
   - Thêm method vào `MediaService` (ví dụ: `deleteByPublicId`) gọi `cloudinary.uploader().destroy(...)`.
   - Remove entity qua `MediaAssetRepository` và return success response với `ApiSuccessCode` mới.
   - Đăng ký endpoint mới trong `MediaController`.

3. **Generate signed URLs hoặc transformations:**
   - Inject `Cloudinary` vào helper service mới gọi relevant Cloudinary APIs.
   - Wrap outputs trong DTOs và expose qua additional service/controller methods.
   - Update `ApiConstants.Media` với endpoint constants mới.

4. **Client-side direct uploads:**
   - Cung cấp endpoint để generate signed upload parameters (`Cloudinary#apiSignRequest`).
   - Consumers có thể upload trực tiếp đến Cloudinary và vẫn persist metadata bằng cách gọi separate endpoint với upload result.

Resilience Patterns
-------------------

Media endpoints được bảo vệ bởi cả rate limiting và circuit breaker patterns:

### Rate Limiting

| Instance | Endpoint | Mô tả |
| --- | --- | --- |
| `mediaSingle` | `/upload` | Giới hạn single file uploads |
| `mediaMultipart` | `/upload/batch` | Giới hạn batch uploads |

Rate limiters ngăn chặn abuse và bảo vệ server resources. Khi vượt quá, endpoints trả về `429 Too Many Requests`.

### Circuit Breaker

Circuit breaker `media` bảo vệ chống Cloudinary service failures:

* Khi Cloudinary unavailable, circuit opens sau threshold failures.
* Subsequent requests fail fast với `503 Service Unavailable` thay vì timeout.
* Sau wait period, test requests được cho phép để check recovery.

Xem [Rate Limiting](../../resilience-patterns/rate-limit/README_vi.md) và [Circuit Breaker](../../resilience-patterns/circuit-breaker/README_vi.md) documentation để biết chi tiết cấu hình.

Ghi Chú Phát Triển
------------------

* Tests/builds: `mvn -q -DskipTests compile` (Spotless phải pass).
* Database migrations chạy tự động khi startup qua Flyway.
* Đảm bảo Cloudinary secrets có trong `.env`; nếu không, application startup fails do `@Validated` properties.

Cấu trúc này nhằm giữ Cloudinary usage tập trung trong module `media`. Các domains khác nên phụ thuộc chỉ vào DTO (URLs/metadata) thay vì gọi Cloudinary trực tiếp. Để implement features mới, follow existing patterns: define contract trong `MediaService`, implement trong `MediaServiceImpl`, expose through controller, và đăng ký additional constants, success codes, hoặc error codes trong shared common packages.
