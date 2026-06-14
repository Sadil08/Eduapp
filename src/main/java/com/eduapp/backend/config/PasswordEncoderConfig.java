package com.eduapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Exposes the {@link PasswordEncoder} bean. Renamed from {@code AuthConfig} (AMB-authconfig-naming):
 * it only configures password hashing — the security filter chain and authorization rules
 * live in {@code SecurityConfig}.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
