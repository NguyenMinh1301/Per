package com.per.media.controller;

import java.util.List;

import com.per.common.base.BaseController;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.media.dto.response.MediaUploadResponse;
import com.per.media.service.MediaService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Media.ROOT)
@RequiredArgsConstructor
@Tag(name = "Media", description = "Media Upload APIs")
public class MediaController extends BaseController {

    private final MediaService mediaService;

    @PostMapping(value = ApiConstants.Media.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimiter(name = "mediaSingle", fallbackMethod = "fallback")
    @CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(
            @RequestPart("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.uploadSingle(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.MEDIA_UPLOAD_SUCCESS, response));
    }

    @PostMapping(
            value = ApiConstants.Media.UPLOAD_BATCH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimiter(name = "mediaMultipart", fallbackMethod = "fallback")
    @CircuitBreaker(name = "media", fallbackMethod = "circuitBreaker")
    public ResponseEntity<ApiResponse<List<MediaUploadResponse>>> uploadBatch(
            @RequestPart("files") List<MultipartFile> files) {
        List<MediaUploadResponse> responses = mediaService.uploadBatch(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.MEDIA_UPLOAD_BATCH_SUCCESS, responses));
    }
}
