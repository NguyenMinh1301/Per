package com.per.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    boolean existsByOrderCode(Long orderCode);

    Optional<Order> findByOrderCode(Long orderCode);
}
