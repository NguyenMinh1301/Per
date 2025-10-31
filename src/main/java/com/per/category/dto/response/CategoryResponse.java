package com.per.category.dto.response;

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
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private String descriptions;
    private String imagePublicId;
    private String imageUrl;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
