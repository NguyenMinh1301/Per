package com.per.product.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductVariantCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String variantSku;

    @NotNull
    @DecimalMin(value = "0.1")
    private BigDecimal volumeMl;

    @Size(max = 60)
    private String packageType;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal price;

    @DecimalMin(value = "0.0")
    private BigDecimal compareAtPrice;

    @Size(max = 10)
    private String currencyCode;

    private Integer stockQuantity;
    private Integer lowStockThreshold;

    @Size(max = 255)
    private String imagePublicId;

    private String imageUrl;

    private Boolean active;
}
