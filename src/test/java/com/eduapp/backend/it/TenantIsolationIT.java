package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.SchoolClassRepository;
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

/**
 * WP-4 — the load-bearing multi-tenancy guarantee: a school-A user can never read
 * school-B data. Verified at the HTTP layer with two real tenants.
 */
class TenantIsolationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private User createTeacher(School school) {
        String uniq = UUID.randomUUID().toString().substring(0, 8);
        User u = new User("teacher-" + uniq + "@s.test", "x", "teacher-" + uniq);
        u.setRole(Role.TEACHER);
        u.setSchool(school);
        u.setReferralCode("REF" + uniq);
        return userRepository.save(u);
    }

    private HttpHeaders bearer(User u) {
        String token = jwtUtil.generateToken(u.getEmail(), u.getRole(), u.getId(), u.getReferralCode());
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    @Test
    void schoolA_cannotSeeSchoolB_classes() {
        School a = schoolRepository.save(new School("School A", "LK", "a@s.test"));
        School b = schoolRepository.save(new School("School B", "LK", "b@s.test"));
        User teacherA = createTeacher(a);

        SchoolClass classA = classRepository.save(
                new SchoolClass(a.getId(), teacherA.getId(), "A-Physics", "Physics", "G10", "CODEA-" + a.getId()));
        SchoolClass classB = classRepository.save(
                new SchoolClass(b.getId(), null, "B-Maths", "Maths", "G11", "CODEB-" + b.getId()));

        // Teacher A lists classes -> sees only school A's class.
        ResponseEntity<List> list = rest.exchange("/api/school/classes", HttpMethod.GET,
                new HttpEntity<>(bearer(teacherA)), List.class);

        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> body = list.getBody();
        assertThat(body).extracting(m -> m.get("id"))
                .contains(classA.getId().intValue())
                .doesNotContain(classB.getId().intValue());
        assertThat(body).allSatisfy(m ->
                assertThat(((Number) m.get("schoolId")).longValue()).isEqualTo(a.getId()));
    }

    @Test
    void schoolA_cannotFetchSchoolB_classById() {
        School a = schoolRepository.save(new School("School A2", "LK", "a2@s.test"));
        School b = schoolRepository.save(new School("School B2", "LK", "b2@s.test"));
        User teacherA = createTeacher(a);
        SchoolClass classB = classRepository.save(
                new SchoolClass(b.getId(), null, "B-Chem", "Chem", "G12", "CODEB2-" + b.getId()));

        ResponseEntity<String> resp = rest.exchange("/api/school/classes/" + classB.getId(),
                HttpMethod.GET, new HttpEntity<>(bearer(teacherA)), String.class);

        // Cross-tenant fetch must not succeed.
        assertThat(resp.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void globalTierStudent_cannotAccessSchoolEndpoints() {
        String uniq = UUID.randomUUID().toString().substring(0, 8);
        User student = new User("stu-" + uniq + "@g.test", "x", "stu-" + uniq);
        student.setRole(Role.STUDENT); // no school -> global tier
        student.setReferralCode("REFG" + uniq);
        userRepository.save(student);

        ResponseEntity<String> resp = rest.exchange("/api/school/classes", HttpMethod.GET,
                new HttpEntity<>(bearer(student)), String.class);

        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}
