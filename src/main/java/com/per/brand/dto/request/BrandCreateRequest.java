package com.per.brand.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class BrandCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 255)
    private String websiteUrl;

    @Min(1000)
    @Max(9999)
    private Integer foundedYear;

    @Size(max = 255)
    private String imagePublicId;

    @Size(max = 2048)
    private String imageUrl;

    private Boolean active;
}
