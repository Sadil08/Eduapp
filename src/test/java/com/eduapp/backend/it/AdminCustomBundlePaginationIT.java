package com.eduapp.backend.it;

import com.eduapp.backend.model.CustomBundle;
import com.eduapp.backend.model.CustomBundleStatus;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.CustomBundleRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** SCALE-4 — admin custom-bundle queues (pending/approved) are paginated + ADMIN-only. */
class AdminCustomBundlePaginationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired CustomBundleRepository customBundleRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User user(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@cb.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    @Test
    void pendingAndApproved_arePagedAndAdminOnly() {
        User creator = user(Role.STUDENT);
        int pending = 13;
        for (int i = 0; i < pending; i++) {
            CustomBundle b = new CustomBundle(creator, "P-" + UUID.randomUUID(), "d");
            b.setStatus(CustomBundleStatus.PURCHASED);
            b.setPurchasedAt(LocalDateTime.now());
            customBundleRepository.save(b);
        }
        CustomBundle approved = new CustomBundle(creator, "A-" + UUID.randomUUID(), "d");
        approved.setStatus(CustomBundleStatus.APPROVED);
        approved.setApprovedAt(LocalDateTime.now());
        customBundleRepository.save(approved);

        // Student cannot reach the admin endpoint.
        ResponseEntity<String> denied = rest.exchange("/api/admin/custom-bundles/pending",
                HttpMethod.GET, new HttpEntity<>(auth(user(Role.STUDENT))), String.class);
        assertThat(denied.getStatusCode().value()).isIn(401, 403);

        HttpHeaders admin = auth(user(Role.ADMIN));

        ResponseEntity<Map> page0 = rest.exchange("/api/admin/custom-bundles/pending?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(admin), Map.class);
        assertThat(page0.getStatusCode().value()).isEqualTo(200);
        assertThat(((java.util.List<?>) page0.getBody().get("content"))).hasSize(10);
        assertThat(((Number) page0.getBody().get("totalElements")).longValue()).isGreaterThanOrEqualTo(pending);

        ResponseEntity<Map> stats = rest.exchange("/api/admin/custom-bundles/stats",
                HttpMethod.GET, new HttpEntity<>(admin), Map.class);
        assertThat(((Number) stats.getBody().get("pending")).longValue()).isGreaterThanOrEqualTo(pending);
        assertThat(((Number) stats.getBody().get("approved")).longValue()).isGreaterThanOrEqualTo(1);
    }
}
