package com.eduapp.backend.it;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.PaperRepository;
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
 * SCALE-3 — the paper picker must page + search server-side (never load the whole table).
 */
class PaperSearchIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired PaperRepository paperRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders authStudent() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("stu-" + u + "@p.test", "x", "stu-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(), user.getReferralCode()));
        return h;
    }

    private void newPaper(String name) {
        Paper p = new Paper();
        p.setName(name);
        p.setType(PaperType.ESSAY);
        paperRepository.save(p);
    }

    @Test
    void search_isPagedAndCapped_andFiltersByName() {
        String tag = "ZX" + UUID.randomUUID().toString().substring(0, 4);
        for (int i = 0; i < 25; i++) newPaper(tag + " Physics Paper " + i);
        newPaper("Unrelated Chemistry " + UUID.randomUUID());

        HttpHeaders headers = authStudent();

        // Page 0, size 10 -> 10 of the 25 matches; totalElements = 25.
        ResponseEntity<Map> p0 = rest.exchange("/api/papers/search?q=" + tag + "&page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(p0.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) p0.getBody().get("totalElements")).longValue()).isEqualTo(25);
        assertThat(((java.util.List<?>) p0.getBody().get("content"))).hasSize(10);

        // size beyond the cap (1000) is clamped to <= 100, so it can't return everything.
        ResponseEntity<Map> big = rest.exchange("/api/papers/search?q=" + tag + "&page=0&size=1000",
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(((Number) big.getBody().get("size")).intValue()).isLessThanOrEqualTo(100);
    }
}
