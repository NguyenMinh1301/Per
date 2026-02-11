package com.per.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.per.product.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    boolean existsByVariantSkuIgnoreCase(String variantSku);

    boolean existsByVariantSkuIgnoreCaseAndIdNot(String variantSku, UUID id);

    List<ProductVariant> findByProductId(UUID productId);

    Optional<ProductVariant> findByIdAndProductId(UUID id, UUID productId);

    // Bulk fetch for CDC indexing
    List<ProductVariant> findByProductIdIn(List<UUID> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") UUID id);
}
