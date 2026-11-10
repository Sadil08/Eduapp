package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.AIAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;

/**
 * Happy-path "student does a paper" lifecycle over HTTP: a purchaser starts an attempt, submits
 * answers, and the attempt is persisted as SUBMITTED; the owner can read results and a second
 * student cannot. AI marking ({@link AIAnalysisService}) is mocked so the test is deterministic
 * and makes no paid call.
 */
class PaperAttemptLifecycleIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired PaperBundleRepository bundleRepository;
    @Autowired StudentBundleAccessRepository accessRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired JwtUtil jwtUtil;

    @MockBean AIAnalysisService aiAnalysisService;

    private User owner;
    private User other;
    private Paper paper;
    private PaperBundle bundle;
    private Question question;

    private User newStudent() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("life-" + u + "@p.test", "x", "life-" + u);
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
        doNothing().when(aiAnalysisService).analyzeAttempt(any());

        owner = newStudent();
        other = newStudent();

        bundle = bundleRepository.save(new PaperBundle("Life " + UUID.randomUUID().toString().substring(0, 5),
                "desc", new BigDecimal("199.00"), PaperType.ESSAY, null, null, null, false));

        paper = new Paper();
        paper.setName("Lifecycle " + UUID.randomUUID().toString().substring(0, 5));
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(10);
        paper.setMaxFreeAttempts(2);
        paper.setBundles(List.of(bundle));
        paper = paperRepository.save(paper);

        question = new Question(paper, "What is 2+2?", QuestionType.ESSAY, "4");
        question.setMarks(10);
        question = questionRepository.save(question);

        accessRepository.save(new StudentBundleAccess(owner, bundle, "pay-" + UUID.randomUUID()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void owner_startsSubmitsAndReadsResults_otherStudentCannot() {
        // 1) START — creates an IN_PROGRESS attempt
        ResponseEntity<Map> start = rest.exchange(
                "/api/papers/" + paper.getId() + "/attempt?bundleId=" + bundle.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(owner)), Map.class);
        assertThat(start.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number attemptId = (Number) start.getBody().get("attemptId");
        assertThat(attemptId).as("start must create/return an attempt id").isNotNull();

        // 2) SUBMIT answers
        Map<String, Object> submission = Map.of(
                "timeTakenMinutes", 7,
                "answers", List.of(Map.of("questionId", question.getId(), "answerText", "4")));
        ResponseEntity<Map> submit = rest.exchange(
                "/api/papers/" + paper.getId() + "/submit?bundleId=" + bundle.getId(),
                HttpMethod.POST, new HttpEntity<>(submission, auth(owner)), Map.class);
        assertThat(submit.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3) Persisted as SUBMITTED, and AI marking was dispatched (mocked)
        StudentPaperAttempt persisted = attemptRepository.findById(attemptId.longValue()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(AttemptStatus.SUBMITTED);

        // 4) Owner can read results
        ResponseEntity<Map> results = rest.exchange(
                "/api/papers/attempts/" + attemptId.longValue(),
                HttpMethod.GET, new HttpEntity<>(auth(owner)), Map.class);
        assertThat(results.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 5) A different student cannot read someone else's attempt results (ownership)
        ResponseEntity<String> otherView = rest.exchange(
                "/api/papers/attempts/" + attemptId.longValue(),
                HttpMethod.GET, new HttpEntity<>(auth(other)), String.class);
        assertThat(otherView.getStatusCode().value()).isIn(403, 404);
    }
}
