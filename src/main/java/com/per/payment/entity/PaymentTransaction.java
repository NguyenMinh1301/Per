package com.per.payment.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.per.payment.enums.PaymentTransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "payment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_txn_payment"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Payment payment;

    @Column(name = "reference", length = 128, unique = true)
    private String reference;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private PaymentTransactionStatus status = PaymentTransactionStatus.SUCCEEDED;

    @Column(name = "transaction_date_time")
    private Instant transactionDateTime;

    @Builder.Default
    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode = "VND";

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    @Column(name = "counter_account_number", length = 64)
    private String counterAccountNumber;

    @Column(name = "counter_account_name", length = 255)
    private String counterAccountName;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Map<String, Object> rawPayload = Map.of();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
