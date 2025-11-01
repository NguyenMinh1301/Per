package com.per.product.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

    private UUID id;
    private String variantSku;
    private BigDecimal volumeMl;
    private String packageType;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currencyCode;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private String imagePublicId;
    private String imageUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
