package com.per.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.per.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Cascade re-indexing queries for CDC
    List<Product> findByBrandId(UUID brandId);

    List<Product> findByCategoryId(UUID categoryId);

    List<Product> findByMadeInId(UUID madeInId);

    // ========== Optimized queries for Bulk CDC Indexing ==========
    // These use JOIN FETCH to load Brand, Category, MadeIn in one query.
    // Variants are fetched separately to avoid Cartesian product.

    @Query(
            "SELECT DISTINCT p FROM Product p "
                    + "LEFT JOIN FETCH p.brand "
                    + "LEFT JOIN FETCH p.category "
                    + "LEFT JOIN FETCH p.madeIn "
                    + "WHERE p.brand.id = :brandId")
    List<Product> findAllByBrandIdWithRelations(@Param("brandId") UUID brandId);

    @Query(
            "SELECT DISTINCT p FROM Product p "
                    + "LEFT JOIN FETCH p.brand "
                    + "LEFT JOIN FETCH p.category "
                    + "LEFT JOIN FETCH p.madeIn "
                    + "WHERE p.category.id = :categoryId")
    List<Product> findAllByCategoryIdWithRelations(@Param("categoryId") UUID categoryId);

    @Query(
            "SELECT DISTINCT p FROM Product p "
                    + "LEFT JOIN FETCH p.brand "
                    + "LEFT JOIN FETCH p.category "
                    + "LEFT JOIN FETCH p.madeIn "
                    + "WHERE p.madeIn.id = :madeInId")
    List<Product> findAllByMadeInIdWithRelations(@Param("madeInId") UUID madeInId);
}
