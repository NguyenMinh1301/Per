package com.per.payment.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.per.order.enums.OrderStatus;
import com.per.payment.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {
    private final UUID orderId;
    private final Long orderCode;
    private final OrderStatus orderStatus;
    private final UUID paymentId;
    private final PaymentStatus paymentStatus;
    private final String paymentLinkId;
    private final String checkoutUrl;
    private final BigDecimal amount;
}
