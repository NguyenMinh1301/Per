package com.per.product.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.per.product.enums.FragranceFamily;
import com.per.product.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private UUID brandId;
    private UUID categoryId;
    private UUID madeInId;
    private String name;
    private String shortDescription;
    private String imageUrl;
    private Integer launchYear;
    private FragranceFamily fragranceFamily;
    private Gender gender;
    private boolean limitedEdition;
    private boolean discontinued;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
