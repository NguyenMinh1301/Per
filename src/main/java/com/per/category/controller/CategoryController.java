package com.per.category.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.per.category.dto.request.CategoryCreateRequest;
import com.per.category.dto.request.CategoryUpdateRequest;
import com.per.category.dto.response.CategoryResponse;
import com.per.category.service.CategoryService;
import com.per.common.ApiConstants;
import com.per.common.base.BaseController;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Category.ROOT)
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category Management APIs")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @GetMapping(ApiConstants.Category.LIST)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> searchCategories(
            @RequestParam(value = "query", required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<CategoryResponse> response = categoryService.getCategories(query, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CATEGORY_LIST_SUCCESS, response));
    }

    @GetMapping(ApiConstants.Category.DETAIL)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable("id") UUID id) {
        CategoryResponse response = categoryService.getCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CATEGORY_FETCH_SUCCESS, response));
    }

    @PostMapping(ApiConstants.Category.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.CATEGORY_CREATE_SUCCESS, response));
    }

    @PutMapping(ApiConstants.Category.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable("id") UUID id, @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CATEGORY_UPDATE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.Category.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.CATEGORY_DELETE_SUCCESS));
    }
}
