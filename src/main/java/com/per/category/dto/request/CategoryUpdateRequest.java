package com.per.category.dto.request;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {

    @Size(min = 3, max = 150, message = "Name must contain at least 3 characters")
    private String name;

    private String description;

    private String descriptions;

    @Size(max = 255, message = "Image url is too long")
    private String imagePublicId;

    private String imageUrl;

    private Boolean isActive;
}
