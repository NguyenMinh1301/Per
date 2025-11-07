package com.per.payment.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.per.order.entity.Order;
import com.per.payment.enums.PaymentStatus;
import com.per.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_payment_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_order"))
    private Order order;

    @Column(name = "order_code", nullable = false, unique = true)
    private Long orderCode; // PayOS order code

    @Column(name = "payment_link_id", length = 128, nullable = false, unique = true)
    private String paymentLinkId; // ID returned from PayOS

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode = "VND";

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "checkout_url", columnDefinition = "text")
    private String checkoutUrl; // PayOS redirect link

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 32, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
