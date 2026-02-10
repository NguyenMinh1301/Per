package com.per.order.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.per.order.enums.OrderStatus;
import com.per.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"order\"")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_user"))
    private User user;

    @Setter
    @Column(name = "order_code", nullable = false, unique = true)
    private Long orderCode;

    @Setter
    @Builder.Default
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Setter
    @Builder.Default
    @Column(name = "subtotal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Setter
    @Builder.Default
    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Setter
    @Builder.Default
    @Column(name = "shipping_fee", precision = 15, scale = 2, nullable = false)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Setter
    @Builder.Default
    @Column(name = "grand_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Setter
    @Builder.Default
    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode = "VND";

    @Setter
    @Column(name = "receiver_name", length = 180)
    private String receiverName;

    @Setter
    @Column(name = "receiver_phone", length = 32)
    private String receiverPhone;

    @Setter
    @Column(name = "shipping_address", columnDefinition = "text")
    private String shippingAddress;

    @Setter
    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 32, nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Setter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Transition the order to a new status.
     *
     * @param newStatus the new status to transition to
     * @throws IllegalStateException if the transition is invalid
     */
    public void transitionTo(OrderStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }

        if (isTerminalState(this.status)) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot transition from terminal state %s to %s",
                            this.status, newStatus));
        }

        if (this.status == OrderStatus.PENDING_PAYMENT) {
            if (newStatus == OrderStatus.PAID
                    || newStatus == OrderStatus.FAILED
                    || newStatus == OrderStatus.CANCELLED) {
                this.status = newStatus;
                return;
            }
        }

        throw new IllegalStateException(
                String.format("Invalid transition from %s to %s", this.status, newStatus));
    }

    private boolean isTerminalState(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.FAILED
                || status == OrderStatus.CANCELLED;
    }
}
