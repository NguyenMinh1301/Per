package com.per.made_in.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.per.common.cache.CacheEvictionHelper;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.made_in.dto.request.MadeInCreateRequest;
import com.per.made_in.dto.request.MadeInUpdateRequest;
import com.per.made_in.dto.response.MadeInResponse;
import com.per.made_in.entity.MadeIn;
import com.per.made_in.mapper.MadeInMapper;
import com.per.made_in.repository.MadeInRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MadeInService Unit Tests")
class MadeInServiceImplTest {

    @Mock private MadeInRepository madeInRepository;

    @Mock private MadeInMapper madeInMapper;

    @Mock private CacheEvictionHelper cacheEvictionHelper;

    @InjectMocks private MadeInServiceImpl madeInService;

    private MadeIn testMadeIn;
    private MadeInResponse madeInResponse;
    private UUID madeInId;

    @BeforeEach
    void setUp() {
        madeInId = UUID.randomUUID();
        testMadeIn =
                MadeIn.builder()
                        .id(madeInId)
                        .name("Test Country")
                        .isoCode("TC")
                        .region("Test Region")
                        .description("Test Description")
                        .isActive(true)
                        .build();

        madeInResponse =
                MadeInResponse.builder()
                        .id(madeInId)
                        .name("Test Country")
                        .isoCode("TC")
                        .region("Test Region")
                        .description("Test Description")
                        .isActive(true)
                        .build();
    }

    @Nested
    @DisplayName("Get MadeIns Tests")
    class GetMadeInsTests {

        @Test
        @DisplayName("Should return paginated madeIns when query is null")
        void shouldReturnPaginatedMadeInsWhenQueryIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<MadeIn> madeInPage = new PageImpl<>(java.util.List.of(testMadeIn));

            when(madeInRepository.findAll(pageable)).thenReturn(madeInPage);
            when(madeInMapper.toResponse(any(MadeIn.class))).thenReturn(madeInResponse);

