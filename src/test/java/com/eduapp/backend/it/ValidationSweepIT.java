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

/** N-S9 — @Valid + DTO constraints now reject malformed payloads on non-auth endpoints too. */
class ValidationSweepIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders schoolAdmin() {
        School s = schoolRepository.save(new School("Val " + UUID.randomUUID().toString().substring(0, 5), "LK", "v@s.test"));
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("va-" + u + "@t.test", "x", "va-" + u);
        user.setRole(Role.SCHOOL_ADMIN);
        user.setSchool(s);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(), user.getReferralCode()));
        return h;
    }

    @Test
    void createSchoolPaper_withBlankName_returns400() {
        ResponseEntity<Map> resp = rest.exchange("/api/school/papers", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "", "type", "ESSAY"), schoolAdmin()), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsKey("fields");
    }

    @Test
    void createTeacherInvite_withInvalidEmail_returns400() {
        ResponseEntity<Map> resp = rest.exchange("/api/school/invites", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", "not-an-email"), schoolAdmin()), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
