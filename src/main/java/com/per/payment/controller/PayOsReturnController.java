package com.per.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.order.entity.Order;
import com.per.order.repository.OrderRepository;
import com.per.payment.dto.response.PayOsReturnResponse;
import com.per.payment.entity.Payment;
import com.per.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PayOsReturnController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("${payos.return-path}")
    public ResponseEntity<ApiResponse<PayOsReturnResponse>> handleReturn(
            @RequestParam("orderCode") Long orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);

        Payment payment = paymentRepository.findByOrderCode(orderCode).orElse(null);

        PayOsReturnResponse response =
                PayOsReturnResponse.builder()
                        .orderCode(orderCode)
                        .orderStatus(order != null ? order.getStatus() : null)
                        .paymentStatus(payment != null ? payment.getStatus() : null)
                        .message("Return from PayOS")
                        .build();
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.PAYMENT_RETURN_SUCCESS, response));
    }
}
