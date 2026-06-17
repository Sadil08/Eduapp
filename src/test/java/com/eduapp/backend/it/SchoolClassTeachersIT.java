package com.eduapp.backend.it;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.School;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** BT-4.1/BT-4.6 — a class can have many teachers; only a SCHOOL_ADMIN manages the roster. */
class SchoolClassTeachersIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private School school;

    private User member(Role role) {
        if (school == null) {
            school = schoolRepository.save(new School("MT " + UUID.randomUUID().toString().substring(0, 5), "LK", "mt@s.test"));
        }
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role.name().toLowerCase() + "-" + u + "@t.test", "x", "u-" + u);
        user.setRole(role);
        user.setSchool(school);
        user.setReferralCode("RC" + u);
        return userRepository.save(user);
    }

    private HttpHeaders bearer(User u) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(),
                u.getReferralCode(), u.getTokenVersion()));
        return h;
    }

    @Test
    @SuppressWarnings("unchecked")
    void admin_assignsAndRemovesTeachers_onAClass() {
        User admin = member(Role.SCHOOL_ADMIN);
        User teacherA = member(Role.TEACHER);
        User teacherB = member(Role.TEACHER);

        // Admin creates a class assigned to teacher A.
        ResponseEntity<Map> created = rest.exchange("/api/school/classes", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "G10 Physics", "subject", "Physics",
                        "yearGroup", "G10", "teacherId", teacherA.getId()), bearer(admin)), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long classId = ((Number) created.getBody().get("id")).longValue();
        assertThat(((List<Number>) created.getBody().get("teacherIds")))
                .extracting(Number::longValue).containsExactly(teacherA.getId());

        // Admin assigns teacher B → both teach the class.
        ResponseEntity<Map> assigned = rest.exchange(
                "/api/school/classes/" + classId + "/teachers/" + teacherB.getId(),
                HttpMethod.POST, new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(assigned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((List<Number>) assigned.getBody().get("teacherIds")))
                .extracting(Number::longValue).containsExactlyInAnyOrder(teacherA.getId(), teacherB.getId());

        // Admin removes teacher A → only B remains.
        ResponseEntity<Map> removed = rest.exchange(
                "/api/school/classes/" + classId + "/teachers/" + teacherA.getId(),
                HttpMethod.DELETE, new HttpEntity<>(bearer(admin)), Map.class);
        assertThat(((List<Number>) removed.getBody().get("teacherIds")))
                .extracting(Number::longValue).containsExactly(teacherB.getId());
    }

    @Test
    void teacher_cannotManageTeacherRoster() {
        User admin = member(Role.SCHOOL_ADMIN);
        User teacherA = member(Role.TEACHER);
        ResponseEntity<Map> created = rest.exchange("/api/school/classes", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "G11 Chem", "subject", "Chem",
                        "yearGroup", "G11", "teacherId", teacherA.getId()), bearer(admin)), Map.class);
        Long classId = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<String> resp = rest.exchange(
                "/api/school/classes/" + classId + "/teachers/" + teacherA.getId(),
                HttpMethod.POST, new HttpEntity<>(bearer(teacherA)), String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
