package com.per.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.product.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    boolean existsByVariantSkuIgnoreCase(String variantSku);

    boolean existsByVariantSkuIgnoreCaseAndIdNot(String variantSku, UUID id);

    List<ProductVariant> findByProductId(UUID productId);

    Optional<ProductVariant> findByIdAndProductId(UUID id, UUID productId);
}
