package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The admin "add paper to bundle / remove paper from bundle" composition endpoints
 * ({@code POST|DELETE /api/admin/bundles/{bundleId}/papers/{paperId}}). Verifies the happy path
 * actually links/unlinks, that the endpoints are ADMIN-only, and that missing ids return 404.
 */
class AdminBundleCompositionIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired PaperBundleRepository bundleRepository;
    @Autowired PlatformTransactionManager txManager;
    @Autowired JwtUtil jwtUtil;

    private User admin;
    private User student;
    private Paper paper;
    private PaperBundle bundle;

    private User newUser(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@c.test", "x", role + "-" + u);
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
        admin = newUser(Role.ADMIN);
        student = newUser(Role.STUDENT);

        bundle = bundleRepository.save(new PaperBundle("Comp " + UUID.randomUUID().toString().substring(0, 5),
                "desc", new BigDecimal("100.00"), PaperType.ESSAY, null, null, null, false));

        paper = new Paper();
        paper.setName("Comp paper " + UUID.randomUUID().toString().substring(0, 5));
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(50);
        paper = paperRepository.save(paper);
    }

    private String compositionUrl() {
        return "/api/admin/bundles/" + bundle.getId() + "/papers/" + paper.getId();
    }

    /** Reads the paper's bundle ids inside a transaction so the lazy collection initialises. */
    private java.util.List<Long> bundleIdsOfPaper() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return tx.execute(status -> paperRepository.findById(paper.getId()).orElseThrow()
                .getBundles().stream().map(PaperBundle::getId).toList());
    }

    @Test
    void admin_addsPaperToBundle_thenRemoves() {
        ResponseEntity<String> add = rest.exchange(compositionUrl(), HttpMethod.POST,
                new HttpEntity<>(auth(admin)), String.class);
        assertThat(add.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(bundleIdsOfPaper()).contains(bundle.getId());

        ResponseEntity<String> remove = rest.exchange(compositionUrl(), HttpMethod.DELETE,
                new HttpEntity<>(auth(admin)), String.class);
        assertThat(remove.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(bundleIdsOfPaper()).doesNotContain(bundle.getId());
    }

    @Test
    void student_cannotAddPaperToBundle() {
        ResponseEntity<String> resp = rest.exchange(compositionUrl(), HttpMethod.POST,
                new HttpEntity<>(auth(student)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void admin_addNonexistentPaper_returns404() {
        ResponseEntity<String> resp = rest.exchange(
                "/api/admin/bundles/" + bundle.getId() + "/papers/99999999",
                HttpMethod.POST, new HttpEntity<>(auth(admin)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
