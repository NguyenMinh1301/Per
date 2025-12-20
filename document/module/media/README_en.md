Media Module Overview
=====================

This module encapsulates all media storage concerns for the platform. It provides a Cloudinary-backed persistence layer, HTTP endpoints for uploading media files, and a database model capturing Cloudinary metadata. The goal is to abstract Cloudinary-specific logic away from other domains (e.g., product) so features can simply store the `secureUrl` returned by this module.

Key Components
--------------

* `CloudinaryProperties` (`config/CloudinaryProperties.java`)  
  Binds to the `cloudinary.*` properties exposed via `.env` (cloud name, API key/secret, optional folder). Validation on required fields ensures startup fails fast if credentials are missing.

* `CloudinaryConfig` (`config/CloudinaryConfig.java`)  
  Produces a singleton `Cloudinary` client configured with the properties above. All Cloudinary interactions go through this bean.

* `MediaAsset` (`entity/MediaAsset.java`)  
  JPA entity mirroring the metadata Cloudinary returns for an uploaded resource. Primary key is a generated UUID; `publicId` is unique to protect against duplicate persistence. The Flyway migration `V2__create_media_assets.sql` creates the corresponding table and indices.

* `MediaAssetRepository` (`repository/MediaAssetRepository.java`)  
  Simple Spring Data repository for persisting and querying media assets. Currently exposes `existsByPublicId` for potential duplicate handling.

* `MediaUploadResponse` (`dto/response/MediaUploadResponse.java`)  
  Outbound DTO returned to API consumers. It mirrors the persisted entity so clients can immediately work with the URL, dimensions, bytes, etc.

* `MediaMapper` (`mapper/MediaMapper.java`)  
  Transforms `MediaAsset` entities into `MediaUploadResponse` DTOs. Centralising mapping makes it easier to adjust the response in one place.

* `MediaService` (`service/MediaService.java`)  
  Contract for media operations. Currently exposes `uploadSingle` and `uploadBatch` to cover single/multi file flows.

* `MediaServiceImpl` (`service/impl/MediaServiceImpl.java`)  
  Implements upload logic:
  - Validates inputs (presence, size < 10 MB, content type image/video).
  - Resolves Cloudinary upload options (resource type, folder, filename behaviour).
  - Streams bytes to Cloudinary and maps the response into `MediaAsset`.
  - Saves the entity and returns the mapped DTO.
  - Throws `ApiException` with error codes (`MEDIA_FILE_REQUIRED`, `MEDIA_FILE_TOO_LARGE`, `MEDIA_UNSUPPORTED_TYPE`, `MEDIA_UPLOAD_FAILED`) so the global exception handler can build consistent API responses.

* `MediaController` (`controller/MediaController.java`)  
  Exposes HTTP endpoints under `/api/v1/media` (see API contracts below). Each endpoint delegates to `MediaService` and wraps responses using the shared `ApiResponse` envelope and `ApiSuccessCode`.

API Contracts
-------------

All responses use the common structure:
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
  - `id`: UUID of the stored asset
  - `publicId`: Cloudinary public identifier
  - `secureUrl` / `url`: use `secureUrl` for HTTPS delivery
  - `resourceType`: `image` or `video`
  - `bytes`, `width`, `height`, `duration` (if available)
  - Additional metadata: `originalFilename`, `format`, `mimeType`, `etag`, `signature`, `version`, `cloudCreatedAt`, `createdAt`, `updatedAt`

### Upload multiple files
* **Method & Path:** `POST /api/v1/media/upload/batch`
* **Consumes:** `multipart/form-data`
* **Part name:** `files` (repeat for each file)
* **Success code:** `MEDIA_UPLOAD_BATCH_SUCCESS`
* **Payload (`data`):** array of the same objects returned by the single upload endpoint, in upload order.

Error Handling
--------------

Validation failures result in `success=false` responses with `code` set to the relevant `ApiErrorCode`:
* `MEDIA_FILE_REQUIRED`: missing multipart part or empty file.
* `MEDIA_FILE_TOO_LARGE`: file exceeds 10 MB.
* `MEDIA_UNSUPPORTED_TYPE`: MIME type does not start with `image/` or `video/`.
* `MEDIA_UPLOAD_FAILED`: Cloudinary threw an error or metadata was incomplete during persistence.

Extending the Module
--------------------

1. **Add new metadata fields:**  
   - Update `MediaAsset` with the new columns.  
   - Create a Flyway migration to alter the `media_assets` table.  
   - Extend `MediaMapper` and `MediaUploadResponse` to expose the data.

2. **Support delete operations:**  
   - Add a method to `MediaService` (e.g., `deleteByPublicId`) that invokes `cloudinary.uploader().destroy(...)`.  
   - Remove the entity via `MediaAssetRepository` and return a success response with a new `ApiSuccessCode`.  
   - Register a new endpoint in `MediaController`.

3. **Generate signed URLs or transformations:**  
   - Inject `Cloudinary` into a new helper service that invokes the relevant Cloudinary APIs.  
   - Wrap outputs in DTOs and expose via additional service/controller methods.  
   - Update `ApiConstants.Media` with new endpoint constants.

4. **Client-side direct uploads:**  
   - Provide an endpoint to generate signed upload parameters (`Cloudinary#apiSignRequest`).  
   - Consumers can upload directly to Cloudinary and still persist metadata by calling a separate endpoint with the upload result.

Resilience Patterns
-------------------

Media endpoints are protected by both rate limiting and circuit breaker patterns:

### Rate Limiting

| Instance | Endpoint | Description |
| --- | --- | --- |
| `mediaSingle` | `/upload` | Limits single file uploads |
| `mediaMultipart` | `/upload/batch` | Limits batch uploads |

Rate limiters prevent abuse and protect server resources. When exceeded, endpoints return `429 Too Many Requests`.

### Circuit Breaker

The `media` circuit breaker protects against Cloudinary service failures:

* When Cloudinary is unavailable, the circuit opens after threshold failures.
* Subsequent requests fail fast with `503 Service Unavailable` instead of timing out.
* After a wait period, test requests are allowed to check recovery.

See [Rate Limiting](../../resilience-patterns/rate-limit/README.md) and [Circuit Breaker](../../resilience-patterns/circuit-breaker/README.md) documentation for configuration details.

Development Notes
-----------------

* Tests/builds: `mvn -q -DskipTests compile` (Spotless must pass).  
* Database migrations run automatically on startup via Flyway.  
* Ensure Cloudinary secrets are present in `.env`; otherwise, application startup fails due to `@Validated` properties.

This structure is intended to keep Cloudinary usage concentrated in the `media` module. Other domains should depend only on the DTO (URLs/metadata) rather than calling Cloudinary directly. To implement new features, follow the existing patterns: define contract in `MediaService`, implement in `MediaServiceImpl`, expose through the controller, and register any additional constants, success codes, or error codes in the shared common packages.
