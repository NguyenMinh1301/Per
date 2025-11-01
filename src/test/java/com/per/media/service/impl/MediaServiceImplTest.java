package com.per.media.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.media.config.CloudinaryProperties;
import com.per.media.dto.response.MediaUploadResponse;
import com.per.media.entity.MediaAsset;
import com.per.media.repository.MediaAssetRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaService Unit Tests")
class MediaServiceImplTest {

    @Mock private Cloudinary cloudinary;

    @Mock private CloudinaryProperties cloudinaryProperties;

    @Mock private MediaAssetRepository mediaAssetRepository;

    @Mock private Uploader uploader;

    @InjectMocks private MediaServiceImpl mediaService;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String TEST_FOLDER = "test-folder";

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        lenient().when(cloudinaryProperties.getFolder()).thenReturn(TEST_FOLDER);
    }

    @Nested
    @DisplayName("Upload Single Tests")
    class UploadSingleTests {

        @Test
        @DisplayName("Should upload image file successfully")
        void shouldUploadImageFileSuccessfully() throws IOException {
            // Given
            byte[] fileContent = "fake image content".getBytes();
            MultipartFile file =
                    new MockMultipartFile("image", "test-image.jpg", "image/jpeg", fileContent);

            Map<String, Object> uploadResult = createMockUploadResult("image", "test-image.jpg");

            MediaAsset savedAsset = createMediaAsset(uploadResult, file);

            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);
            when(mediaAssetRepository.save(any(MediaAsset.class))).thenReturn(savedAsset);

            // When
            MediaUploadResponse result = mediaService.uploadSingle(file);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isNotNull();
            assertThat(result.getSecureUrl()).isNotNull();

            verify(uploader).upload(any(byte[].class), anyMap());
            verify(mediaAssetRepository).save(any(MediaAsset.class));
        }

        @Test
        @DisplayName("Should upload video file successfully")
        void shouldUploadVideoFileSuccessfully() throws IOException {
            // Given
            byte[] fileContent = "fake video content".getBytes();
            MultipartFile file =
                    new MockMultipartFile("video", "test-video.mp4", "video/mp4", fileContent);

            Map<String, Object> uploadResult = createMockUploadResult("video", "test-video.mp4");

            MediaAsset savedAsset = createMediaAsset(uploadResult, file);

            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);
            when(mediaAssetRepository.save(any(MediaAsset.class))).thenReturn(savedAsset);

            // When
            MediaUploadResponse result = mediaService.uploadSingle(file);

            // Then
            assertThat(result).isNotNull();
            verify(uploader).upload(any(byte[].class), anyMap());
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() throws IOException {
            // When & Then
            assertThatThrownBy(() -> mediaService.uploadSingle(null))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_FILE_REQUIRED);

            verify(uploader, never()).upload(any(byte[].class), anyMap());
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            MultipartFile emptyFile =
                    new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[0]);

            // When & Then
            assertThatThrownBy(() -> mediaService.uploadSingle(emptyFile))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_FILE_REQUIRED);
        }

        @Test
        @DisplayName("Should throw exception when file size exceeds limit")
        void shouldThrowExceptionWhenFileSizeExceedsLimit() throws IOException {
            // Given
            byte[] largeContent = new byte[(int) (MAX_FILE_SIZE + 1)];
            MultipartFile largeFile =
                    new MockMultipartFile("image", "large-image.jpg", "image/jpeg", largeContent);

            // When & Then
            assertThatThrownBy(() -> mediaService.uploadSingle(largeFile))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_FILE_TOO_LARGE);

            verify(uploader, never()).upload(any(byte[].class), anyMap());
        }

        @Test
        @DisplayName("Should throw exception when file type is unsupported")
        void shouldThrowExceptionWhenFileTypeUnsupported() {
            // Given
            byte[] fileContent = "fake content".getBytes();
            MultipartFile file =
                    new MockMultipartFile("document", "test.pdf", "application/pdf", fileContent);

            // When & Then
            assertThatThrownBy(() -> mediaService.uploadSingle(file))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_UNSUPPORTED_TYPE);
        }

        @Test
        @DisplayName("Should throw exception when upload fails")
        void shouldThrowExceptionWhenUploadFails() throws IOException {
            // Given
            byte[] fileContent = "fake image content".getBytes();
            MultipartFile file =
                    new MockMultipartFile("image", "test-image.jpg", "image/jpeg", fileContent);

            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenThrow(new IOException("Upload failed"));

            // When & Then
            assertThatThrownBy(() -> mediaService.uploadSingle(file))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_UPLOAD_FAILED);

            verify(mediaAssetRepository, never()).save(any(MediaAsset.class));
        }
    }

    @Nested
    @DisplayName("Upload Batch Tests")
    class UploadBatchTests {

        @Test
        @DisplayName("Should upload multiple files successfully")
        void shouldUploadMultipleFilesSuccessfully() throws IOException {
            // Given
            List<MultipartFile> files = new ArrayList<>();
            files.add(
                    new MockMultipartFile(
                            "image1", "test1.jpg", "image/jpeg", "content1".getBytes()));
            files.add(
                    new MockMultipartFile(
                            "image2", "test2.jpg", "image/jpeg", "content2".getBytes()));

            Map<String, Object> uploadResult1 =
                    createMockUploadResult("image", "test1.jpg", "url1", "secureUrl1");
            Map<String, Object> uploadResult2 =
                    createMockUploadResult("image", "test2.jpg", "url2", "secureUrl2");

            MediaAsset asset1 = createMediaAsset(uploadResult1, files.get(0));
            MediaAsset asset2 = createMediaAsset(uploadResult2, files.get(1));

            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenReturn(uploadResult1)
                    .thenReturn(uploadResult2);
            when(mediaAssetRepository.save(any(MediaAsset.class)))
                    .thenReturn(asset1)
                    .thenReturn(asset2);

            // When
            List<MediaUploadResponse> results = mediaService.uploadBatch(files);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getUrl()).isEqualTo("url1");
            assertThat(results.get(1).getUrl()).isEqualTo("url2");

            verify(uploader, times(2)).upload(any(byte[].class), anyMap());
            verify(mediaAssetRepository, times(2)).save(any(MediaAsset.class));
        }

        @Test
        @DisplayName("Should throw exception when files list is null")
        void shouldThrowExceptionWhenFilesListIsNull() {
            // When & Then
            assertThatThrownBy(() -> mediaService.uploadBatch(null))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_FILE_REQUIRED);
        }

        @Test
        @DisplayName("Should throw exception when files list is empty")
        void shouldThrowExceptionWhenFilesListIsEmpty() {
            // Given
            List<MultipartFile> emptyFiles = new ArrayList<>();

            // When & Then
            assertThatThrownBy(() -> mediaService.uploadBatch(emptyFiles))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MEDIA_FILE_REQUIRED);
        }
    }

    private Map<String, Object> createMockUploadResult(String resourceType, String filename) {
        return createMockUploadResult(
                resourceType,
                filename,
                "http://example.com/" + filename,
                "https://example.com/" + filename);
    }

    private Map<String, Object> createMockUploadResult(
            String resourceType, String filename, String url, String secureUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("asset_id", UUID.randomUUID().toString());
        result.put("public_id", filename.replace(".", "_"));
        result.put("resource_type", resourceType);
        result.put("format", filename.substring(filename.lastIndexOf(".") + 1));
        result.put("url", url);
        result.put("secure_url", secureUrl);
        result.put("bytes", 1024L);
        result.put("width", 800);
        result.put("height", 600);
        result.put("original_filename", filename);
        result.put("etag", "etag123");
        result.put("signature", "signature123");
        result.put("version", "1");
        result.put("created_at", java.time.Instant.now().toString());
        if (TEST_FOLDER != null) {
            result.put("folder", TEST_FOLDER);
        }
        return result;
    }

    private MediaAsset createMediaAsset(Map<String, Object> uploadResult, MultipartFile file) {
        return MediaAsset.builder()
                .id(UUID.randomUUID())
                .assetId((String) uploadResult.get("asset_id"))
                .publicId((String) uploadResult.get("public_id"))
                .resourceType((String) uploadResult.get("resource_type"))
                .format((String) uploadResult.get("format"))
                .url((String) uploadResult.get("url"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .bytes((Long) uploadResult.get("bytes"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .originalFilename((String) uploadResult.get("original_filename"))
                .mimeType(file.getContentType())
                .build();
    }
}
