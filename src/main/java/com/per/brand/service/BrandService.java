package com.per.brand.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.request.BrandUpdateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.common.response.PageResponse;

public interface BrandService {

    PageResponse<BrandResponse> getBrands(String query, Pageable pageable);

    BrandResponse getBrand(UUID id);

    BrandResponse createBrand(BrandCreateRequest request);

    BrandResponse updateBrand(UUID id, BrandUpdateRequest request);

    void deleteBrand(UUID id);
}
