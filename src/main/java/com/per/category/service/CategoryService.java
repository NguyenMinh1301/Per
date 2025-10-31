package com.per.category.service;

import com.per.category.dto.request.CategoryCreateRequest;
import com.per.category.dto.request.CategoryUpdateRequest;
import com.per.category.dto.response.CategoryResponse;
import com.per.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface CategoryService {

    PageResponse<CategoryResponse> getCategories(String query, Pageable pageable);

    CategoryResponse getCategory(UUID id);

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request);

    void deleteCategory(UUID id);
}