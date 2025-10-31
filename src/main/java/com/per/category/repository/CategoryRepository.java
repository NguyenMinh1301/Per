package com.per.category.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.per.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query(
            """
			SELECT c FROM Category c
			WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
			""")
    Page<Category> search(String name, Pageable pageable);
}
