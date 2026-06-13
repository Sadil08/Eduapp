package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** WP-8 — cohort analytics: aggregate correctness, effective-mark use, caching, tenant isolation. */
class SchoolAnalyticsIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired SchoolPaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired SchoolPaperAttemptRepository attemptRepository;
    @Autowired SchoolStudentAnswerRepository answerRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Cache c = cacheManager.getCache("schoolPaperSummary");
        if (c != null) c.clear();
    }

    private User teacher(School s) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("t-" + u + "@an.test", "x", "t-" + u);
        user.setRole(Role.TEACHER);
        user.setSchool(s);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    private void attempt(School s, SchoolPaper p, Long questionId, int aiMark, Integer overrideMark, int answerMarks) {
        long studentId = (long) (Math.random() * 1_000_000) + 1;
        SchoolPaperAttempt a = new SchoolPaperAttempt(s.getId(), studentId, p.getId(), p.getClassId());
        a.setStatus(overrideMark != null ? AttemptStatus.GRADED : AttemptStatus.SUBMITTED);
        a.setAiMark(aiMark);
        a.setTeacherOverrideMark(overrideMark);
        a = attemptRepository.save(a);
        SchoolStudentAnswer ans = new SchoolStudentAnswer(a.getId(), questionId, "x");
        ans.setMarksAwarded(answerMarks);
        answerRepository.save(ans);
    }

    private record Setup(School school, SchoolPaper paper, User teacher, Long questionId) {}

    private Setup seedCohort() {
        School school = schoolRepository.save(new School("An " + UUID.randomUUID().toString().substring(0, 5), "LK", "a@s.test"));
        SchoolClass cls = classRepository.save(new SchoolClass(school.getId(), null, "C", "S", "G10", "AN-" + UUID.randomUUID().toString().substring(0, 6)));
        User t = teacher(school);
        SchoolPaper paper = new SchoolPaper(school.getId(), "Analytics Paper", null, PaperType.MCQ, t.getId());
        paper.setClassId(cls.getId());
        paper.setStatus(SchoolPaperStatus.ASSIGNED);
        paper = paperRepository.save(paper);
        Question q = new Question();
        q.setSchoolPaperId(paper.getId());
        q.setText("Q1");
        q.setType(QuestionType.MCQ);
        q.setMarks(5);
        q = questionRepository.save(q);

        // 3 attempts: effective marks 5, 3 (overridden), 0. Answer marks 5, 5, 0.
        attempt(school, paper, q.getId(), 5, null, 5);
        attempt(school, paper, q.getId(), 5, 3, 5);
        attempt(school, paper, q.getId(), 0, null, 0);
        return new Setup(school, paper, t, q.getId());
    }

    @Test
    void summary_usesEffectiveMarks_andIsCached() {
        Setup s = seedCohort();

        ResponseEntity<Map> resp = rest.exchange("/api/school/analytics/papers/" + s.paper.getId() + "/summary",
                HttpMethod.GET, new HttpEntity<>(auth(s.teacher)), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = resp.getBody();
        assertThat(((Number) body.get("attemptCount")).longValue()).isEqualTo(3);
        assertThat(((Number) body.get("submittedCount")).longValue()).isEqualTo(3);
        // effective marks 5, 3, 0 -> avg 2.666..., high 5, low 0
        assertThat(((Number) body.get("averageMark")).doubleValue()).isCloseTo(2.6667, within(0.01));
        assertThat(((Number) body.get("highestMark")).intValue()).isEqualTo(5);
        assertThat(((Number) body.get("lowestMark")).intValue()).isEqualTo(0);

        // Cache populated under the tenant-keyed entry.
        Cache cache = cacheManager.getCache("schoolPaperSummary");
        assertThat(cache).isNotNull();
        assertThat(cache.get(s.school.getId() + ":" + s.paper.getId())).isNotNull();
    }

    @Test
    void questionAnalysis_reportsAveragesAndCorrectRate() {
        Setup s = seedCohort();

        ResponseEntity<java.util.List> resp = rest.exchange(
                "/api/school/analytics/papers/" + s.paper.getId() + "/questions",
                HttpMethod.GET, new HttpEntity<>(auth(s.teacher)), java.util.List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        java.util.List<Map<String, Object>> qs = resp.getBody();
        assertThat(qs).hasSize(1);
        Map<String, Object> q = qs.get(0);
        assertThat(((Number) q.get("answeredCount")).longValue()).isEqualTo(3);
        // answer marks 5,5,0 -> avg 3.333; correctRate 2/3 (two full-mark)
        assertThat(((Number) q.get("averageMarks")).doubleValue()).isCloseTo(3.333, within(0.01));
        assertThat(((Number) q.get("correctRate")).doubleValue()).isCloseTo(0.6667, within(0.01));
    }

    @Test
    void teacherOfOtherSchool_cannotReadAnalytics() {
        Setup s = seedCohort();
        School b = schoolRepository.save(new School("Other", "LK", "o@s.test"));
        User teacherB = teacher(b);

        ResponseEntity<String> resp = rest.exchange("/api/school/analytics/papers/" + s.paper.getId() + "/summary",
                HttpMethod.GET, new HttpEntity<>(auth(teacherB)), String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }
}
