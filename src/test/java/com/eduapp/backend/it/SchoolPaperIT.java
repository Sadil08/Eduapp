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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-6 — school paper lifecycle (create→add question→approve→assign), student visibility, isolation. */
class SchoolPaperIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired SchoolEnrolmentRepository enrolmentRepository;
    @Autowired SchoolPaperRepository paperRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User user(Role role, School school) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@p.test", "x", role + "-" + u);
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

    private SchoolClass classIn(School s) {
        return classRepository.save(new SchoolClass(s.getId(), null, "C", "Sub", "G10", "CC-" + UUID.randomUUID().toString().substring(0, 6)));
    }

    @Test
    void teacher_createsApprovesAndAssignsPaper_throughLifecycle() {
        School school = schoolRepository.save(new School("Paper School", "LK", "p@s.test"));
        User teacher = user(Role.TEACHER, school);
        SchoolClass cls = classIn(school);

        // Create draft
        ResponseEntity<Map> created = rest.exchange("/api/school/papers", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "Term Test", "type", "ESSAY"), auth(teacher)), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody().get("status")).isEqualTo("DRAFT");
        Integer paperId = (Integer) created.getBody().get("id");

        // Add a question (the "teacher reviews extracted question" step)
        ResponseEntity<Map> q = rest.exchange("/api/school/papers/" + paperId + "/questions", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Explain osmosis", "type", "ESSAY", "marks", 10), auth(teacher)), Map.class);
        assertThat(q.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Approve
        ResponseEntity<Map> approved = rest.exchange("/api/school/papers/" + paperId + "/approve", HttpMethod.POST,
                new HttpEntity<>(auth(teacher)), Map.class);
        assertThat(approved.getBody().get("status")).isEqualTo("APPROVED");

        // Assign to class with an exam window
        Map<String, Object> assignBody = Map.of(
                "classId", cls.getId().intValue(),
                "examWindowStart", LocalDateTime.now().minusMinutes(5).toString(),
                "examWindowEnd", LocalDateTime.now().plusHours(2).toString(),
                "timeLimitMinutes", 60);
        ResponseEntity<Map> assigned = rest.exchange("/api/school/papers/" + paperId + "/assign", HttpMethod.POST,
                new HttpEntity<>(assignBody, auth(teacher)), Map.class);
        assertThat(assigned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assigned.getBody().get("status")).isEqualTo("ASSIGNED");
        assertThat(((Number) assigned.getBody().get("classId")).longValue()).isEqualTo(cls.getId());
    }

    @Test
    void approve_failsWhenPaperHasNoQuestions() {
        School school = schoolRepository.save(new School("Empty School", "LK", "e@s.test"));
        User teacher = user(Role.TEACHER, school);
        ResponseEntity<Map> created = rest.exchange("/api/school/papers", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "Empty", "type", "ESSAY"), auth(teacher)), Map.class);
        Integer paperId = (Integer) created.getBody().get("id");

        ResponseEntity<String> approve = rest.exchange("/api/school/papers/" + paperId + "/approve",
                HttpMethod.POST, new HttpEntity<>(auth(teacher)), String.class);

        assertThat(approve.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void student_seesOnlyAssignedPapers_forEnrolledClass() {
        School school = schoolRepository.save(new School("Vis School", "LK", "v@s.test"));
        SchoolClass cls = classIn(school);
        User student = user(Role.SCHOOL_STUDENT, school);
        enrolmentRepository.save(new SchoolEnrolment(school.getId(), cls.getId(), student.getId()));

        // One DRAFT (hidden) and one ASSIGNED (visible) paper for the class.
        SchoolPaper draft = new SchoolPaper(school.getId(), "Draft P", null, PaperType.ESSAY, null);
        draft.setClassId(cls.getId());
        paperRepository.save(draft);
        SchoolPaper assigned = new SchoolPaper(school.getId(), "Assigned P", null, PaperType.ESSAY, null);
        assigned.setClassId(cls.getId());
        assigned.setStatus(SchoolPaperStatus.ASSIGNED);
        paperRepository.save(assigned);

        ResponseEntity<List> resp = rest.exchange("/api/student/papers?classId=" + cls.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(student)), List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> body = resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("name")).isEqualTo("Assigned P");
        assertThat(body.get(0).get("status")).isEqualTo("ASSIGNED");
    }

    @Test
    void student_notEnrolled_isDenied() {
        School school = schoolRepository.save(new School("NoEnrol", "LK", "n@s.test"));
        SchoolClass cls = classIn(school);
        User student = user(Role.SCHOOL_STUDENT, school); // not enrolled in cls

        ResponseEntity<String> resp = rest.exchange("/api/student/papers?classId=" + cls.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(student)), String.class);

        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void teacherA_cannotAccessSchoolB_paper() {
        School a = schoolRepository.save(new School("PA", "LK", "pa@s.test"));
        School b = schoolRepository.save(new School("PB", "LK", "pb@s.test"));
        User teacherA = user(Role.TEACHER, a);
        SchoolPaper bPaper = paperRepository.save(new SchoolPaper(b.getId(), "B paper", null, PaperType.ESSAY, null));

        ResponseEntity<String> resp = rest.exchange("/api/school/papers/" + bPaper.getId(),
                HttpMethod.GET, new HttpEntity<>(auth(teacherA)), String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }
}
