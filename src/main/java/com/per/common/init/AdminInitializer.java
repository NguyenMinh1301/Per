package com.per.common.init;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.per.auth.entity.Role;
import com.per.auth.entity.RoleType;
import com.per.auth.repository.RoleRepository;
import com.per.auth.repository.UserRepository;
import com.per.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes default admin user on application startup if not exists. Admin credentials are read
 * from environment variables.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@per.com}")
    private String adminEmail;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.enabled:true}")
    private boolean adminCreationEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!adminCreationEnabled) {
            log.info("Admin auto-creation is disabled");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin username already exists: {}", adminUsername);
            return;
        }

        Role adminRole =
                roleRepository
                        .findByName(RoleType.ADMIN)
                        .orElseGet(
                                () -> {
                                    log.info("Creating ADMIN role...");
                                    Role role = new Role(RoleType.ADMIN, "Administrator role");
                                    return roleRepository.save(role);
                                });

        User admin =
                User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .firstName("Admin")
                        .lastName("User")
                        .emailVerified(true)
                        .active(true)
                        .roles(Set.of(adminRole))
                        .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", adminEmail);
    }
}
