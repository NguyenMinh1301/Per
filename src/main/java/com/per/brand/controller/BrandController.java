package com.per.brand.controller;

import java.util.UUID;

import com.per.common.base.BaseController;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.request.BrandUpdateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.brand.service.BrandService;
import com.per.common.ApiConstants;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Brand.ROOT)
@RequiredArgsConstructor
@Tag(name = "Brand", description = "Brand Management APIs")
public class BrandController extends BaseController {

    private final BrandService brandService;

    @GetMapping
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<PageResponse<BrandResponse>>> searchBrands(
            @RequestParam(value = "query", required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<BrandResponse> response = brandService.getBrands(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.BRAND_LIST_SUCCESS, response));
    }

    @GetMapping(ApiConstants.Brand.DETAILS)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrand(@PathVariable("id") UUID id) {
        BrandResponse response = brandService.getBrand(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.BRAND_FETCH_SUCCESS, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(
            @Valid @RequestBody BrandCreateRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.BRAND_CREATE_SUCCESS, response));
    }

    @PutMapping(ApiConstants.Brand.DETAILS)
    public ResponseEntity<ApiResponse<BrandResponse>> update(
            @PathVariable("id") UUID id, @Valid @RequestBody BrandUpdateRequest request) {
        BrandResponse response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.BRAND_UPDATE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.Brand.DETAILS)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.BRAND_DELETE_SUCCESS));
    }
}