            // When
            PageResponse<MadeInResponse> result = madeInService.getMadeIns(null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Country");

            verify(madeInRepository).findAll(pageable);
            verify(madeInRepository, never()).search(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return searched madeIns when query is provided")
        void shouldReturnSearchedMadeInsWhenQueryIsProvided() {
            // Given
            String query = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<MadeIn> madeInPage = new PageImpl<>(java.util.List.of(testMadeIn));

            when(madeInRepository.search(query, pageable)).thenReturn(madeInPage);
            when(madeInMapper.toResponse(any(MadeIn.class))).thenReturn(madeInResponse);

            // When
            PageResponse<MadeInResponse> result = madeInService.getMadeIns(query, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(madeInRepository).search(query, pageable);
        }
    }

    @Nested
    @DisplayName("Get MadeIn Tests")
    class GetMadeInTests {

        @Test
        @DisplayName("Should return madeIn by id")
        void shouldReturnMadeInById() {
            // Given
            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(madeInMapper.toResponse(testMadeIn)).thenReturn(madeInResponse);

            // When
            MadeInResponse result = madeInService.getMadeIn(madeInId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(madeInId);
            assertThat(result.getName()).isEqualTo("Test Country");

            verify(madeInRepository).findById(madeInId);
            verify(madeInMapper).toResponse(testMadeIn);
        }

        @Test
        @DisplayName("Should throw exception when madeIn not found")
        void shouldThrowExceptionWhenMadeInNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(madeInRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> madeInService.getMadeIn(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MADEIN_NOT_FOUND);

            verify(madeInRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Create MadeIn Tests")
    class CreateMadeInTests {

        @Test
        @DisplayName("Should create madeIn successfully")
        void shouldCreateMadeInSuccessfully() {
            // Given
            MadeInCreateRequest request =
                    MadeInCreateRequest.builder()
                            .name("New Country")
                            .isoCode("NC")
                            .region("New Region")
                            .description("New Description")
                            .isActive(true)
                            .build();

            MadeIn newMadeIn =
                    MadeIn.builder()
                            .id(UUID.randomUUID())
                            .name("New Country")
                            .isoCode("NC")
                            .region("New Region")
                            .description("New Description")
                            .isActive(true)
                            .build();

            MadeInResponse newMadeInResponse =
                    MadeInResponse.builder()
                            .id(newMadeIn.getId())
                            .name("New Country")
                            .isoCode("NC")
                            .region("New Region")
                            .description("New Description")
                            .isActive(true)
                            .build();

            when(madeInRepository.existsByNameIgnoreCase("New Country")).thenReturn(false);
            when(madeInMapper.toEntity(request)).thenReturn(newMadeIn);
            when(madeInRepository.save(any(MadeIn.class))).thenReturn(newMadeIn);
            when(madeInMapper.toResponse(newMadeIn)).thenReturn(newMadeInResponse);

            // When
            MadeInResponse result = madeInService.createMadeIn(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Country");
            assertThat(result.getIsActive()).isTrue();

            verify(madeInRepository).existsByNameIgnoreCase("New Country");
        }

        @Test
        @DisplayName("Should set isActive to true by default when not provided")
        void shouldSetIsActiveToTrueByDefaultWhenNotProvided() {
            // Given
            MadeInCreateRequest request =
                    MadeInCreateRequest.builder()
                            .name("New Country")
                            .isoCode("NC")
                            .isActive(null)
                            .build();

            MadeIn newMadeIn =
                    MadeIn.builder()
                            .id(UUID.randomUUID())
                            .name("New Country")
                            .isoCode("NC")
                            .isActive(true)
                            .build();

            MadeInResponse newMadeInResponse =
                    MadeInResponse.builder()
                            .id(newMadeIn.getId())
                            .name("New Country")
                            .isActive(true)
                            .build();

            when(madeInRepository.existsByNameIgnoreCase("New Country")).thenReturn(false);
            when(madeInMapper.toEntity(request)).thenReturn(newMadeIn);
            when(madeInRepository.save(any(MadeIn.class))).thenReturn(newMadeIn);
            when(madeInMapper.toResponse(newMadeIn)).thenReturn(newMadeInResponse);

            // When
            MadeInResponse result = madeInService.createMadeIn(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when madeIn name already exists")
        void shouldThrowExceptionWhenMadeInNameExists() {
            // Given
            MadeInCreateRequest request =
                    MadeInCreateRequest.builder().name("Existing Country").isoCode("EC").build();

            when(madeInRepository.existsByNameIgnoreCase("Existing Country")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> madeInService.createMadeIn(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MADEIN_NAME_CONFLICT);

            verify(madeInRepository, never()).save(any(MadeIn.class));
        }
    }

    @Nested
    @DisplayName("Update MadeIn Tests")
    class UpdateMadeInTests {

        @Test
        @DisplayName("Should update madeIn successfully")
        void shouldUpdateMadeInSuccessfully() {
            // Given
            MadeInUpdateRequest request =
                    MadeInUpdateRequest.builder()
                            .name("Updated Country")
                            .isoCode("UC")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            MadeIn updatedMadeIn =
                    MadeIn.builder()
                            .id(madeInId)
                            .name("Updated Country")
                            .isoCode("UC")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            MadeInResponse updatedMadeInResponse =
                    MadeInResponse.builder()
                            .id(madeInId)
                            .name("Updated Country")
                            .isoCode("UC")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(madeInRepository.existsByNameIgnoreCaseAndIdNot("Updated Country", madeInId))
                    .thenReturn(false);
            when(madeInRepository.save(any(MadeIn.class))).thenReturn(updatedMadeIn);
            when(madeInMapper.toResponse(updatedMadeIn)).thenReturn(updatedMadeInResponse);

            // When
            MadeInResponse result = madeInService.updateMadeIn(madeInId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Country");
            assertThat(result.getIsActive()).isFalse();

            verify(madeInRepository).findById(madeInId);
            verify(madeInRepository).save(any(MadeIn.class));
        }

        @Test
        @DisplayName("Should throw exception when updated name already exists")
        void shouldThrowExceptionWhenUpdatedNameExists() {
            // Given
            MadeInUpdateRequest request =
                    MadeInUpdateRequest.builder().name("Existing Country").build();

            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));
            when(madeInRepository.existsByNameIgnoreCaseAndIdNot("Existing Country", madeInId))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> madeInService.updateMadeIn(madeInId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MADEIN_NAME_CONFLICT);
        }

        @Test
        @DisplayName("Should throw exception when madeIn not found")
        void shouldThrowExceptionWhenMadeInNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            MadeInUpdateRequest request =
                    MadeInUpdateRequest.builder().name("Updated Country").build();

            when(madeInRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> madeInService.updateMadeIn(nonExistentId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MADEIN_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete MadeIn Tests")
    class DeleteMadeInTests {

        @Test
        @DisplayName("Should delete madeIn successfully")
        void shouldDeleteMadeInSuccessfully() {
            // Given
            when(madeInRepository.findById(madeInId)).thenReturn(Optional.of(testMadeIn));

            // When
            madeInService.deleteMadeIn(madeInId);

            // Then
            verify(madeInRepository).findById(madeInId);
            verify(madeInRepository).delete(testMadeIn);
        }

        @Test
        @DisplayName("Should throw exception when madeIn not found")
        void shouldThrowExceptionWhenMadeInNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(madeInRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> madeInService.deleteMadeIn(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.MADEIN_NOT_FOUND);

            verify(madeInRepository, never()).delete(any(MadeIn.class));
        }
    }
}
