package com.per.payment.dto.response;

import com.per.order.enums.OrderStatus;
import com.per.payment.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayOsReturnResponse {
    private final Long orderCode;
    private final OrderStatus orderStatus;
    private final PaymentStatus paymentStatus;
    private final String message;
}
