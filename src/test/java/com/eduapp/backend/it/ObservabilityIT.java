package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-A1.2 — health is public (probes); metrics/prometheus are ADMIN-only. */
class ObservabilityIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders auth(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@obs.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(), user.getReferralCode()));
        return h;
    }

    @Test
    void health_isPublic() {
        ResponseEntity<String> resp = rest.getForEntity("/actuator/health", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).contains("UP");
    }

    @Test
    void prometheus_isAdminOnly() {
        ResponseEntity<String> anon = rest.getForEntity("/actuator/prometheus", String.class);
        assertThat(anon.getStatusCode().value()).isIn(401, 403);

        ResponseEntity<String> student = rest.exchange("/actuator/prometheus", HttpMethod.GET,
                new HttpEntity<>(auth(Role.STUDENT)), String.class);
        assertThat(student.getStatusCode().value()).isIn(401, 403);

        ResponseEntity<String> admin = rest.exchange("/actuator/prometheus", HttpMethod.GET,
                new HttpEntity<>(auth(Role.ADMIN)), String.class);
        assertThat(admin.getStatusCode().value()).isEqualTo(200);
        assertThat(admin.getBody()).contains("# HELP"); // Prometheus exposition format
    }
}
