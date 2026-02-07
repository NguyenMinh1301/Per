package com.per.category.service.impl;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.category.dto.request.CategoryCreateRequest;
import com.per.category.dto.request.CategoryUpdateRequest;
import com.per.category.dto.response.CategoryResponse;
import com.per.category.entity.Category;
import com.per.category.mapper.CategoryMapper;
import com.per.category.repository.CategoryRepository;
import com.per.category.service.CategoryService;
import com.per.common.config.cache.CacheEvictionHelper;
import com.per.common.config.cache.CacheNames;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CacheEvictionHelper cacheEvictionHelper;

    // Note: Dual-write removed - CDC via Debezium handles ES/Qdrant sync

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.CATEGORIES,
            key =
                    "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize",
            sync = true)
    public PageResponse<CategoryResponse> getCategories(String query, Pageable pageable) {
        Page<Category> page;

        if (query == null || query.isBlank()) {
            page = categoryRepository.findAll(pageable);
        } else {
            page = categoryRepository.search(query, pageable);
        }

        return PageResponse.from(page.map(categoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY, key = "#id", sync = true)
    public CategoryResponse getCategory(UUID id) {
        Category category = findById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String name = request.getName();
        validateNameUniqueness(name);

        Category category = categoryMapper.toEntity(request);
        category.setName(name);

        Category saved = categoryRepository.save(category);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.CATEGORIES);
        // Note: ES/Qdrant sync handled by CDC

        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = findById(id);

        String name = null;
        if (request.getName() != null) {
            name = request.getName();
            if (!name.equalsIgnoreCase(category.getName())) {
                validateNameUniqueness(name, id);
            }
        }

        categoryMapper.updateEntity(request, category);

        if (name != null) {
            category.setName(name);
        }

        Category saved = categoryRepository.save(category);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.CATEGORIES);
        cacheEvictionHelper.evictAfterCommit(CacheNames.CATEGORY, id);
        // Note: ES/Qdrant sync handled by CDC

        return categoryMapper.toResponse(saved);
    }

    @Override
    public void deleteCategory(UUID id) {
        Category category = findById(id);
        categoryRepository.delete(category);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.CATEGORIES);
        cacheEvictionHelper.evictAfterCommit(CacheNames.CATEGORY, id);
        // Note: ES/Qdrant sync handled by CDC
    }

    private Category findById(UUID id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateNameUniqueness(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(ApiErrorCode.CATEGORY_NAME_CONFLICT);
        }
    }

    private void validateNameUniqueness(String name, UUID id) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(ApiErrorCode.CATEGORY_NAME_CONFLICT);
        }
    }
}
