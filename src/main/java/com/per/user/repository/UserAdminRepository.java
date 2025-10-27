package com.per.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.per.user.entity.User;

public interface UserAdminRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    @Query(
            """
			SELECT u FROM User u
			WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
			""")
    Page<User> search(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    Page<User> findAllBy(Pageable pageable);
}
