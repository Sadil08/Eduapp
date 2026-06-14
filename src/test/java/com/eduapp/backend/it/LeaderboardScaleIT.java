package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** SCALE: leaderboard response is capped (top-N); the entries CRUD dump is ADMIN-only + paged. */
class LeaderboardScaleIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired OverallPaperAnalysisRepository overallRepository;
    @Autowired JwtUtil jwtUtil;

    private User user(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@lb.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    @Test
    void paperLeaderboard_respectsLimitCap() {
        Paper paper = new Paper();
        paper.setName("LB " + UUID.randomUUID().toString().substring(0, 5));
        paper.setType(PaperType.MCQ);
        paper.setTotalMarks(100);
        paper = paperRepository.save(paper);

        // 3 distinct students each with one opted-in, graded attempt.
        for (int i = 0; i < 3; i++) {
            User s = user(Role.STUDENT);
            StudentPaperAttempt a = new StudentPaperAttempt(s, paper, 1, LocalDateTime.now());
            a.setOptedIn(true);
            a.setTimeTakenMinutes(10 + i);
            a = attemptRepository.save(a);
            overallRepository.save(new OverallPaperAnalysis(a, 90 - i, null, null));
        }

        HttpHeaders headers = auth(user(Role.STUDENT));
        ResponseEntity<List> capped = rest.exchange("/api/leaderboard/paper/" + paper.getId() + "?limit=2",
                HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(capped.getStatusCode().value()).isEqualTo(200);
        assertThat(capped.getBody()).hasSize(2); // capped below the 3 available

        // limit above the hard cap is clamped (still succeeds, returns what exists).
        ResponseEntity<List> all = rest.exchange("/api/leaderboard/paper/" + paper.getId() + "?limit=9999",
                HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(all.getBody().size()).isLessThanOrEqualTo(100);
    }

    @Test
    void leaderboardEntriesDump_isAdminOnlyAndPaged() {
        ResponseEntity<String> student = rest.exchange("/api/leaderboard-entries",
                HttpMethod.GET, new HttpEntity<>(auth(user(Role.STUDENT))), String.class);
        assertThat(student.getStatusCode().value()).isIn(401, 403);

        ResponseEntity<java.util.Map> admin = rest.exchange("/api/leaderboard-entries?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(auth(user(Role.ADMIN))), java.util.Map.class);
        assertThat(admin.getStatusCode().value()).isEqualTo(200);
        assertThat(admin.getBody()).containsKey("totalElements");
    }
}
