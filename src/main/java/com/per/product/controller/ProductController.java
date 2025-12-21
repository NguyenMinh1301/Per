package com.per.product.controller;

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

import com.per.common.ApiConstants;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;
import com.per.product.dto.request.ProductCreateRequest;
import com.per.product.dto.request.ProductUpdateRequest;
import com.per.product.dto.response.ProductDetailResponse;
import com.per.product.dto.response.ProductResponse;
import com.per.product.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Product.ROOT)
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management APIs")
public class ProductController extends BaseController {

    private final ProductService productService;

    @GetMapping
    @RateLimiter(name = "highTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<ProductResponse> data = productService.getProducts(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_LIST_SUCCESS, data));
    }

    @GetMapping(ApiConstants.Product.DETAILS)
    @RateLimiter(name = "highTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable("id") UUID id) {
        ProductDetailResponse data = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_FETCH_SUCCESS, data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse data = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.PRODUCT_CREATE_SUCCESS, data));
    }

    @PutMapping(ApiConstants.Product.DETAILS)
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable("id") UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse data = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_UPDATE_SUCCESS, data));
    }

    @DeleteMapping(ApiConstants.Product.DETAILS)
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_DELETE_SUCCESS));
    }
}
