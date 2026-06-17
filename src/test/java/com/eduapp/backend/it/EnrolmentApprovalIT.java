package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BT-9 — a self-join is PENDING (no class access) until a teacher of the class accepts it;
 * acceptance grants access.
 */
class EnrolmentApprovalIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired UserRepository userRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired SchoolEnrolmentRepository enrolmentRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(),
                u.getReferralCode(), u.getTokenVersion()));
        return h;
    }

    private User user(Role role, School school) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role.name().toLowerCase() + "-" + u + "@t.test", "x", "u-" + u);
        user.setRole(role);
        user.setSchool(school);
        user.setReferralCode("RC" + u);
        return userRepository.save(user);
    }

    @Test
    @SuppressWarnings("unchecked")
    void selfJoinIsPending_thenTeacherAcceptGrantsAccess() {
        School school = schoolRepository.save(new School("Appr " + UUID.randomUUID().toString().substring(0, 5), "LK", "ap@s.test"));
        User teacher = user(Role.TEACHER, school);
        String code = "JC" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        SchoolClass cls = classRepository.save(
                new SchoolClass(school.getId(), teacher.getId(), "G10 Bio", "Bio", "G10", code));
        User student = user(Role.STUDENT, null); // global student joins by code

        // 1) Self-join → PENDING.
        ResponseEntity<Map> join = rest.exchange("/api/enrolments/join", HttpMethod.POST,
                new HttpEntity<>(Map.of("classCode", code), bearer(student)), Map.class);
        assertThat(join.getStatusCode().is2xxSuccessful()).isTrue();
        SchoolEnrolment enr = enrolmentRepository.findByClassIdAndStudentId(cls.getId(), student.getId()).orElseThrow();
        assertThat(enr.getStatus()).isEqualTo(SchoolEnrolmentStatus.PENDING);

        // The student is now SCHOOL_STUDENT (role upgraded) — mint a token reflecting that.
        User joined = userRepository.findById(student.getId()).orElseThrow();
        assertThat(joined.getRole()).isEqualTo(Role.SCHOOL_STUDENT);

        // 2) PENDING → no class access.
        ResponseEntity<String> blocked = rest.exchange("/api/student/papers?classId=" + cls.getId(),
                HttpMethod.GET, new HttpEntity<>(bearer(joined)), String.class);
        assertThat(blocked.getStatusCode().value()).isIn(401, 403);

        // 3) Teacher sees the pending request.
        ResponseEntity<List> pending = rest.exchange(
                "/api/school/classes/" + cls.getId() + "/enrolments?status=PENDING",
                HttpMethod.GET, new HttpEntity<>(bearer(teacher)), List.class);
        assertThat(pending.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<Map<String, Object>>) pending.getBody())
                .anySatisfy(m -> assertThat(((Number) m.get("studentId")).longValue()).isEqualTo(student.getId()));

        // 4) Teacher accepts → ACTIVE.
        ResponseEntity<Map> accepted = rest.exchange(
                "/api/school/classes/" + cls.getId() + "/enrolments/" + enr.getId() + "/accept",
                HttpMethod.POST, new HttpEntity<>(bearer(teacher)), Map.class);
        assertThat(accepted.getBody().get("status")).isEqualTo("ACTIVE");

        // 5) Now the student has class access (empty list, but 200 not 403).
        ResponseEntity<List> ok = rest.exchange("/api/student/papers?classId=" + cls.getId(),
                HttpMethod.GET, new HttpEntity<>(bearer(joined)), List.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void teacherOfOtherClass_cannotReviewEnrolments() {
        School school = schoolRepository.save(new School("Appr2 " + UUID.randomUUID().toString().substring(0, 5), "LK", "ap2@s.test"));
        User teacherA = user(Role.TEACHER, school);
        User teacherB = user(Role.TEACHER, school);
        String code = "OC" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        SchoolClass cls = classRepository.save(
                new SchoolClass(school.getId(), teacherA.getId(), "G11 Bio", "Bio", "G11", code));

        // teacherB does not teach this class → cannot list its enrolments.
        ResponseEntity<String> resp = rest.exchange(
                "/api/school/classes/" + cls.getId() + "/enrolments?status=PENDING",
                HttpMethod.GET, new HttpEntity<>(bearer(teacherB)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
