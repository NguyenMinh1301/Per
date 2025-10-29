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
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> searchBrands(String query, Pageable pageable) {
        Page<Brand> page;
        if (query == null || query.isBlank()) {
            page = brandRepository.findAll(pageable);
        } else {
            page = brandRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
        }
        return PageResponse.from(page.map(BrandMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrand(UUID id) {
        Brand brand = findBrand(id);
        return BrandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse createBrand(BrandCreateRequest request) {
        String normalizedName = normalizeName(request.getName());
        validateNameUniqueness(normalizedName);

        Brand brand = BrandMapper.toEntity(request);
        brand.setName(normalizedName);

        Brand saved = brandRepository.save(brand);
        return BrandMapper.toResponse(saved);
    }

    @Override
    public BrandResponse updateBrand(UUID id, BrandUpdateRequest request) {
        Brand brand = findBrand(id);

        if (request.getName() != null) {
            String normalizedName = normalizeName(request.getName());
            if (!normalizedName.equalsIgnoreCase(brand.getName())) {
                validateNameUniqueness(normalizedName, id);
                brand.setName(normalizedName);
            }
        }

        if (request.getDescription() != null) {
            brand.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getWebsiteUrl() != null) {
            brand.setWebsiteUrl(trimToNull(request.getWebsiteUrl()));
        }
        if (request.getFoundedYear() != null) {
            brand.setFoundedYear(request.getFoundedYear());
        }
        if (request.getImagePublicId() != null) {
            brand.setImagePublicId(trimToNull(request.getImagePublicId()));
        }
        if (request.getImageUrl() != null) {
            brand.setImageUrl(trimToNull(request.getImageUrl()));
        }
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        Brand saved = brandRepository.save(brand);
        return BrandMapper.toResponse(saved);
    }

    @Override
    public void deleteBrand(UUID id) {
        Brand brand = findBrand(id);
        brandRepository.delete(brand);
    }

    private Brand findBrand(UUID id) {
        return brandRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.BRAND_NOT_FOUND));
    }

    private void validateNameUniqueness(String name) {
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(ApiErrorCode.BRAND_NAME_CONFLICT);
        }
    }

    private void validateNameUniqueness(String name, UUID id) {
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(ApiErrorCode.BRAND_NAME_CONFLICT);
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Brand name must not be blank");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
