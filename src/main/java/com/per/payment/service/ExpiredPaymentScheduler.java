package com.per.payment.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.per.order.entity.Order;
import com.per.order.enums.OrderStatus;
import com.per.order.service.OrderInventoryService;
import com.per.payment.entity.Payment;
import com.per.payment.enums.PaymentStatus;
import com.per.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredPaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final OrderInventoryService orderInventoryService;

    @Value("${payment.expiration-check-batch-size:50}")
    private int batchSize;

    // @Scheduled(fixedDelayString = "${payment.expiration-check-interval:PT1M}")
    @Transactional
    public void markExpiredPayments() {
        Instant now = Instant.now();
        List<Payment> expired =
                paymentRepository.findTop100ByStatusAndExpiredAtBeforeOrderByExpiredAtAsc(
                        PaymentStatus.PENDING, now);

        if (expired.isEmpty()) {
            return;
        }

        expired.stream().limit(batchSize).forEach(this::expirePayment);
    }

    private void expirePayment(Payment payment) {
        if (payment.getExpiredAt() == null) {
            return;
        }
        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            orderInventoryService.restoreStock(order);
            order.setStatus(OrderStatus.FAILED);
            log.info(
                    "Order {} marked as FAILED due to expiration (payment {}).",
                    order.getOrderCode(),
                    payment.getId());
        }
        payment.setStatus(PaymentStatus.FAILED);
    }
}
