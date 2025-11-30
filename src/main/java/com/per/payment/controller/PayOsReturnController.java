package com.per.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.order.entity.Order;
import com.per.order.enums.OrderStatus;
import com.per.order.repository.OrderRepository;
import com.per.order.service.OrderInventoryService;
import com.per.payment.dto.response.PayOsReturnResponse;
import com.per.payment.entity.Payment;
import com.per.payment.enums.PaymentStatus;
import com.per.payment.repository.PaymentRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment Management APIs")
public class PayOsReturnController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderInventoryService orderInventoryService;

    @GetMapping("${payos.return-path}")
    @Transactional
    public ResponseEntity<ApiResponse<PayOsReturnResponse>> handleReturn(
            @RequestParam("orderCode") Long orderCode,
            @RequestParam(value = "cancel", required = false, defaultValue = "false")
                    boolean cancel,
            @RequestParam(value = "code", required = false) String code) {
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);

        Payment payment = paymentRepository.findByOrderCode(orderCode).orElse(null);

        boolean isFailure =
                cancel
                        || (code != null
                                && !"00".equalsIgnoreCase(code)
                                && !"0".equalsIgnoreCase(code));

        if (isFailure && payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.CANCELLED);
            if (order != null && order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                orderInventoryService.restoreStock(order);
                order.setStatus(OrderStatus.CANCELLED);
            }
        }

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
