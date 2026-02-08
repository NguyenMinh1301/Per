package com.per.made_in.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MadeInUpdateRequest {

    @Size(min = 3, max = 150, message = "Name must contain at least 3 characters")
    private String name;

    @NotBlank(message = "Iso Code is required")
    @Size(min = 2, max = 150, message = "Name must contain at least 2 characters")
    private String isoCode;

    private String region;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 255)
    private String imagePublicId;

    private String imageUrl;

    private Boolean active;
}
