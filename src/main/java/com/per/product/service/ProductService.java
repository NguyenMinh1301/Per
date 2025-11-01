package com.per.product.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;
import com.per.product.dto.request.ProductCreateRequest;
import com.per.product.dto.request.ProductUpdateRequest;
import com.per.product.dto.request.ProductVariantCreateRequest;
import com.per.product.dto.request.ProductVariantUpdateRequest;
import com.per.product.dto.response.ProductDetailResponse;
import com.per.product.dto.response.ProductResponse;
import com.per.product.dto.response.ProductVariantResponse;

public interface ProductService {

    PageResponse<ProductResponse> getProducts(String query, Pageable pageable);

    ProductDetailResponse getProduct(UUID id);

    ProductDetailResponse createProduct(ProductCreateRequest request);

    ProductDetailResponse updateProduct(UUID id, ProductUpdateRequest request);

    void deleteProduct(UUID id);

    ProductVariantResponse addVariant(UUID productId, ProductVariantCreateRequest request);

    ProductVariantResponse updateVariant(
            UUID productId, UUID variantId, ProductVariantUpdateRequest request);

    void deleteVariant(UUID productId, UUID variantId);
}
