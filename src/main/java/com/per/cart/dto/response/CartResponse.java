package com.per.cart.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.per.cart.enums.CartStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    private UUID userId;
    private Integer totalItems;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private CartStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CartItemResponse> items;
}
