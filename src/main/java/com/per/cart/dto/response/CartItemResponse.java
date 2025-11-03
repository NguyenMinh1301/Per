package com.per.cart.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productImageUrl;
    private UUID variantId;
    private String variantSku;
    private BigDecimal variantVolumeMl;
    private String variantPackageType;
    private String variantImageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotalAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
