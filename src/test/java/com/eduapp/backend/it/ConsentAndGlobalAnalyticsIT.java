package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-9 — consent (incl. age gate) and the Cambridge aggregate API (consent filter, 50-floor, no school data). */
class ConsentAndGlobalAnalyticsIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired StudentConsentRecordRepository consentRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired OverallPaperAnalysisRepository overallRepository;
    @Autowired SchoolRepository schoolRepository;
    @Autowired JwtUtil jwtUtil;

    private User newUser(Role role, School school) {
        String u = UUID.randomUUID().toString().substring(0, 10);
        User user = new User(u + "@c.test", "x", "u-" + u);
        user.setRole(role);
        user.setSchool(school);
        user.setReferralCode("R" + u.substring(0, 7));
        return userRepository.save(user);
    }

    private HttpHeaders auth(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    private Paper newPaper() {
        Paper p = new Paper();
        p.setName("Global Paper " + UUID.randomUUID().toString().substring(0, 5));
        p.setType(PaperType.ESSAY);
        return paperRepository.save(p);
    }

    /** Creates a user (global unless school given) with a consent flag and a scored attempt on the paper. */
    private void seedScore(Paper paper, int score, boolean analyticsConsent, School school) {
        User u = newUser(school == null ? Role.STUDENT : Role.SCHOOL_STUDENT, school);
        StudentConsentRecord c = new StudentConsentRecord();
        c.setUserId(u.getId());
        c.setConsentVersion("v1");
        c.setAnalyticsSharingConsented(analyticsConsent);
        consentRepository.save(c);
        StudentPaperAttempt a = attemptRepository.save(new StudentPaperAttempt(u, paper, 1, LocalDateTime.now()));
        overallRepository.save(new OverallPaperAnalysis(a, score, null, null));
    }

    private User admin() {
        return newUser(Role.ADMIN, null);
    }

    private Map<String, Object> aggregate(Paper paper, User admin) {
        ResponseEntity<Map> resp = rest.exchange(
                "/api/analytics/global/papers/" + paper.getId() + "/score-aggregate",
                HttpMethod.GET, new HttpEntity<>(auth(admin)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    // ---------- Consent + age gate ----------

    @Test
    void recordingConsent_persistsChoice() {
        User user = newUser(Role.STUDENT, null);
        ResponseEntity<Map> resp = rest.exchange("/api/consent", HttpMethod.POST,
                new HttpEntity<>(Map.of("analyticsSharing", true, "dateOfBirth", "1995-01-01"), auth(user)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consentRepository.findByUserId(user.getId()).orElseThrow().isAnalyticsSharingConsented()).isTrue();
    }

    @Test
    void under16_withoutParentalConsent_isRejected() {
        User user = newUser(Role.STUDENT, null);
        String dob = LocalDate.now().minusYears(12).toString();
        ResponseEntity<String> resp = rest.exchange("/api/consent", HttpMethod.POST,
                new HttpEntity<>(Map.of("analyticsSharing", true, "dateOfBirth", dob), auth(user)), String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void under16_withParentalConsent_isAccepted() {
        User user = newUser(Role.STUDENT, null);
        String dob = LocalDate.now().minusYears(12).toString();
        ResponseEntity<Map> resp = rest.exchange("/api/consent", HttpMethod.POST,
                new HttpEntity<>(Map.of("analyticsSharing", true, "dateOfBirth", dob, "parentalConsentGranted", true), auth(user)), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ---------- Cambridge aggregate: 50-floor, consent filter, school exclusion ----------

    @Test
    void aggregate_isSuppressedBelow50_andReleasedAt50() {
        Paper paper = newPaper();
        User admin = admin();

        for (int i = 0; i < 49; i++) seedScore(paper, 10, true, null);
        assertThat(aggregate(paper, admin).get("suppressed")).isEqualTo(true);
        assertThat(aggregate(paper, admin).get("cohortSize")).isNull();

        seedScore(paper, 10, true, null); // 50th consenting student
        Map<String, Object> released = aggregate(paper, admin);
        assertThat(released.get("suppressed")).isEqualTo(false);
        assertThat(((Number) released.get("cohortSize")).longValue()).isEqualTo(50);
        assertThat(((Number) released.get("averageScore")).doubleValue()).isEqualTo(10.0);
    }

    @Test
    void aggregate_excludesNonConsentingAndSchoolTierStudents() {
        Paper paper = newPaper();
        User admin = admin();
        School school = schoolRepository.save(new School("Excluded School", "LK", "x@s.test"));

        for (int i = 0; i < 50; i++) seedScore(paper, 10, true, null);   // consenting global, score 10
        seedScore(paper, 100, false, null);                              // global but NOT consenting
        seedScore(paper, 100, true, school);                            // school-tier (must be excluded)

        Map<String, Object> agg = aggregate(paper, admin);
        // Only the 50 consenting global students count; the score-100 extras are excluded.
        assertThat(((Number) agg.get("cohortSize")).longValue()).isEqualTo(50);
        assertThat(((Number) agg.get("averageScore")).doubleValue()).isEqualTo(10.0);
        assertThat(((Number) agg.get("maxScore")).intValue()).isEqualTo(10);
    }

    @Test
    void globalAnalytics_isAdminOnly() {
        Paper paper = newPaper();
        User student = newUser(Role.STUDENT, null);
        ResponseEntity<String> resp = rest.exchange(
                "/api/analytics/global/papers/" + paper.getId() + "/score-aggregate",
                HttpMethod.GET, new HttpEntity<>(auth(student)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
