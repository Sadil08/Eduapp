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

/** BT-1 — ADMIN creates a School + its first SCHOOL_ADMIN; non-admins cannot. */
class AdminSchoolIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders bearer(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role.name().toLowerCase() + "-" + u + "@t.test", "x", "u-" + u);
        user.setRole(role);
        user.setReferralCode("RC" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(),
                user.getReferralCode(), user.getTokenVersion()));
        return h;
    }

    private Map<String, Object> body(String adminEmail) {
        return Map.of(
                "name", "Greenwood College",
                "country", "LK",
                "contactEmail", "office" + UUID.randomUUID().toString().substring(0, 5) + "@gw.test",
                "adminName", "Pat Principal",
                "adminEmail", adminEmail);
    }

    @Test
    void admin_createsSchool_andFirstSchoolAdmin() {
        String adminEmail = "head-" + UUID.randomUUID().toString().substring(0, 8) + "@gw.test";
        ResponseEntity<Map> resp = rest.exchange("/api/admin/schools", HttpMethod.POST,
                new HttpEntity<>(body(adminEmail), bearer(Role.ADMIN)), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long schoolId = ((Number) resp.getBody().get("id")).longValue();

        User created = userRepository.findByEmail(adminEmail).orElseThrow();
        assertThat(created.getRole()).isEqualTo(Role.SCHOOL_ADMIN);
        assertThat(created.getSchoolId()).isEqualTo(schoolId);
    }

    @Test
    void nonAdmin_cannotCreateSchool() {
        ResponseEntity<String> resp = rest.exchange("/api/admin/schools", HttpMethod.POST,
                new HttpEntity<>(body("x" + UUID.randomUUID() + "@gw.test"), bearer(Role.SCHOOL_ADMIN)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void duplicateAdminEmail_isRejected() {
        String dup = "dup-" + UUID.randomUUID().toString().substring(0, 8) + "@gw.test";
        rest.exchange("/api/admin/schools", HttpMethod.POST,
                new HttpEntity<>(body(dup), bearer(Role.ADMIN)), Map.class);
        ResponseEntity<String> second = rest.exchange("/api/admin/schools", HttpMethod.POST,
                new HttpEntity<>(body(dup), bearer(Role.ADMIN)), String.class);
        assertThat(second.getStatusCode().value()).isEqualTo(400);
    }
}
