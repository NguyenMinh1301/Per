package com.per.payment.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.payment.entity.Payment;
import com.per.payment.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderCode(Long orderCode);

    Optional<Payment> findByPaymentLinkId(String paymentLinkId);

    List<Payment> findTop100ByStatusAndExpiredAtBeforeOrderByExpiredAtAsc(
            PaymentStatus status, Instant expiredAt);
}
