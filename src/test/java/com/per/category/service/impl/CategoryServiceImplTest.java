package com.per.category.service.impl;

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

import com.per.category.dto.request.CategoryCreateRequest;
import com.per.category.dto.request.CategoryUpdateRequest;
import com.per.category.dto.response.CategoryResponse;
import com.per.category.entity.Category;
import com.per.category.mapper.CategoryMapper;
import com.per.category.repository.CategoryRepository;
import com.per.common.cache.CacheEvictionHelper;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;

    @Mock private CategoryMapper categoryMapper;

    @Mock private CacheEvictionHelper cacheEvictionHelper;

    @InjectMocks private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryResponse categoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        testCategory =
                Category.builder()
                        .id(categoryId)
                        .name("Test Category")
                        .description("Test Description")
                        .isActive(true)
                        .build();

        categoryResponse =
                CategoryResponse.builder()
                        .id(categoryId)
                        .name("Test Category")
                        .description("Test Description")
                        .isActive(true)
                        .build();
    }

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories when query is null")
        void shouldReturnPaginatedCategoriesWhenQueryIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(java.util.List.of(testCategory));

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

            // When
            PageResponse<CategoryResponse> result = categoryService.getCategories(null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Category");

            verify(categoryRepository).findAll(pageable);
            verify(categoryRepository, never()).search(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return searched categories when query is provided")
        void shouldReturnSearchedCategoriesWhenQueryIsProvided() {
            // Given
            String query = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(java.util.List.of(testCategory));

            when(categoryRepository.search(query, pageable)).thenReturn(categoryPage);
            when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

            // When
            PageResponse<CategoryResponse> result = categoryService.getCategories(query, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(categoryRepository).search(query, pageable);
        }
    }

    @Nested
    @DisplayName("Get Category Tests")
    class GetCategoryTests {

        @Test
        @DisplayName("Should return category by id")
        void shouldReturnCategoryById() {
            // Given
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

            // When
            CategoryResponse result = categoryService.getCategory(categoryId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(categoryId);
            assertThat(result.getName()).isEqualTo("Test Category");

            verify(categoryRepository).findById(categoryId);
            verify(categoryMapper).toResponse(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.getCategory(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.CATEGORY_NOT_FOUND);

            verify(categoryRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            // Given
            CategoryCreateRequest request =
                    CategoryCreateRequest.builder()
                            .name("New Category")
                            .description("New Description")
                            .isActive(true)
                            .build();

            Category newCategory =
                    Category.builder()
                            .id(UUID.randomUUID())
                            .name("New Category")
                            .description("New Description")
                            .isActive(true)
                            .build();

            CategoryResponse newCategoryResponse =
                    CategoryResponse.builder()
                            .id(newCategory.getId())
                            .name("New Category")
                            .description("New Description")
                            .isActive(true)
                            .build();

            when(categoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
            when(categoryMapper.toEntity(request)).thenReturn(newCategory);
            when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
            when(categoryMapper.toResponse(newCategory)).thenReturn(newCategoryResponse);

            // When
            CategoryResponse result = categoryService.createCategory(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Category");
            assertThat(result.getIsActive()).isTrue();

            verify(categoryRepository).existsByNameIgnoreCase("New Category");
        }

        @Test
        @DisplayName("Should set isActive to true by default when not provided")
        void shouldSetIsActiveToTrueByDefaultWhenNotProvided() {
            // Given
            CategoryCreateRequest request =
                    CategoryCreateRequest.builder()
                            .name("New Category")
                            .description("New Description")
                            .isActive(null)
                            .build();

            Category newCategory =
                    Category.builder()
                            .id(UUID.randomUUID())
                            .name("New Category")
                            .description("New Description")
                            .isActive(true)
                            .build();

            CategoryResponse newCategoryResponse =
                    CategoryResponse.builder()
                            .id(newCategory.getId())
                            .name("New Category")
                            .isActive(true)
                            .build();

            when(categoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
            when(categoryMapper.toEntity(request)).thenReturn(newCategory);
            when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
            when(categoryMapper.toResponse(newCategory)).thenReturn(newCategoryResponse);

            // When
            CategoryResponse result = categoryService.createCategory(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowExceptionWhenCategoryNameExists() {
            // Given
            CategoryCreateRequest request =
                    CategoryCreateRequest.builder()
                            .name("Existing Category")
                            .description("Description")
                            .build();

            when(categoryRepository.existsByNameIgnoreCase("Existing Category")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.createCategory(request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.CATEGORY_NAME_CONFLICT);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Given
            CategoryUpdateRequest request =
                    CategoryUpdateRequest.builder()
                            .name("Updated Category")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            Category updatedCategory =
                    Category.builder()
                            .id(categoryId)
                            .name("Updated Category")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            CategoryResponse updatedCategoryResponse =
                    CategoryResponse.builder()
                            .id(categoryId)
                            .name("Updated Category")
                            .description("Updated Description")
                            .isActive(false)
                            .build();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Updated Category", categoryId))
                    .thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
            when(categoryMapper.toResponse(updatedCategory)).thenReturn(updatedCategoryResponse);

            // When
            CategoryResponse result = categoryService.updateCategory(categoryId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Category");
            assertThat(result.getIsActive()).isFalse();

            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when updated name already exists")
        void shouldThrowExceptionWhenUpdatedNameExists() {
            // Given
            CategoryUpdateRequest request =
                    CategoryUpdateRequest.builder().name("Existing Category").build();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Existing Category", categoryId))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.CATEGORY_NAME_CONFLICT);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            CategoryUpdateRequest request =
                    CategoryUpdateRequest.builder().name("Updated Category").build();

            when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.updateCategory(nonExistentId, request))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() {
            // Given
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

            // When
            categoryService.deleteCategory(categoryId);

            // Then
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.deleteCategory(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ApiErrorCode.CATEGORY_NOT_FOUND);

            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }
}
