package com.per.media.service.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.media.config.CloudinaryProperties;
import com.per.media.dto.response.MediaUploadResponse;
import com.per.media.entity.MediaAsset;
import com.per.media.mapper.MediaMapper;
import com.per.media.repository.MediaAssetRepository;
import com.per.media.service.MediaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    public MediaUploadResponse uploadSingle(MultipartFile file) {
        MediaAsset storedAsset = uploadInternal(file);
        return MediaMapper.toUploadResponse(storedAsset);
    }

    @Override
    public List<MediaUploadResponse> uploadBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(
                    ApiErrorCode.MEDIA_FILE_REQUIRED, "At least one file is required");
        }

        List<MediaUploadResponse> responses = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            MediaAsset asset = uploadInternal(file);
            responses.add(MediaMapper.toUploadResponse(asset));
        }
        return responses;
    }

    private MediaAsset uploadInternal(MultipartFile file) {
        validateFile(file);
        String resourceType = resolveResourceType(file);

        Map<String, Object> options = buildUploadOptions(resourceType);
        Map<String, Object> uploadResult = executeUpload(file, options);

        MediaAsset asset = mapToEntity(uploadResult, file);
        return mediaAssetRepository.save(asset);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiErrorCode.MEDIA_FILE_REQUIRED);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(
                    ApiErrorCode.MEDIA_FILE_TOO_LARGE,
                    "File exceeds maximum size of 10MB: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null
                || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new ApiException(
                    ApiErrorCode.MEDIA_UNSUPPORTED_TYPE,
                    "Only image or video files are supported: " + file.getOriginalFilename());
        }
    }

    private String resolveResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ApiException(ApiErrorCode.MEDIA_UNSUPPORTED_TYPE);
        }
        if (contentType.startsWith("video/")) {
            return "video";
        }
        return "image";
    }

    private Map<String, Object> buildUploadOptions(String resourceType) {
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType);
        options.put("use_filename", true);
        options.put("unique_filename", true);
        options.put("overwrite", false);
        if (StringUtils.hasText(cloudinaryProperties.getFolder())) {
            options.put("folder", cloudinaryProperties.getFolder());
        }
        return options;
    }

    private Map<String, Object> executeUpload(
            MultipartFile file, Map<String, Object> uploadOptions) {
        try {
            return cloudinary.uploader().upload(file.getBytes(), new HashMap<>(uploadOptions));
        } catch (IOException ex) {
            throw new ApiException(
                    ApiErrorCode.MEDIA_UPLOAD_FAILED,
                    "Unable to upload file to Cloudinary: " + file.getOriginalFilename(),
                    ex);
        } catch (RuntimeException ex) {
            throw new ApiException(
                    ApiErrorCode.MEDIA_UPLOAD_FAILED,
                    "Cloudinary rejected file: " + file.getOriginalFilename(),
                    ex);
        }
    }

    private MediaAsset mapToEntity(Map<String, Object> result, MultipartFile file) {
        MediaAsset.MediaAssetBuilder builder =
                MediaAsset.builder()
                        .assetId((String) result.get("asset_id"))
                        .publicId((String) result.get("public_id"))
                        .folder(resolveFolder(result))
                        .resourceType((String) result.get("resource_type"))
                        .format((String) result.get("format"))
                        .mimeType(file.getContentType())
                        .url((String) result.get("url"))
                        .secureUrl((String) result.get("secure_url"))
                        .bytes(asLong(result.get("bytes")))
                        .width(asInteger(result.get("width")))
                        .height(asInteger(result.get("height")))
                        .duration(asDouble(result.get("duration")))
                        .originalFilename((String) result.get("original_filename"))
                        .etag((String) result.get("etag"))
                        .signature((String) result.get("signature"))
                        .version(stringValue(result.get("version")))
                        .cloudCreatedAt(parseInstant(result.get("created_at")));

        return builder.build();
    }

    private String resolveFolder(Map<String, Object> result) {
        Object folderFromResult = result.get("folder");
        if (folderFromResult instanceof String folder && StringUtils.hasText(folder)) {
            return folder;
        }
        return cloudinaryProperties.getFolder();
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            return Long.parseLong(str);
        }
        throw new ApiException(ApiErrorCode.MEDIA_UPLOAD_FAILED, "Missing file size metadata");
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            return Integer.parseInt(str);
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            return Double.parseDouble(str);
        }
        return null;
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private Instant parseInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            return Instant.parse(str);
        }
        return null;
    }
}
