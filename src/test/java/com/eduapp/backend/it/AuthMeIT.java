package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.School;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** FE-A1.1 — /api/auth/me returns the server-validated profile; requires a valid token. */
class AuthMeIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired SchoolRepository schoolRepository;
    @Autowired JwtUtil jwtUtil;

    @Test
    void me_returnsProfile_forAuthenticatedSchoolAdmin() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        School s = schoolRepository.save(new School("Me " + u, "LK", "me" + u + "@s.test"));
        User user = new User("me-" + u + "@t.test", "x", "me-" + u);
        user.setRole(Role.SCHOOL_ADMIN);
        user.setSchool(s);
        user.setReferralCode("ME" + u);
        user = userRepository.save(user);

        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(),
                user.getReferralCode(), user.getTokenVersion()));
        ResponseEntity<Map> resp = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(h), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("email")).isEqualTo(user.getEmail());
        assertThat(resp.getBody().get("role")).isEqualTo("SCHOOL_ADMIN");
        assertThat(((Number) resp.getBody().get("schoolId")).longValue()).isEqualTo(s.getId());
    }

    @Test
    void me_returns401_withoutToken() {
        ResponseEntity<String> resp = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
