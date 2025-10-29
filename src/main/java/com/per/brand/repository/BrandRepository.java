package com.per.brand.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.per.brand.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
