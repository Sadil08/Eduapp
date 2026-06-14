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
 * SCALE-4 + data-exposure: the "list all attempts/answers" endpoints used to return every
 * user's data to any authenticated caller. They are now ADMIN-only and paginated.
 */
class AttemptAnswerAdminListIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders auth(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@aa.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(), user.getReferralCode()));
        return h;
    }

    @Test
    void listAllAttempts_isAdminOnlyAndPaged() {
        ResponseEntity<String> student = rest.exchange("/api/student-paper-attempts",
                HttpMethod.GET, new HttpEntity<>(auth(Role.STUDENT)), String.class);
        assertThat(student.getStatusCode().value()).isIn(401, 403);

        ResponseEntity<Map> admin = rest.exchange("/api/student-paper-attempts?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(auth(Role.ADMIN)), Map.class);
        assertThat(admin.getStatusCode().value()).isEqualTo(200);
        assertThat(admin.getBody()).containsKey("totalElements");
        assertThat(admin.getBody()).containsKey("content");
    }

    @Test
    void listAllAnswers_isAdminOnlyAndPaged() {
        ResponseEntity<String> student = rest.exchange("/api/student-answers",
                HttpMethod.GET, new HttpEntity<>(auth(Role.STUDENT)), String.class);
        assertThat(student.getStatusCode().value()).isIn(401, 403);

        ResponseEntity<Map> admin = rest.exchange("/api/student-answers?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(auth(Role.ADMIN)), Map.class);
        assertThat(admin.getStatusCode().value()).isEqualTo(200);
        assertThat(admin.getBody()).containsKey("totalElements");
    }
}
