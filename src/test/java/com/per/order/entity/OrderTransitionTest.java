package com.per.order.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.per.order.enums.OrderStatus;

class OrderTransitionTest {

    @Test
    void shouldTransitionFromPendingToPaid() {
        Order order = Order.builder().status(OrderStatus.PENDING_PAYMENT).build();
        order.transitionTo(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldTransitionFromPendingToFailed() {
        Order order = Order.builder().status(OrderStatus.PENDING_PAYMENT).build();
        order.transitionTo(OrderStatus.FAILED);
        assertEquals(OrderStatus.FAILED, order.getStatus());
    }

    @Test
    void shouldTransitionFromPendingToCancelled() {
        Order order = Order.builder().status(OrderStatus.PENDING_PAYMENT).build();
        order.transitionTo(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromPaid() {
        Order order = Order.builder().status(OrderStatus.PAID).build();
        assertThrows(IllegalStateException.class, () -> order.transitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromCancelled() {
        Order order = Order.builder().status(OrderStatus.CANCELLED).build();
        assertThrows(IllegalStateException.class, () -> order.transitionTo(OrderStatus.PAID));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningFromFailed() {
        Order order = Order.builder().status(OrderStatus.FAILED).build();
        assertThrows(IllegalStateException.class, () -> order.transitionTo(OrderStatus.PAID));
    }
}
