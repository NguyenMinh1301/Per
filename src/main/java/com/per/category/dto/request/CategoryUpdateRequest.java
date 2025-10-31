package com.per.category.dto.request;

import jakarta.validation.constraints.Size;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    @Size(min = 3, max = 150, message = "Name must contain at least 3 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String descriptions;

    @Size(max = 255)
    private String imagePublicId;

    private String imageUrl;

    private Boolean isActive;
}
