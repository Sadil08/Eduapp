package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * N-S9 — the entity-CRUD scaffolding (Cart/Notification/Progress/LeaderboardEntry) no longer
 * lets a non-admin create/update/delete arbitrary rows (mass-assignment + cross-user writes).
 * The user-facing scoped reads still work.
 */
class NS9AuthorizationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User student() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("s9-" + u + "@t.test", "x", "s9-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("S9" + u);
        return userRepository.save(user);
    }

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(),
                u.getReferralCode(), u.getTokenVersion()));
        return h;
    }

    private int post(String path, User u) {
        return rest.exchange(path, HttpMethod.POST,
                new HttpEntity<>(Map.of(), bearer(u)), String.class).getStatusCode().value();
    }

    @Test
    void student_cannotWriteEntityCrudEndpoints() {
        User s = student();
        assertThat(post("/api/notifications", s)).isIn(401, 403);
        assertThat(post("/api/progress", s)).isIn(401, 403);
        assertThat(post("/api/carts", s)).isIn(401, 403);
        // whole leaderboard-entries controller is ADMIN-only
        assertThat(rest.exchange("/api/leaderboard-entries", HttpMethod.GET,
                new HttpEntity<>(bearer(s)), String.class).getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void student_canStillReadOwnScopedLists() {
        User s = student();
        // getMy* lists remain accessible to the authenticated user.
        assertThat(rest.exchange("/api/notifications", HttpMethod.GET,
                new HttpEntity<>(bearer(s)), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.exchange("/api/progress", HttpMethod.GET,
                new HttpEntity<>(bearer(s)), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.exchange("/api/carts", HttpMethod.GET,
                new HttpEntity<>(bearer(s)), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
