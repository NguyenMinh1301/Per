package com.per.brand.service.impl;

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

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.request.BrandUpdateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.brand.entity.Brand;
import com.per.brand.mapper.BrandMapper;
import com.per.brand.repository.BrandRepository;
import com.per.common.cache.CacheEvictionHelper;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrandService Unit Tests")
class BrandServiceImplTest {

    @Mock private BrandRepository brandRepository;

    @Mock private BrandMapper brandMapper;

    @Mock private CacheEvictionHelper cacheEvictionHelper;

    @InjectMocks private BrandServiceImpl brandService;

    private Brand testBrand;
    private BrandResponse brandResponse;
    private UUID brandId;

    @BeforeEach
    void setUp() {
        brandId = UUID.randomUUID();
        testBrand =
                Brand.builder()
                        .id(brandId)
                        .name("Test Brand")
                        .description("Test Description")
                        .isActive(true)
                        .build();

        brandResponse =
                BrandResponse.builder()
                        .id(brandId)
                        .name("Test Brand")
                        .description("Test Description")
                        .isActive(true)
                        .build();
    }

    @Nested
    @DisplayName("Get Brands Tests")
    class GetBrandsTests {

        @Test
        @DisplayName("Should return paginated brands when query is null")
        void shouldReturnPaginatedBrandsWhenQueryIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Brand> brandPage = new PageImpl<>(java.util.List.of(testBrand));

            when(brandRepository.findAll(pageable)).thenReturn(brandPage);
            when(brandMapper.toResponse(any(Brand.class))).thenReturn(brandResponse);

