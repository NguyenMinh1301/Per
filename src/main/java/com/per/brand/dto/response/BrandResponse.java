package com.per.brand.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

    private UUID id;
    private String name;
    private String description;
    private String websiteUrl;
    private Integer foundedYear;
    private String imagePublicId;
    private String imageUrl;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
