package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-IDOR (Sprint-0) end-to-end: the consumer attempt/answer endpoints must enforce object-level
 * ownership. A second student must not be able to read/delete another student's attempt or answer,
 * or trigger paid AI on it — the guard returns 404 (no enumeration oracle). The owner and an ADMIN
 * may access it.
 */
class AttemptAuthzIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired StudentAnswerRepository studentAnswerRepository;
    @Autowired JwtUtil jwtUtil;

    private User userA;
    private User userB;
    private User admin;
    private StudentPaperAttempt attemptOfA;
    private StudentAnswer answerOfA;

    private User newUser(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@idor.test", "x", role + "-" + u);
        user.setRole(role);
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
        userA = newUser(Role.STUDENT);
        userB = newUser(Role.STUDENT);
        admin = newUser(Role.ADMIN);

        Paper paper = new Paper();
        paper.setName("IDOR " + UUID.randomUUID().toString().substring(0, 5));
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(100);
        paper = paperRepository.save(paper);

        Question q = new Question(paper, "What is 2+2?", QuestionType.ESSAY, "4");
        q.setMarks(10);
        q = questionRepository.save(q);

        attemptOfA = attemptRepository.save(
                new StudentPaperAttempt(userA, paper, 1, LocalDateTime.now()));
        answerOfA = studentAnswerRepository.save(
                new StudentAnswer(attemptOfA, q, "four", null));
    }

    @Test
    void nonOwner_cannotReadAttempt() {
        ResponseEntity<String> resp = rest.exchange("/api/student-paper-attempts/" + attemptOfA.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(userB)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void nonOwner_cannotDeleteAttempt() {
        ResponseEntity<String> resp = rest.exchange("/api/student-paper-attempts/" + attemptOfA.getId(),
                HttpMethod.DELETE, new HttpEntity<>(auth(userB)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(attemptRepository.existsById(attemptOfA.getId())).isTrue(); // not deleted
    }

    @Test
    void nonOwner_cannotReadAnswer() {
        ResponseEntity<String> resp = rest.exchange("/api/student-answers/" + answerOfA.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(userB)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void nonOwner_cannotReadDrafts() {
        ResponseEntity<String> resp = rest.exchange(
                "/api/student-answers/attempts/" + attemptOfA.getId() + "/draft-answers",
                HttpMethod.GET, new HttpEntity<>(auth(userB)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void nonOwner_cannotTriggerRetryAnalysis() {
        // The paid-AI cost-abuse vector: must be blocked before any dispatch.
        ResponseEntity<String> resp = rest.exchange(
                "/api/student-answers/attempts/" + attemptOfA.getId() + "/retry-analysis",
                HttpMethod.POST, new HttpEntity<>(auth(userB)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void owner_canReadOwnAttempt() {
        ResponseEntity<String> resp = rest.exchange("/api/student-paper-attempts/" + attemptOfA.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(userA)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void admin_canReadAnyAttempt() {
        ResponseEntity<String> resp = rest.exchange("/api/student-paper-attempts/" + attemptOfA.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(admin)), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }
}
