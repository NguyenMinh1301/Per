package com.per.category.repository;

import com.per.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Category> search(String name, Pageable pageable);

}
