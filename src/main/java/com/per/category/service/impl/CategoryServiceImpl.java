package com.per.category.service.impl;

import java.util.UUID;

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

    @Override
    @Transactional(readOnly = true)
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
    public CategoryResponse getCategory(UUID id) {
        Category category = findById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String name = request.getName();
        validateNameUniqueness(name);

        Category category = categoryMapper.toEntity(request);
        category.setName(name);
        category.setIsActive(
                request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive()));

        Category saved = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
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
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteCategory(UUID id) {
        Category category = findById(id);
        categoryRepository.delete(category);
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
