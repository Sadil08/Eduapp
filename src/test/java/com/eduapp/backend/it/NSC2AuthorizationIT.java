package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.CartRepository;
import com.eduapp.backend.repository.NotificationRepository;
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
 * N-SC2-A — list endpoints no longer leak other users' rows, and unbounded admin lists are
 * locked down. Verified against the real security filter chain.
 */
class NSC2AuthorizationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired CartRepository cartRepository;
    @Autowired JwtUtil jwtUtil;

    private User createUser(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role.name().toLowerCase() + "-" + u + "@t.test", "x", "u-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(),
                u.getReferralCode(), u.getTokenVersion()));
        return h;
    }

    @Test
    void notifications_returnOnlyCallersOwn() {
        User u1 = createUser(Role.STUDENT);
        User u2 = createUser(Role.STUDENT);
        notificationRepository.save(new Notification(u1, "for u1", NotificationType.INFO));
        notificationRepository.save(new Notification(u2, "for u2", NotificationType.INFO));

        ResponseEntity<List> resp = rest.exchange("/api/notifications", HttpMethod.GET,
                new HttpEntity<>(bearer(u1)), List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> body = resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("message")).isEqualTo("for u1");
    }

    @Test
    void carts_returnOnlyCallersOwn() {
        User u1 = createUser(Role.STUDENT);
        User u2 = createUser(Role.STUDENT);
        cartRepository.save(new Cart(u2)); // someone else's cart

        ResponseEntity<List> resp = rest.exchange("/api/carts", HttpMethod.GET,
                new HttpEntity<>(bearer(u1)), List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty(); // u1 has no cart; must NOT see u2's
    }

    @Test
    void progress_isScopedToCaller_andNeverAllUsers() {
        User u1 = createUser(Role.STUDENT);
        ResponseEntity<List> resp = rest.exchange("/api/progress", HttpMethod.GET,
                new HttpEntity<>(bearer(u1)), List.class);
        // Fresh user -> own (empty) progress, not the whole table.
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    void adminPapers_isPaginated() {
        User admin = createUser(Role.ADMIN);
        ResponseEntity<Map> resp = rest.exchange("/api/papers?page=0&size=5", HttpMethod.GET,
                new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("content").containsKey("totalElements");
    }

    @Test
    void adminBundles_isPaginated() {
        User admin = createUser(Role.ADMIN);
        ResponseEntity<Map> resp = rest.exchange("/api/admin/bundles?page=0&size=5", HttpMethod.GET,
                new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("content").containsKey("totalElements");
    }

    @Test
    void allQuestions_isAdminOnly() {
        User student = createUser(Role.STUDENT);
        ResponseEntity<String> asStudent = rest.exchange("/api/questions", HttpMethod.GET,
                new HttpEntity<>(bearer(student)), String.class);
        assertThat(asStudent.getStatusCode().value()).isIn(401, 403);

        User admin = createUser(Role.ADMIN);
        ResponseEntity<Map> asAdmin = rest.exchange("/api/questions?page=0&size=5", HttpMethod.GET,
                new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(asAdmin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(asAdmin.getBody()).containsKey("content"); // Page shape
    }
}
