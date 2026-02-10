package com.per.payment.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.order.entity.Order;
import com.per.order.enums.OrderStatus;
import com.per.order.service.OrderInventoryService;
import com.per.payment.entity.Payment;
import com.per.payment.entity.PaymentTransaction;
import com.per.payment.enums.PaymentStatus;
import com.per.payment.enums.PaymentTransactionStatus;
import com.per.payment.repository.PaymentRepository;
import com.per.payment.repository.PaymentTransactionRepository;
import com.per.payment.service.PayOsWebhookService;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@Service
@RequiredArgsConstructor
@Transactional
public class PayOsWebhookServiceImpl implements PayOsWebhookService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderInventoryService orderInventoryService;
    private final ObjectMapper objectMapper;

    @Override
    public void handleWebhook(Webhook payload) {
        WebhookData data = verifyPayload(payload);

        Payment payment = paymentRepository.findByOrderCode(data.getOrderCode()).orElse(null);
        if (payment == null) {
            return;
        }

        boolean success =
                payload.getSuccess() != null
                        ? payload.getSuccess()
                        : "00".equalsIgnoreCase(data.getCode());
        PaymentStatus targetStatus = success ? PaymentStatus.PAID : PaymentStatus.CANCELLED;

        if (!success && payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        payment.setStatus(success ? PaymentStatus.PAID : targetStatus);

        Order order = payment.getOrder();
        if (success) {
            order.transitionTo(OrderStatus.PAID);
        } else if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            orderInventoryService.restoreStock(order);
            order.transitionTo(OrderStatus.CANCELLED);
        }

        persistTransactionIfNeeded(payment, data, success);
    }

    private WebhookData verifyPayload(Webhook payload) {
        try {
            return payOS.verifyPaymentWebhookData(payload);
        } catch (Exception ex) {
            throw new ApiException(ApiErrorCode.PAYMENT_WEBHOOK_INVALID, "Invalid PayOS webhook");
        }
    }

    private void persistTransactionIfNeeded(Payment payment, WebhookData data, boolean success) {
        if (data.getReference() != null
                && paymentTransactionRepository.findByReference(data.getReference()).isPresent()) {
            return;
        }
        try {
            PaymentTransaction transaction =
                    PaymentTransaction.builder()
                            .payment(payment)
                            .reference(data.getReference())
                            .amount(
                                    BigDecimal.valueOf(
                                            data.getAmount() == null ? 0 : data.getAmount()))
                            .status(
                                    success
                                            ? PaymentTransactionStatus.SUCCEEDED
                                            : PaymentTransactionStatus.FAILED)
                            .transactionDateTime(parseInstant(data.getTransactionDateTime()))
                            .currencyCode(data.getCurrency() == null ? "VND" : data.getCurrency())
                            .accountNumber(data.getAccountNumber())
                            .counterAccountNumber(data.getCounterAccountNumber())
                            .counterAccountName(data.getCounterAccountName())
                            .rawPayload(
                                    objectMapper.convertValue(
                                            data, new TypeReference<Map<String, Object>>() {}))
                            .build();
            paymentTransactionRepository.save(transaction);
        } catch (DataIntegrityViolationException ignored) {
            // duplicate reference, ignore
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
