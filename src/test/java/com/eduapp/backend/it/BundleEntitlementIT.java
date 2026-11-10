package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bundle entitlement: a student must have PURCHASED the parent bundle before they can attempt a
 * paper inside it. Exercised end-to-end over HTTP with a paper linked to a paid bundle.
 *
 * <h3>SECURITY FINDING (Hole 2) — paywall bypass on the attempt-START path</h3>
 * {@code PaperService.checkAttemptLimit(...)} (reached via {@code GET /api/papers/{id}/attempt})
 * never verifies {@code StudentBundleAccess} for a regular bundle — it only counts attempts. So an
 * unpaid student can START an attempt and read the paper's questions for free. The SUBMIT path
 * ({@code POST /api/papers/{id}/submit}) DOES gate on purchase
 * ({@code existsByStudentIdAndBundleId}), so the gap is an asymmetry: content is reachable without
 * paying even though marks aren't. {@code nonOwner_cannotStartAttempt} asserts the secure contract
 * (403) and will FAIL until the START path enforces the same check the SUBMIT path already does.
 */
class BundleEntitlementIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired PaperBundleRepository bundleRepository;
    @Autowired StudentBundleAccessRepository accessRepository;
    @Autowired JwtUtil jwtUtil;

    private User owner;     // purchased the bundle
    private User nonOwner;  // never purchased
    private Paper paper;
    private PaperBundle bundle;

    private User newStudent() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("ent-" + u + "@b.test", "x", "ent-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User user) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(),
                user.getReferralCode()));
        return h;
    }

    @BeforeEach
    void setUp() {
        owner = newStudent();
        nonOwner = newStudent();

        bundle = new PaperBundle("Bundle " + UUID.randomUUID().toString().substring(0, 5),
                "desc", new BigDecimal("499.00"), PaperType.ESSAY, null, null, null, false);
        bundle = bundleRepository.save(bundle);

        paper = new Paper();
        paper.setName("Entitled " + UUID.randomUUID().toString().substring(0, 5));
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(100);
        paper.setMaxFreeAttempts(2);
        paper.setBundles(List.of(bundle));
        paper = paperRepository.save(paper);

        Question q = new Question(paper, "Explain photosynthesis.", QuestionType.ESSAY, "model answer");
        q.setMarks(10);
        questionRepository.save(q);

        // Only the owner gets an access row.
        accessRepository.save(new StudentBundleAccess(owner, bundle, "pay-" + UUID.randomUUID()));
    }

    private String attemptUrl() {
        return "/api/papers/" + paper.getId() + "/attempt?bundleId=" + bundle.getId();
    }

    @Test
    void owner_canStartAttempt() {
        ResponseEntity<String> resp = rest.exchange(attemptUrl(), HttpMethod.GET,
                new HttpEntity<>(auth(owner)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void nonOwner_cannotStartAttempt() {
        ResponseEntity<String> resp = rest.exchange(attemptUrl(), HttpMethod.GET,
                new HttpEntity<>(auth(nonOwner)), String.class);
        assertThat(resp.getStatusCode().value())
                .as("unpaid student must not be able to start an attempt / read paper content (Hole 2)")
                .isEqualTo(403);
    }

    @Test
    void nonOwner_cannotSubmit() {
        // The submit path already enforces purchase — this should pass (green) today.
        Map<String, Object> submission = Map.of("answers", List.of(), "timeTakenMinutes", 5);
        ResponseEntity<String> resp = rest.exchange(
                "/api/papers/" + paper.getId() + "/submit?bundleId=" + bundle.getId(),
                HttpMethod.POST, new HttpEntity<>(submission, auth(nonOwner)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void attempt_withoutAnyBundleContext_returns400() {
        ResponseEntity<String> resp = rest.exchange("/api/papers/" + paper.getId() + "/attempt",
                HttpMethod.GET, new HttpEntity<>(auth(owner)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
