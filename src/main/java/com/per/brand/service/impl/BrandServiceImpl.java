package com.per.brand.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.request.BrandUpdateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.brand.entity.Brand;
import com.per.brand.mapper.BrandMapper;
import com.per.brand.repository.BrandRepository;
import com.per.brand.service.BrandService;
import com.per.common.exception.ApiErrorCode;
import com.per.common.response.PageResponse;
import com.per.common.util.ApiPreconditions;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> getBrands(String query, Pageable pageable) {
        Page<Brand> page;
        if (query == null || query.isBlank()) {
            page = brandRepository.findAll(pageable);
        } else {
            page = brandRepository.findByNameContainingIgnoreCase(query, pageable);
        }
        return PageResponse.from(page.map(brandMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrand(UUID id) {
        Brand brand = findBrand(id);
        return brandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse createBrand(BrandCreateRequest request) {
        String normalizedName = normalizeName(request.getName());
        assertNameAvailable(normalizedName);

        Brand brand = brandMapper.toEntity(request);
        brand.setName(normalizedName);
        boolean shouldActivate = request.getActive() == null || Boolean.TRUE.equals(request.getActive());
        brand.setActive(shouldActivate);

        Brand saved = brandRepository.save(brand);
        return brandMapper.toResponse(saved);
    }

    @Override
    public BrandResponse updateBrand(UUID id, BrandUpdateRequest request) {
        Brand brand = findBrand(id);

        String normalizedName = null;
        if (request.getName() != null) {
            normalizedName = normalizeName(request.getName());
            boolean nameChanged = !normalizedName.equalsIgnoreCase(brand.getName());
            if (nameChanged) {
                assertNameAvailable(normalizedName, id);
            }
        }

        brandMapper.updateEntity(request, brand);

        if (normalizedName != null) {
            brand.setName(normalizedName);
        }
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        Brand saved = brandRepository.save(brand);
        return brandMapper.toResponse(saved);
    }

    @Override
    public void deleteBrand(UUID id) {
        Brand brand = findBrand(id);
        brandRepository.delete(brand);
    }

    private Brand findBrand(UUID id) {
        return ApiPreconditions.checkFound(
                brandRepository.findById(id), ApiErrorCode.BRAND_NOT_FOUND);
    }

    private void assertNameAvailable(String name) {
        boolean exists = brandRepository.existsByNameIgnoreCase(name);
        ApiPreconditions.checkUnique(
                exists, ApiErrorCode.BRAND_NAME_CONFLICT, "Brand name already exists");
    }

    private void assertNameAvailable(String name, UUID excludeId) {
        boolean exists = brandRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        ApiPreconditions.checkUnique(
                exists, ApiErrorCode.BRAND_NAME_CONFLICT, "Brand name already exists");
    }

    private String normalizeName(String name) {
        ApiPreconditions.checkArgument(
                name != null && !name.isBlank(), ApiErrorCode.BAD_REQUEST, "Brand name must not be blank");
        return name;
    }
}
