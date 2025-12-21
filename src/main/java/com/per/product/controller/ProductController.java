package com.per.product.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiConstants;
import com.per.common.base.BaseController;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;
import com.per.product.dto.request.ProductCreateRequest;
import com.per.product.dto.request.ProductSearchRequest;
import com.per.product.dto.request.ProductUpdateRequest;
import com.per.product.dto.response.ProductDetailResponse;
import com.per.product.dto.response.ProductResponse;
import com.per.product.dto.response.ProductSearchResponse;
import com.per.product.service.ProductSearchService;
import com.per.product.service.ProductService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Product.ROOT)
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management APIs")
public class ProductController extends BaseController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    @GetMapping(ApiConstants.Product.SEARCH)
    @RateLimiter(name = "highTraffic", fallbackMethod = "rateLimit")
    @Operation(
            summary = "Search products",
            description = "Full-text search with fuzzy matching and filters")
    public ResponseEntity<ApiResponse<PageResponse<ProductSearchResponse>>> searchProducts(
            @Valid ProductSearchRequest request, @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ProductSearchResponse> data = productSearchService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_LIST_SUCCESS, data));
    }

    @PostMapping(ApiConstants.Product.REINDEX)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reindex all products",
            description = "Admin operation to rebuild Elasticsearch index")
    public ResponseEntity<ApiResponse<Void>> reindexProducts() {
        productSearchService.reindexAll();
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_UPDATE_SUCCESS));
    }

    @GetMapping(ApiConstants.Product.LIST)
    @RateLimiter(name = "highTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<ProductResponse> data = productService.getProducts(null, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_LIST_SUCCESS, data));
    }

    @GetMapping(ApiConstants.Product.DETAIL)
    @RateLimiter(name = "highTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable("id") UUID id) {
        ProductDetailResponse data = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_FETCH_SUCCESS, data));
    }

    @PostMapping(ApiConstants.Product.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse data = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.PRODUCT_CREATE_SUCCESS, data));
    }

    @PutMapping(ApiConstants.Product.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable("id") UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse data = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_UPDATE_SUCCESS, data));
    }

    @DeleteMapping(ApiConstants.Product.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PRODUCT_DELETE_SUCCESS));
    }
}
