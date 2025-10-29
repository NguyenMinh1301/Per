package com.per.brand.mapper;

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.brand.entity.Brand;

public final class BrandMapper {

    private BrandMapper() {}

    public static Brand toEntity(BrandCreateRequest request) {
        String normalizedName = normalize(request.getName());
        boolean active = request.getActive() == null || Boolean.TRUE.equals(request.getActive());

        return Brand.builder()
                .name(normalizedName)
                .description(trimToNull(request.getDescription()))
                .websiteUrl(trimToNull(request.getWebsiteUrl()))
                .foundedYear(request.getFoundedYear())
                .imagePublicId(trimToNull(request.getImagePublicId()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .isActive(active)
                .build();
    }

    public static BrandResponse toResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .websiteUrl(brand.getWebsiteUrl())
                .foundedYear(brand.getFoundedYear())
                .imagePublicId(brand.getImagePublicId())
                .imageUrl(brand.getImageUrl())
                .active(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
