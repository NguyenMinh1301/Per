package com.per.brand.mapper;

import org.springframework.stereotype.Component;

import com.per.brand.document.BrandDocument;
import com.per.brand.entity.Brand;

@Component
public class BrandDocumentMapper {

    public BrandDocument toDocument(Brand brand) {
        return BrandDocument.builder()
                .id(brand.getId().toString())
                .name(brand.getName())
                .description(brand.getDescription())
                .websiteUrl(brand.getWebsiteUrl())
                .foundedYear(brand.getFoundedYear())
                .imageUrl(brand.getImageUrl())
                .isActive(brand.getIsActive())
                .build();
    }
}
