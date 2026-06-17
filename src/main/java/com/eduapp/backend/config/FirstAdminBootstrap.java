package com.eduapp.backend.config;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * BT-2: first-admin bootstrap. On a cold system (no ADMIN exists yet), if BOOTSTRAP_ADMIN_EMAIL
 * is configured, provision exactly one ADMIN with a set-password OTP (never a default password).
 * Idempotent: does nothing once any ADMIN exists.
 */
@Component
public class FirstAdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(FirstAdminBootstrap.class);

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${bootstrap.admin-email:}")
    private String bootstrapAdminEmail;

    public FirstAdminBootstrap(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()) {
            return; // not configured
        }
        if (userRepository.existsByRole(Role.ADMIN)) {
            return; // already bootstrapped
        }
        try {
            userService.provisionUser(bootstrapAdminEmail.trim(), "Bootstrap Admin", Role.ADMIN, null);
            logger.warn("BOOTSTRAP: created first ADMIN {} — a set-password OTP was emailed. "
                    + "Complete setup immediately.", bootstrapAdminEmail);
        } catch (Exception e) {
            logger.error("BOOTSTRAP: failed to create first admin: {}", e.getMessage());
        }
    }
}
