package com.per.brand.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.per.brand.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query(
            """
			SELECT b FROM Brand b
			WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(COALESCE(b.websiteUrl, '')) LIKE LOWER(CONCAT('%', :query, '%'))
			""")
    Page<Brand> search(String name, Pageable pageable);
}
