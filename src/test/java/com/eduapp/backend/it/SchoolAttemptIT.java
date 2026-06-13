package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-7 — school attempts: exam window, deterministic MCQ marking, teacher override, results gating. */
class SchoolAttemptIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired SchoolEnrolmentRepository enrolmentRepository;
    @Autowired SchoolPaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired SchoolPaperAttemptRepository attemptRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User user(Role role, School school) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@a.test", "x", role + "-" + u);
        user.setRole(role);
        user.setSchool(school);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders auth(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    /** Seeds an ASSIGNED MCQ paper (window open now) with one enrolled student. */
    private record Fixture(School school, SchoolClass cls, User teacher, User student, SchoolPaper paper, Question q) {}

    private Fixture seed(LocalDateTime windowStart, LocalDateTime windowEnd) {
        School school = schoolRepository.save(new School("Att " + UUID.randomUUID().toString().substring(0, 5), "LK", "a@s.test"));
        SchoolClass cls = classRepository.save(new SchoolClass(school.getId(), null, "C", "Sub", "G10", "AC-" + UUID.randomUUID().toString().substring(0, 6)));
        User teacher = user(Role.TEACHER, school);
        User student = user(Role.SCHOOL_STUDENT, school);
        enrolmentRepository.save(new SchoolEnrolment(school.getId(), cls.getId(), student.getId()));

        SchoolPaper paper = new SchoolPaper(school.getId(), "MCQ Paper", null, PaperType.MCQ, teacher.getId());
        paper.setClassId(cls.getId());
        paper.setStatus(SchoolPaperStatus.ASSIGNED);
        paper.setExamWindowStart(windowStart);
        paper.setExamWindowEnd(windowEnd);
        paper = paperRepository.save(paper);

        Question q = new Question();
        q.setSchoolPaperId(paper.getId());
        q.setText("2+2 = ?");
        q.setType(QuestionType.MCQ);
        q.setMarks(5);
        q.setCorrectAnswerText("4");
        q = questionRepository.save(q);
        return new Fixture(school, cls, teacher, student, paper, q);
    }

    @Test
    void student_sitsMcqPaper_aiMarkComputed_thenTeacherOverrides_thenResultsReleased() {
        Fixture f = seed(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(2));

        // Start
        ResponseEntity<Map> start = rest.exchange("/api/student/attempts/start", HttpMethod.POST,
                new HttpEntity<>(Map.of("paperId", f.paper.getId()), auth(f.student)), Map.class);
        assertThat(start.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer attemptId = (Integer) start.getBody().get("id");

        // Answer correctly + submit -> AI mark = 5 (rule-based MCQ)
        rest.exchange("/api/student/attempts/" + attemptId + "/answers", HttpMethod.POST,
                new HttpEntity<>(Map.of("questionId", f.q.getId(), "answerText", "4"), auth(f.student)), Void.class);
        ResponseEntity<Map> submit = rest.exchange("/api/student/attempts/" + attemptId + "/submit", HttpMethod.POST,
                new HttpEntity<>(auth(f.student)), Map.class);
        assertThat(submit.getBody().get("aiMark")).isEqualTo(5);
        assertThat(submit.getBody().get("status")).isEqualTo("SUBMITTED");

        // Result hidden until released
        ResponseEntity<Map> before = rest.exchange("/api/student/attempts/" + attemptId + "/result", HttpMethod.GET,
                new HttpEntity<>(auth(f.student)), Map.class);
        assertThat(before.getBody().get("released")).isEqualTo(false);
        assertThat(before.getBody().get("mark")).isNull();

        // Teacher overrides the AI mark (with mandatory note) -> effective mark = 8
        ResponseEntity<Map> override = rest.exchange("/api/school/attempts/" + attemptId + "/override", HttpMethod.POST,
                new HttpEntity<>(Map.of("mark", 8, "note", "Q1 partial credit on working"), auth(f.teacher)), Map.class);
        assertThat(override.getBody().get("effectiveMark")).isEqualTo(8);
        assertThat(override.getBody().get("teacherReviewedAt")).isNotNull();

        // Teacher releases results -> student now sees the effective (overridden) mark
        rest.exchange("/api/school/papers/" + f.paper.getId() + "/release-results", HttpMethod.POST,
                new HttpEntity<>(auth(f.teacher)), Map.class);
        ResponseEntity<Map> after = rest.exchange("/api/student/attempts/" + attemptId + "/result", HttpMethod.GET,
                new HttpEntity<>(auth(f.student)), Map.class);
        assertThat(after.getBody().get("released")).isEqualTo(true);
        assertThat(after.getBody().get("mark")).isEqualTo(8);
    }

    @Test
    void student_cannotStart_beforeExamWindowOpens() {
        Fixture f = seed(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)); // opens later
        ResponseEntity<String> resp = rest.exchange("/api/student/attempts/start", HttpMethod.POST,
                new HttpEntity<>(Map.of("paperId", f.paper.getId()), auth(f.student)), String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void student_cannotStart_afterExamWindowCloses() {
        Fixture f = seed(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1)); // already closed
        ResponseEntity<String> resp = rest.exchange("/api/student/attempts/start", HttpMethod.POST,
                new HttpEntity<>(Map.of("paperId", f.paper.getId()), auth(f.student)), String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void teacherOverride_requiresJustificationNote() {
        Fixture f = seed(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(2));
        SchoolPaperAttempt attempt = attemptRepository.save(
                new SchoolPaperAttempt(f.school.getId(), f.student.getId(), f.paper.getId(), f.cls.getId()));

        ResponseEntity<String> resp = rest.exchange("/api/school/attempts/" + attempt.getId() + "/override",
                HttpMethod.POST, new HttpEntity<>(Map.of("mark", 7, "note", ""), auth(f.teacher)), String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void teacherOfSchoolB_cannotOverrideSchoolA_attempt() {
        Fixture a = seed(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(2));
        School b = schoolRepository.save(new School("B-att", "LK", "b@s.test"));
        User teacherB = user(Role.TEACHER, b);
        SchoolPaperAttempt attemptA = attemptRepository.save(
                new SchoolPaperAttempt(a.school.getId(), a.student.getId(), a.paper.getId(), a.cls.getId()));

        ResponseEntity<String> resp = rest.exchange("/api/school/attempts/" + attemptA.getId() + "/override",
                HttpMethod.POST, new HttpEntity<>(Map.of("mark", 9, "note", "hax"), auth(teacherB)), String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }
}
