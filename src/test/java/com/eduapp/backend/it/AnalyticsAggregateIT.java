package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * N-SC3 — user analytics is served by a single paginated/aggregate DB query
 * ({@code findUserAnalyticsOptimized}), not by materialising every user. This verifies the
 * live endpoints return correct numbers over seeded fixtures and a proper Page shape.
 */
class AnalyticsAggregateIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User createUser(Role role, String unique) {
        User user = new User(unique + "@an.test", "x", unique);
        user.setRole(role);
        user.setReferralCode("AN" + unique);
        return userRepository.save(user);
    }

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(),
                u.getReferralCode(), u.getTokenVersion()));
        return h;
    }

    @Test
    void userAnalytics_isPaginated_andTotalMatchesUserCount() {
        User admin = createUser(Role.ADMIN, "anadmin-" + UUID.randomUUID().toString().substring(0, 6));
        createUser(Role.STUDENT, "anstu1-" + UUID.randomUUID().toString().substring(0, 6));
        createUser(Role.STUDENT, "anstu2-" + UUID.randomUUID().toString().substring(0, 6));
        long expectedTotal = userRepository.count();

        ResponseEntity<Map> resp = rest.exchange("/api/admin/analytics/users?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(bearer(admin)), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = resp.getBody();
        assertThat(body).containsKey("content").containsKey("totalElements");
        // The aggregate query counts every user, not just the page.
        assertThat(((Number) body.get("totalElements")).longValue()).isEqualTo(expectedTotal);
    }

    @Test
    void userAnalytics_searchTerm_narrowsResults() {
        User admin = createUser(Role.ADMIN, "anadmin2-" + UUID.randomUUID().toString().substring(0, 6));
        String uniq = "needle" + UUID.randomUUID().toString().substring(0, 8);
        createUser(Role.STUDENT, uniq);

        ResponseEntity<Map> resp = rest.exchange("/api/admin/analytics/users?page=0&size=10&searchTerm=" + uniq,
                HttpMethod.GET, new HttpEntity<>(bearer(admin)), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content = (List<?>) resp.getBody().get("content");
        assertThat(content).hasSize(1);
    }

    @Test
    void overview_returnsTotalsAndIsAdminOnly() {
        User admin = createUser(Role.ADMIN, "anadmin3-" + UUID.randomUUID().toString().substring(0, 6));

        ResponseEntity<Map> ok = rest.exchange("/api/admin/analytics/overview", HttpMethod.GET,
                new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        // getOverview() is @Cacheable, so the exact count can be stale here; the precise
        // user-count assertion lives in userAnalytics_isPaginated_andTotalMatchesUserCount
        // (the non-cached /users endpoint). Here we just check it computes a valid total.
        assertThat(((Number) ok.getBody().get("totalUsers")).longValue()).isGreaterThanOrEqualTo(1L);

        User student = createUser(Role.STUDENT, "anstud-" + UUID.randomUUID().toString().substring(0, 6));
        ResponseEntity<String> denied = rest.exchange("/api/admin/analytics/overview", HttpMethod.GET,
                new HttpEntity<>(bearer(student)), String.class);
        assertThat(denied.getStatusCode().value()).isIn(401, 403);
    }
}
