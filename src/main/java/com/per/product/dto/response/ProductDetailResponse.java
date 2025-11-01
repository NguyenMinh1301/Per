package com.per.product.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
public class ProductDetailResponse {

    private UUID id;
    private UUID brandId;
    private UUID categoryId;
    private UUID madeInId;
    private String name;
    private String shortDescription;
    private String description;
    private Integer launchYear;
    private String imagePublicId;
    private String imageUrl;
    private FragranceFamily fragranceFamily;
    private Gender gender;
    private Sillage sillage;
    private Longevity longevity;
    private Seasonality seasonality;
    private Occasion occasion;
    private boolean limitedEdition;
    private boolean discontinued;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProductVariantResponse> variants;
}
