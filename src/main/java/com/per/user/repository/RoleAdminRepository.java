package com.per.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;

public interface RoleAdminRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
