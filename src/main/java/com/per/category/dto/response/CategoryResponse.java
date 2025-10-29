package com.per.category.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private final UUID id;
    private final String name;
    private final String description;
    private final String descriptions;
    private final String imagePublicId;
    private final String imageUrl;
    private final Boolean isActive;
}
