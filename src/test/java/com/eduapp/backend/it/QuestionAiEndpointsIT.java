package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.AIService;
import com.eduapp.backend.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Covers the AI-assisted question-ingestion endpoints on {@code /api/questions}:
 * {@code extract-from-image}, {@code extract-batch} and {@code import-from-pdf}
 * (the "AI method" and "full PDF" admin flows).
 *
 * <p>{@link AIService} and {@link FileStorageService} are mocked so no real paid AI call or disk
 * write happens — the test exercises the HTTP wiring, not the model.
 *
 * <h3>SECURITY FINDING (Hole 1) — these tests are written to assert the SECURE contract</h3>
 * These three endpoints have no {@code @PreAuthorize} and are not under {@code /api/admin/**}, so
 * {@code SecurityConfig.anyRequest().authenticated()} lets ANY logged-in user (incl. a STUDENT)
 * trigger paid AI extraction / PDF import — a cost-abuse + data-tampering vector. The
 * {@code student_cannot*} / {@code anonymous_cannot*} tests below assert 401/403 and will FAIL
 * against the current code until the endpoints are locked to ADMIN. They intentionally pin the bug.
 */
class QuestionAiEndpointsIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    @MockBean AIService aiService;
    @MockBean FileStorageService fileStorageService;

    private User admin;
    private User student;

    private User newUser(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@ai.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User user, MediaType contentType) {
        HttpHeaders h = new HttpHeaders();
        if (user != null) {
            h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(),
                    user.getReferralCode()));
        }
        if (contentType != null) {
            h.setContentType(contentType);
        }
        return h;
    }

    private ByteArrayResource filePart(String name, byte[] bytes) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return name;
            }
        };
    }

    @BeforeEach
    void setUp() throws Exception {
        admin = newUser(Role.ADMIN);
        student = newUser(Role.STUDENT);

        when(fileStorageService.storeFile(any(), any())).thenReturn("/files/questions/stored.png");
        when(fileStorageService.storeBytes(any(), any(), any())).thenReturn("/files/questions/page.png");
        when(aiService.extractTextFromImage(any(), any(), any())).thenReturn("extracted question text");
        when(aiService.extractTextFromImagesBatch(any(), any()))
                .thenReturn(Map.of("q1", "text-1", "q2", "text-2"));
        when(aiService.importFromPdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Map.of("questions", List.of(Map.of("text", "Q1", "marks", 2))));
    }

    // ---------- extract-from-image ----------

    @Test
    void extractFromImage_asAdmin_returnsExtractedText() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart("q.png", new byte[] {1, 2, 3}));
        ResponseEntity<Map> resp = rest.exchange("/api/questions/extract-from-image", HttpMethod.POST,
                new HttpEntity<>(body, auth(admin, MediaType.MULTIPART_FORM_DATA)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("extractedText", "extracted question text");
    }

    @Test
    void extractFromImage_asStudent_isForbidden() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart("q.png", new byte[] {1, 2, 3}));
        ResponseEntity<String> resp = rest.exchange("/api/questions/extract-from-image", HttpMethod.POST,
                new HttpEntity<>(body, auth(student, MediaType.MULTIPART_FORM_DATA)), String.class);
        assertThat(resp.getStatusCode().value())
                .as("paid AI extraction must be ADMIN-only (Hole 1)")
                .isIn(401, 403);
    }

    @Test
    void extractFromImage_anonymous_isUnauthorized() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart("q.png", new byte[] {1, 2, 3}));
        ResponseEntity<String> resp = rest.exchange("/api/questions/extract-from-image", HttpMethod.POST,
                new HttpEntity<>(body, auth(null, MediaType.MULTIPART_FORM_DATA)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    // ---------- extract-batch ----------

    @Test
    void extractBatch_asAdmin_returnsResults() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", filePart("a.png", new byte[] {1}));
        body.add("files", filePart("b.png", new byte[] {2}));
        body.add("ids", "q1,q2");
        ResponseEntity<Map> resp = rest.exchange("/api/questions/extract-batch", HttpMethod.POST,
                new HttpEntity<>(body, auth(admin, MediaType.MULTIPART_FORM_DATA)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("totalProcessed", 2);
    }

    @Test
    void extractBatch_withMismatchedIds_returns400() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", filePart("a.png", new byte[] {1}));
        body.add("files", filePart("b.png", new byte[] {2}));
        body.add("ids", "only-one-id");
        ResponseEntity<Map> resp = rest.exchange("/api/questions/extract-batch", HttpMethod.POST,
                new HttpEntity<>(body, auth(admin, MediaType.MULTIPART_FORM_DATA)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void extractBatch_asStudent_isForbidden() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", filePart("a.png", new byte[] {1}));
        body.add("ids", "q1");
        ResponseEntity<String> resp = rest.exchange("/api/questions/extract-batch", HttpMethod.POST,
                new HttpEntity<>(body, auth(student, MediaType.MULTIPART_FORM_DATA)), String.class);
        assertThat(resp.getStatusCode().value())
                .as("batch AI extraction must be ADMIN-only (Hole 1)")
                .isIn(401, 403);
    }

    // ---------- import-from-pdf ----------

    @Test
    void importFromPdf_asAdmin_returnsParsedQuestions() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("questionPaper", filePart("paper.pdf", new byte[] {37, 80, 68, 70}));
        ResponseEntity<Map> resp = rest.exchange("/api/questions/import-from-pdf", HttpMethod.POST,
                new HttpEntity<>(body, auth(admin, MediaType.MULTIPART_FORM_DATA)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("questions");
    }

    @Test
    void importFromPdf_asStudent_isForbidden() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("questionPaper", filePart("paper.pdf", new byte[] {37, 80, 68, 70}));
        ResponseEntity<String> resp = rest.exchange("/api/questions/import-from-pdf", HttpMethod.POST,
                new HttpEntity<>(body, auth(student, MediaType.MULTIPART_FORM_DATA)), String.class);
        assertThat(resp.getStatusCode().value())
                .as("PDF import (paid AI) must be ADMIN-only (Hole 1)")
                .isIn(401, 403);
    }

    @Test
    void importFromPdf_anonymous_isUnauthorized() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("questionPaper", filePart("paper.pdf", new byte[] {37, 80, 68, 70}));
        ResponseEntity<String> resp = rest.exchange("/api/questions/import-from-pdf", HttpMethod.POST,
                new HttpEntity<>(body, auth(null, MediaType.MULTIPART_FORM_DATA)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    // ---------- question CRUD lockdown (answer-leak surface) ----------

    @Test
    void createQuestion_asStudent_isForbidden() {
        HttpHeaders h = auth(student, MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest.exchange("/api/questions", HttpMethod.POST,
                new HttpEntity<>("{\"text\":\"x\",\"type\":\"ESSAY\"}", h), String.class);
        assertThat(resp.getStatusCode().value())
                .as("students must not create questions (Hole 1)")
                .isIn(401, 403);
    }

    @Test
    void getQuestionById_asStudent_isForbidden() {
        // The full question DTO includes correctAnswerText — students must never read it directly.
        ResponseEntity<String> resp = rest.exchange("/api/questions/1", HttpMethod.GET,
                new HttpEntity<>(auth(student, null)), String.class);
        assertThat(resp.getStatusCode().value())
                .as("students must not read raw questions with model answers (Hole 1)")
                .isIn(401, 403);
    }

    @Test
    void deleteQuestion_asStudent_isForbidden() {
        ResponseEntity<String> resp = rest.exchange("/api/questions/1", HttpMethod.DELETE,
                new HttpEntity<>(auth(student, null)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
