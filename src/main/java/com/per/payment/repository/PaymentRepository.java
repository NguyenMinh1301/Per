package com.per.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderCode(Long orderCode);

    Optional<Payment> findByPaymentLinkId(String paymentLinkId);
}
