package com.per.media.mapper;

import com.per.media.dto.response.MediaUploadResponse;
import com.per.media.entity.MediaAsset;

public final class MediaMapper {

    private MediaMapper() {}

    public static MediaUploadResponse toUploadResponse(MediaAsset asset) {
        return MediaUploadResponse.builder()
                .id(asset.getId())
                .assetId(asset.getAssetId())
                .publicId(asset.getPublicId())
                .folder(asset.getFolder())
                .resourceType(asset.getResourceType())
                .format(asset.getFormat())
                .mimeType(asset.getMimeType())
                .url(asset.getUrl())
                .secureUrl(asset.getSecureUrl())
                .bytes(asset.getBytes())
                .width(asset.getWidth())
                .height(asset.getHeight())
                .duration(asset.getDuration())
                .originalFilename(asset.getOriginalFilename())
                .etag(asset.getEtag())
                .signature(asset.getSignature())
                .version(asset.getVersion())
                .cloudCreatedAt(asset.getCloudCreatedAt())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
