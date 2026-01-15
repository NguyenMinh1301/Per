# Domain Module: Media Asset Management

## 1. Overview

The **Media Module** provides a centralized abstraction for handling digital assets (images, videos). It decouples the core domain (Products, Brands) from the specific implementation details of the underlying storage provider (**Cloudinary**).

---

## 2. Architecture

The module operates as a dedicated storage proxy, handling upload streaming and metadata persistence.

### 2.1 Integration Pattern

```mermaid
graph LR
    Client -->|Multipart File| MediaService
    MediaService -->|Stream| Cloudinary[(Cloudinary)]
    Cloudinary -->|Metadata (URL/ID)| MediaService
    MediaService -->|Persist| DB[(PostgreSQL)]
    MediaService -->|Public ID/URL| Client
```

### 2.2 Entity Model

The `MediaAsset` entity serves as a local registry of remote assets.

*   `id` (UUID): Internal reference.
*   `publicId` (String): Cloudinary's unique identifier.
*   `secureUrl` (String): HTTPS delivery URL.
*   `resourceType`: Asset type (IMAGE/VIDEO).

---

## 3. Business Logic & Invariants

### 3.1 Validation Rules

1.  **File Constraints**:
    *   **Size**: Max **10MB** per file (`MEDIA_FILE_TOO_LARGE`).
    *   **Type**: Must match `image/*` or `video/*` MIME types (`MEDIA_UNSUPPORTED_TYPE`).
2.  **Duplicate Prevention**: Currently handled by Cloudinary's underlying deduplication or simple overwrite policy based on naming configuration.

### 3.2 Resilience

The upload process is protected by **Circuit Breaker** patterns.

*   **Instance**: `media`
*   **Behavior**: If Cloudinary API latency spikes or returns errors, the circuit opens to prevent thread pool exhaustion, failing fast with `SERVICE_UNAVAILABLE`.

---

## 4. API Specification

Prefix: `/api/v1/media`

### 4.1 Upload Operations

#### Single Upload
`POST /upload`
**Header**: `Content-Type: multipart/form-data`
**Body**: `file` (Binary)
**Response**: `MediaUploadResponse` containing the usable `secureUrl`.

#### Batch Upload
`POST /upload/batch`
**Body**: `files` (Array of Binary)
**Response**: List of `MediaUploadResponse`.

---

## 5. Implementation Reference

### 5.1 Cloudinary Abstraction

All vendor-specific logic is encapsulated in `MediaServiceImpl`.

```java
@CircuitBreaker(name = "media")
public MediaUploadResponse uploadSingle(MultipartFile file) {
    // 1. Validate
    validateFile(file);
    
    // 2. Upload to Cloudinary
    Map result = cloudinary.uploader().upload(file.getBytes(), options);
    
    // 3. Persist Metadata
    MediaAsset asset = mapToEntity(result);
    repository.save(asset);
    
    return mapper.toResponse(asset);
}
```

### 5.2 Configuration

Defined via `CloudinaryProperties`.

| Property | Env Key |
| :--- | :--- |
| `cloud-name` | `CLOUDINARY_CLOUD_NAME` |
| `api-key` | `CLOUDINARY_API_KEY` |
| `api-secret` | `CLOUDINARY_API_SECRET` |

---

## 6. Future Extensions

*   **Transformations**: Expose API parameters to request on-the-fly resizing/cropping via Cloudinary URL transformation syntax.
*   **Direct Upload**: Generate signed upload parameters to allow the frontend to upload directly to Cloudinary (saving backend bandwidth) and then callback to save metadata.
