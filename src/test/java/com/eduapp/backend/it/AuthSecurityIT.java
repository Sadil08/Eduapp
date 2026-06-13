package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end verification of the WP-1 security fixes over real HTTP against the
 * scratch database. These are the "must never regress" security scenarios.
 */
class AuthSecurityIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    UserRepository userRepository;

    private Map<String, Object> registerBody(String email, String role) {
        // username is unique in the schema — derive it from the unique email.
        String username = "user-" + email.substring(0, email.indexOf('@'));
        return Map.of("email", email, "password", "pw12345678", "name", username, "role", role);
    }

    @Test
    void register_requestingAdminRole_isDowngradedToStudent_overHttp() {
        String email = "esc-" + UUID.randomUUID() + "@test.local";

        ResponseEntity<Map> resp = rest.postForEntity("/api/auth/register",
                registerBody(email, "ADMIN"), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Response DTO reports STUDENT, not ADMIN.
        assertThat(resp.getBody().get("role")).isEqualTo("STUDENT");
        // And the persisted row is STUDENT — privilege escalation is impossible.
        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void register_responseNeverContainsPasswordHash() {
        String email = "leak-" + UUID.randomUUID() + "@test.local";

        ResponseEntity<String> resp = rest.postForEntity("/api/auth/register",
                registerBody(email, "STUDENT"), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().toLowerCase()).doesNotContain("password");
    }

    @Test
    void adminRevenueEndpoint_isNotReachableAnonymously() {
        ResponseEntity<String> resp = rest.getForEntity("/api/admin/dashboard/revenue", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void aiAnalysesEndpoint_isNotReachableAnonymously() {
        ResponseEntity<String> resp = rest.getForEntity("/api/ai-analyses", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
