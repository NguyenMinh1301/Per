package com.per.product.dto.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Size;

import com.per.product.enums.FragranceFamily;
import com.per.product.enums.Gender;
import com.per.product.enums.Longevity;
import com.per.product.enums.Occasion;
import com.per.product.enums.Seasonality;
import com.per.product.enums.Sillage;

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
public class ProductUpdateRequest {

    private UUID brandId;
    private UUID categoryId;
    private UUID madeInId;

    @Size(min = 1, max = 255)
    private String name;

    @Size(max = 600)
    private String shortDescription;

    private String description;

    private Integer launchYear;

    @Size(max = 255)
    private String imagePublicId;

    private String imageUrl;

    private FragranceFamily fragranceFamily;
    private Gender gender;
    private Sillage sillage;
    private Longevity longevity;
    private Seasonality seasonality;
    private Occasion occasion;

    private Boolean limitedEdition;
    private Boolean discontinued;
    private Boolean active;

    private Set<ProductVariantCreateRequest> variantsToAdd;
    private Set<ProductVariantUpdateRequest> variantsToUpdate;
    private Set<UUID> variantsToDelete;
}
