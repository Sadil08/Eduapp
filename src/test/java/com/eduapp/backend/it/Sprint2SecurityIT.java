package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.School;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprint 2 (deep-audit P1) JWT hardening:
 *  - N-S5: token-version revocation (a token issued before a version bump is rejected).
 *  - N-S6: issuer/audience binding (a token with a foreign iss/aud is rejected).
 */
class Sprint2SecurityIT extends AbstractIntegrationTest {

    // The real runtime secret, so a foreign-iss/aud token is signed VALIDLY and is rejected
    // purely on the iss/aud check (N-S6) — not on a signature mismatch.
    @Value("${JWT_SECRET}") String jwtSecret;

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User createTeacher() {
        School s = schoolRepository.save(new School("S2 " + UUID.randomUUID().toString().substring(0, 5), "LK", "s2@s.test"));
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("t2-" + u + "@t.test", "x", "t2-" + u);
        user.setRole(Role.TEACHER);
        user.setSchool(s);
        user.setReferralCode("R2" + u);
        return userRepository.save(user);
    }

    private ResponseEntity<String> callWithToken(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return rest.exchange("/api/school/classes", HttpMethod.GET, new HttpEntity<>(h), String.class);
    }

    @Test
    void validToken_isAccepted() {
        User teacher = createTeacher();
        String token = jwtUtil.generateToken(teacher.getEmail(), teacher.getRole(),
                teacher.getId(), teacher.getReferralCode(), teacher.getTokenVersion());
        assertThat(callWithToken(token).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void tokenBelowCurrentVersion_isRejected() {
        User teacher = createTeacher();
        // Mint a token at version 0, then bump the user's version (as a password reset would).
        String oldToken = jwtUtil.generateToken(teacher.getEmail(), teacher.getRole(),
                teacher.getId(), teacher.getReferralCode(), 0);
        teacher.incrementTokenVersion(); // -> 1
        userRepository.save(teacher);

        assertThat(callWithToken(oldToken).getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void tokenWithForeignIssuerAudience_isRejected() {
        User teacher = createTeacher();
        // Same signing key, but a different iss/aud (e.g. a token from another service).
        JwtUtil foreign = new JwtUtil(jwtSecret, "evil-issuer", "evil-audience");
        String token = foreign.generateToken(teacher.getEmail(), teacher.getRole(),
                teacher.getId(), teacher.getReferralCode(), teacher.getTokenVersion());

        assertThat(callWithToken(token).getStatusCode().value()).isIn(401, 403);
    }
}
