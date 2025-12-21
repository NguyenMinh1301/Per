package com.per.cart.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.per.cart.dto.request.CartItemCreateRequest;
import com.per.cart.dto.request.CartItemUpdateRequest;
import com.per.cart.dto.response.CartResponse;
import com.per.cart.service.CartItemService;
import com.per.common.ApiConstants;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Cart.ROOT)
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart APIs")
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping(ApiConstants.Cart.ITEMS)
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemCreateRequest request) {
        CartResponse response = cartItemService.addItem(request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CART_ITEM_ADD_SUCCESS, response));
    }

    @PatchMapping(ApiConstants.Cart.ITEM_DETAILS)
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        CartResponse response = cartItemService.updateItem(itemId, request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CART_ITEM_UPDATE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.Cart.ITEM_DETAILS)
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable("itemId") UUID itemId) {
        CartResponse response = cartItemService.removeItem(itemId);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.CART_ITEM_REMOVE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.Cart.ITEMS)
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        CartResponse response = cartItemService.clearCart();
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.CART_CLEAR_SUCCESS, response));
    }
}
