package com.per.product.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.per.product.enums.FragranceFamily;
import com.per.product.enums.Gender;
import com.per.product.enums.Longevity;
import com.per.product.enums.Occasion;
import com.per.product.enums.Seasonality;
import com.per.product.enums.Sillage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for product search with optional filters. All fields are optional - null values are
 * ignored in the search query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    /** Full-text search query. Searches name, description, brand, and category. */
    private String query;

    /** Filter by brand ID. */
    private UUID brandId;

    /** Filter by category ID. */
    private UUID categoryId;

    /** Filter by gender. */
    private Gender gender;

    /** Filter by fragrance family. */
    private FragranceFamily fragranceFamily;

    /** Filter by sillage (projection). */
    private Sillage sillage;

    /** Filter by longevity. */
    private Longevity longevity;

    /** Filter by seasonality. */
    private Seasonality seasonality;

    /** Filter by occasion. */
    private Occasion occasion;

    /** Filter by minimum price (inclusive). */
    private BigDecimal minPrice;

    /** Filter by maximum price (inclusive). */
    private BigDecimal maxPrice;

    /** Filter by active status. Defaults to true if not specified. */
    private Boolean isActive;
}
