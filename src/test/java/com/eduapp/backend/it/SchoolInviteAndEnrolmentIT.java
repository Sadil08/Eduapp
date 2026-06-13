package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-5 — teacher invites and class-code self-enrolment, end-to-end. */
class SchoolInviteAndEnrolmentIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired SchoolInviteRepository inviteRepository;
    @Autowired SchoolEnrolmentRepository enrolmentRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User saveUser(Role role, School school) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role.name().toLowerCase() + "-" + u + "@t.test", "x", role.name().toLowerCase() + "-" + u);
        user.setRole(role);
        user.setSchool(school);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode()));
        return h;
    }

    // ---------- Teacher invites ----------

    @Test
    void schoolAdmin_invitesTeacher_andAcceptCreatesTeacherInThatSchool() {
        School school = schoolRepository.save(new School("Invite School", "LK", "i@s.test"));
        User admin = saveUser(Role.SCHOOL_ADMIN, school);
        String teacherEmail = "newteacher-" + UUID.randomUUID().toString().substring(0, 8) + "@t.test";

        // Admin creates the invite (tenant-scoped).
        ResponseEntity<Map> created = rest.exchange("/api/school/invites", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", teacherEmail), bearer(admin)), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) created.getBody().get("token");
        assertThat(token).isNotBlank();

        // Anyone with the token accepts (public) -> a TEACHER is created in the school.
        ResponseEntity<Map> accepted = rest.postForEntity("/api/invites/accept",
                Map.of("token", token, "name", "Teacher-" + UUID.randomUUID(), "password", "pw12345678"), Map.class);
        assertThat(accepted.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accepted.getBody().get("role")).isEqualTo("TEACHER");

        User teacher = userRepository.findByEmail(teacherEmail).orElseThrow();
        assertThat(teacher.getRole()).isEqualTo(Role.TEACHER);
        assertThat(teacher.getSchoolId()).isEqualTo(school.getId());
    }

    @Test
    void invite_cannotBeAcceptedTwice() {
        School school = schoolRepository.save(new School("Once School", "LK", "o@s.test"));
        User admin = saveUser(Role.SCHOOL_ADMIN, school);
        String email = "once-" + UUID.randomUUID().toString().substring(0, 8) + "@t.test";
        ResponseEntity<Map> created = rest.exchange("/api/school/invites", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email), bearer(admin)), Map.class);
        String token = (String) created.getBody().get("token");

        rest.postForEntity("/api/invites/accept",
                Map.of("token", token, "name", "First-" + UUID.randomUUID(), "password", "pw12345678"), Map.class);
        ResponseEntity<String> second = rest.postForEntity("/api/invites/accept",
                Map.of("token", token, "name", "Second-" + UUID.randomUUID(), "password", "pw12345678"), String.class);

        assertThat(second.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void teacher_cannotCreateInvites_onlySchoolAdminCan() {
        School school = schoolRepository.save(new School("Authz School", "LK", "az@s.test"));
        User teacher = saveUser(Role.TEACHER, school);

        ResponseEntity<String> resp = rest.exchange("/api/school/invites", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", "x@t.test"), bearer(teacher)), String.class);

        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }

    // ---------- Class-code enrolment ----------

    @Test
    void globalStudent_joinsByClassCode_becomesSchoolStudentBoundToSchool() {
        School school = schoolRepository.save(new School("Enrol School", "LK", "e@s.test"));
        SchoolClass cls = classRepository.save(
                new SchoolClass(school.getId(), null, "Bio", "Biology", "G9", "JOIN-" + UUID.randomUUID().toString().substring(0, 5)));
        User student = saveUser(Role.STUDENT, null); // global-tier student

        ResponseEntity<Map> resp = rest.exchange("/api/enrolments/join", HttpMethod.POST,
                new HttpEntity<>(Map.of("classCode", cls.getClassCode()), bearer(student)), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) resp.getBody().get("classId")).longValue()).isEqualTo(cls.getId());

        User reloaded = userRepository.findById(student.getId()).orElseThrow();
        assertThat(reloaded.getRole()).isEqualTo(Role.SCHOOL_STUDENT);
        assertThat(reloaded.getSchoolId()).isEqualTo(school.getId());
        assertThat(enrolmentRepository.existsByClassIdAndStudentId(cls.getId(), student.getId())).isTrue();
    }

    @Test
    void studentOfSchoolA_cannotJoinSchoolB_classCode() {
        School a = schoolRepository.save(new School("A-enrol", "LK", "ae@s.test"));
        School b = schoolRepository.save(new School("B-enrol", "LK", "be@s.test"));
        User studentA = saveUser(Role.SCHOOL_STUDENT, a);
        SchoolClass clsB = classRepository.save(
                new SchoolClass(b.getId(), null, "B-Bio", "Biology", "G9", "BJOIN-" + UUID.randomUUID().toString().substring(0, 5)));

        ResponseEntity<String> resp = rest.exchange("/api/enrolments/join", HttpMethod.POST,
                new HttpEntity<>(Map.of("classCode", clsB.getClassCode()), bearer(studentA)), String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }
}
