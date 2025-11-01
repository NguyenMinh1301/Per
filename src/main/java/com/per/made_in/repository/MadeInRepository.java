package com.per.made_in.repository;

import com.per.made_in.entity.MadeIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MadeInRepository extends JpaRepository<MadeIn, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query(
            """
			SELECT m FROM MadeIn m
			WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(m.isoCode) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(m.region) LIKE LOWER(CONCAT('%', :query, '%'))
			""")
    Page<MadeIn> search(String name, Pageable pageable);
}
