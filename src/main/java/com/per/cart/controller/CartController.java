package com.per.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.per.cart.dto.response.CartResponse;
import com.per.cart.service.CartService;
import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Cart.ROOT)
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getActiveCart();
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.CART_FETCH_SUCCESS, response));
    }
}
