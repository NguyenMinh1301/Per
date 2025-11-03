package com.per.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.per.cart.entity.Cart;
import com.per.cart.enums.CartStatus;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.productVariant"})
    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);

    @EntityGraph(attributePaths = {"items", "items.product", "items.productVariant"})
    Optional<Cart> findWithItemsById(UUID id);
}
