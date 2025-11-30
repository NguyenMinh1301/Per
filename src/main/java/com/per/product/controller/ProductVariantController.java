package com.per.product.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.product.dto.request.ProductVariantCreateRequest;
import com.per.product.dto.request.ProductVariantUpdateRequest;
import com.per.product.dto.response.ProductVariantResponse;
import com.per.product.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.ProductVariant.ROOT)
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management APIs")
public class ProductVariantController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody ProductVariantCreateRequest request) {
        ProductVariantResponse data = productService.addVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.PRODUCT_VARIANT_CREATE_SUCCESS, data));
    }

    @PutMapping(ApiConstants.Product.DETAILS)
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable("productId") UUID productId,
            @PathVariable("variantId") UUID variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request) {
        ProductVariantResponse data = productService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.PRODUCT_VARIANT_UPDATE_SUCCESS, data));
    }

    @DeleteMapping(ApiConstants.Product.DETAILS)
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable("productId") UUID productId, @PathVariable("variantId") UUID variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.PRODUCT_VARIANT_DELETE_SUCCESS));
    }
}
