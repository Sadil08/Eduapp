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

/**
 * SEC-TOKEN-1 (C4) token revocation + (C3) security headers, end-to-end.
 *
 * <p>C4: "log out everywhere" bumps token_version so a token issued before it is rejected — the
 * mitigation for a token suspected stolen from localStorage. C3: the API emits defense-in-depth
 * security headers on every response.
 */
class TokenRevocationAndHeadersIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    @Test
    void logoutAll_revokesPreviouslyIssuedTokens() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("revoke-" + u + "@t.test", "x", "revoke-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);

        // Token minted at the current version (0).
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(),
                user.getReferralCode());

        // Valid before logout-all.
        assertThat(rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(bearer(token)), String.class)
                .getStatusCode().value()).isEqualTo(200);

        // Log out everywhere → bumps token_version.
        assertThat(rest.exchange("/api/auth/logout-all", HttpMethod.POST, new HttpEntity<>(bearer(token)), Void.class)
                .getStatusCode().value()).isEqualTo(204);

        // The SAME (now stale) token is rejected (this app answers unauthenticated with 401 or 403).
        assertThat(rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(bearer(token)), String.class)
                .getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void api_emitsSecurityHeaders() {
        // exam-types is a public GET — no auth/rate-limit-auth tier involved.
        ResponseEntity<String> resp = rest.getForEntity("/api/exam-types", String.class);
        HttpHeaders headers = resp.getHeaders();

        assertThat(headers.getFirst("Content-Security-Policy"))
                .contains("frame-ancestors 'none'");
        assertThat(headers.getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(headers.getFirst("Referrer-Policy")).isEqualTo("no-referrer");
    }
}
