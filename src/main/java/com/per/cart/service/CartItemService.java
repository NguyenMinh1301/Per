package com.per.cart.service;

import java.util.UUID;

import com.per.cart.dto.request.CartItemCreateRequest;
import com.per.cart.dto.request.CartItemUpdateRequest;
import com.per.cart.dto.response.CartResponse;

public interface CartItemService {

    CartResponse addItem(CartItemCreateRequest request);

    CartResponse updateItem(UUID itemId, CartItemUpdateRequest request);

    CartResponse removeItem(UUID itemId);

    CartResponse clearCart();
}
