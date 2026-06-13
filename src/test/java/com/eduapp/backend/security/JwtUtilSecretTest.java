package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WP-1.2 — JWT secret is externalised and validated. App refuses a missing/short
 * secret (fail-fast) and round-trips a token with a valid one.
 */
class JwtUtilSecretTest {

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> new JwtUtil(""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtUtil("tooshort"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void acceptsValidSecretAndRoundTripsToken() {
        JwtUtil jwt = new JwtUtil("0123456789012345678901234567890123456789"); // 40 bytes
        String token = jwt.generateToken("a@b.com", Role.STUDENT, 1L, "REF123");

        assertThat(jwt.extractEmail(token)).isEqualTo("a@b.com");
        assertThat(jwt.extractRole(token)).isEqualTo(Role.STUDENT);
        assertThat(jwt.isTokenExpired(token)).isFalse();
    }
}
