package com.per.common.init.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.auth.repository.RoleRepository;
import com.per.user.entity.User;
import com.per.user.repository.UserAdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Initializes required roles and admin account on application startup. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserAdminRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminAccount();
    }

    private void initializeRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByName(roleType).isEmpty()) {
                Role role = new Role(roleType, roleType.name() + " role");
                roleRepository.save(role);
                log.info("Created role: {}", roleType);
            }
        }
    }

    private void initializeAdminAccount() {
        String adminUsername = env.getProperty("APP_ADMIN_USERNAME", "admin");
        String adminEmail = env.getProperty("APP_ADMIN_EMAIL", "admin@per.com");
        String adminPassword = env.getProperty("APP_ADMIN_PASSWORD", "admin123");

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin account '{}' already exists", adminUsername);
            return;
        }

        Role adminRole =
                roleRepository
                        .findByName(RoleType.ADMIN)
                        .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User admin =
                User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .firstName("Admin")
                        .lastName("User")
                        .emailVerified(true)
                        .active(true)
                        .build();
        admin.addRole(adminRole);

        userRepository.save(admin);
        log.info("Admin account '{}' created successfully", adminUsername);
    }
}
