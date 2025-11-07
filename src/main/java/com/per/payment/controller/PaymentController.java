package com.per.payment.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.payment.dto.request.CheckoutRequest;
import com.per.payment.dto.response.CheckoutResponse;
import com.per.payment.service.CheckoutService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.Payment.ROOT)
@RequiredArgsConstructor
public class PaymentController {

    private final CheckoutService checkoutService;

    @PostMapping(ApiConstants.Payment.CHECKOUT)
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.PAYMENT_CHECKOUT_SUCCESS, response));
    }
}
