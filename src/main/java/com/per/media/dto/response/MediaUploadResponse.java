package com.per.media.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaUploadResponse {

    private final UUID id;
    private final String assetId;
    private final String publicId;
    private final String folder;
    private final String resourceType;
    private final String format;
    private final String mimeType;
    private final String url;
    private final String secureUrl;
    private final long bytes;
    private final Integer width;
    private final Integer height;
    private final Double duration;
    private final String originalFilename;
    private final String etag;
    private final String signature;
    private final String version;
    private final Instant cloudCreatedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
