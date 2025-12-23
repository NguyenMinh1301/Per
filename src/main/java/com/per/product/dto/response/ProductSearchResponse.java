package com.per.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for product search results. Contains essential fields for displaying search results
 * in a list view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    private String id;
    private String name;
    private String shortDescription;
    private String brandName;
    private String categoryName;
    private String gender;
    private Double minPrice;
    private Double maxPrice;
    private String imageUrl;
}