            // When
            PageResponse<BrandResponse> result = brandService.getBrands(null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Brand");

            verify(brandRepository).findAll(pageable);
            verify(brandRepository, never()).search(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated brands when query is blank")
        void shouldReturnPaginatedBrandsWhenQueryIsBlank() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Brand> brandPage = new PageImpl<>(java.util.List.of(testBrand));

            when(brandRepository.findAll(pageable)).thenReturn(brandPage);
            when(brandMapper.toResponse(any(Brand.class))).thenReturn(brandResponse);

            // When
            PageResponse<BrandResponse> result = brandService.getBrands("   ", pageable);

            // Then
            assertThat(result).isNotNull();
            verify(brandRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return searched brands when query is provided")
        void shouldReturnSearchedBrandsWhenQueryIsProvided() {
            // Given
            String query = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Brand> brandPage = new PageImpl<>(java.util.List.of(testBrand));

            when(brandRepository.search(query, pageable)).thenReturn(brandPage);
            when(brandMapper.toResponse(any(Brand.class))).thenReturn(brandResponse);

            // When
            PageResponse<BrandResponse> result = brandService.getBrands(query, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(brandRepository).search(query, pageable);
            verify(brandRepository, never()).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Get Brand Tests")
    class GetBrandTests {

        @Test
        @DisplayName("Should return brand by id")
        void shouldReturnBrandById() {
            // Given
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(brandMapper.toResponse(testBrand)).thenReturn(brandResponse);

            // When
            BrandResponse result = brandService.getBrand(brandId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(brandId);
            assertThat(result.getName()).isEqualTo("Test Brand");

            verify(brandRepository).findById(brandId);
            verify(brandMapper).toResponse(testBrand);
        }

        @Test
        @DisplayName("Should throw exception when brand not found")
        void shouldThrowExceptionWhenBrandNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> brandService.getBrand(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.BRAND_NOT_FOUND);

            verify(brandRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Create Brand Tests")
    class CreateBrandTests {

        @Test
        @DisplayName("Should create brand successfully")
        void shouldCreateBrandSuccessfully() {
            // Given
            BrandCreateRequest request =
                    BrandCreateRequest.builder()
                            .name("New Brand")
                            .description("New Description")
                            .isActive(true)
                            .build();

            Brand newBrand =
                    Brand.builder()
                            .id(UUID.randomUUID())
                            .name("New Brand")
                            .description("New Description")
                            .isActive(true)
                            .build();

            BrandResponse newBrandResponse =
                    BrandResponse.builder()
                            .id(newBrand.getId())
                            .name("New Brand")
                            .description("New Description")
                            .isActive(true)
                            .build();

            when(brandRepository.existsByNameIgnoreCase("New Brand")).thenReturn(false);
            when(brandMapper.toEntity(request)).thenReturn(newBrand);
            when(brandRepository.save(any(Brand.class))).thenReturn(newBrand);
            when(brandMapper.toResponse(newBrand)).thenReturn(newBrandResponse);

            // When
            BrandResponse result = brandService.createBrand(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Brand");
            assertThat(result.getIsActive()).isTrue();

            verify(brandRepository).existsByNameIgnoreCase("New Brand");
            verify(brandRepository).save(any(Brand.class));
        }

        @Test
        @DisplayName("Should set isActive to true by default when not provided")
        void shouldSetIsActiveToTrueByDefaultWhenNotProvided() {
            // Given
            BrandCreateRequest request =
                    BrandCreateRequest.builder()
                            .name("New Brand")
                            .description("New Description")
                            .isActive(null)
                            .build();

            Brand newBrand =
                    Brand.builder()
                            .id(UUID.randomUUID())
                            .name("New Brand")
                            .description("New Description")
                            .isActive(true)
                            .build();

            BrandResponse newBrandResponse =
                    BrandResponse.builder()
                            .id(newBrand.getId())
                            .name("New Brand")
                            .isActive(true)
                            .build();

            when(brandRepository.existsByNameIgnoreCase("New Brand")).thenReturn(false);
            when(brandMapper.toEntity(request)).thenReturn(newBrand);
            when(brandRepository.save(any(Brand.class))).thenReturn(newBrand);
            when(brandMapper.toResponse(newBrand)).thenReturn(newBrandResponse);

            // When
            BrandResponse result = brandService.createBrand(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when brand name already exists")
        void shouldThrowExceptionWhenBrandNameExists() {
            // Given
            BrandCreateRequest request =
                    BrandCreateRequest.builder()
                            .name("Existing Brand")
                            .description("Description")
                            .build();

            when(brandRepository.existsByNameIgnoreCase("Existing Brand")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> brandService.createBrand(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.BRAND_NAME_CONFLICT);

            verify(brandRepository, never()).save(any(Brand.class));
        }
    }

    @Nested
    @DisplayName("Update Brand Tests")
    class UpdateBrandTests {

        @Test
        @DisplayName("Should update brand successfully")
        void shouldUpdateBrandSuccessfully() {
            // Given
            BrandUpdateRequest request =
                    BrandUpdateRequest.builder()
                            .name("Updated Brand")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            Brand updatedBrand =
                    Brand.builder()
                            .id(brandId)
                            .name("Updated Brand")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            BrandResponse updatedBrandResponse =
                    BrandResponse.builder()
                            .id(brandId)
                            .name("Updated Brand")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(brandRepository.existsByNameIgnoreCaseAndIdNot("Updated Brand", brandId))
                    .thenReturn(false);
            when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);
            when(brandMapper.toResponse(updatedBrand)).thenReturn(updatedBrandResponse);

            // When
            BrandResponse result = brandService.updateBrand(brandId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Brand");
            assertThat(result.getIsActive()).isFalse();

            verify(brandRepository).findById(brandId);
            verify(brandRepository).save(any(Brand.class));
        }

        @Test
        @DisplayName("Should update brand without name validation when name unchanged")
        void shouldUpdateBrandWithoutNameValidationWhenNameUnchanged() {
            // Given
            BrandUpdateRequest request =
                    BrandUpdateRequest.builder()
                            .name("Test Brand")
                            .description("Updated Description")
                            .build();

            Brand updatedBrand =
                    Brand.builder()
                            .id(brandId)
                            .name("Test Brand")
                            .description("Updated Description")
                            .isActive(true)
                            .build();

            BrandResponse updatedBrandResponse =
                    BrandResponse.builder()
                            .id(brandId)
                            .name("Test Brand")
                            .description("Updated Description")
                            .build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);
            when(brandMapper.toResponse(updatedBrand)).thenReturn(updatedBrandResponse);

            // When
            BrandResponse result = brandService.updateBrand(brandId, request);

            // Then
            assertThat(result).isNotNull();
            verify(brandRepository, never())
                    .existsByNameIgnoreCaseAndIdNot(anyString(), any(UUID.class));
        }

        @Test
        @DisplayName("Should throw exception when updated name already exists")
        void shouldThrowExceptionWhenUpdatedNameExists() {
            // Given
            BrandUpdateRequest request =
                    BrandUpdateRequest.builder().name("Existing Brand").build();

            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(brandRepository.existsByNameIgnoreCaseAndIdNot("Existing Brand", brandId))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> brandService.updateBrand(brandId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.BRAND_NAME_CONFLICT);
        }

        @Test
        @DisplayName("Should throw exception when brand not found")
        void shouldThrowExceptionWhenBrandNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            BrandUpdateRequest request = BrandUpdateRequest.builder().name("Updated Brand").build();

            when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> brandService.updateBrand(nonExistentId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.BRAND_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete Brand Tests")
    class DeleteBrandTests {

        @Test
        @DisplayName("Should delete brand successfully")
        void shouldDeleteBrandSuccessfully() {
            // Given
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));

            // When
            brandService.deleteBrand(brandId);

            // Then
            verify(brandRepository).findById(brandId);
            verify(brandRepository).delete(testBrand);
        }

        @Test
        @DisplayName("Should throw exception when brand not found")
        void shouldThrowExceptionWhenBrandNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(brandRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> brandService.deleteBrand(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.BRAND_NOT_FOUND);

            verify(brandRepository, never()).delete(any(Brand.class));
        }
    }
}
