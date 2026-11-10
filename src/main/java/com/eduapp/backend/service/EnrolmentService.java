package com.eduapp.backend.service;

import com.eduapp.backend.dto.SchoolEnrolmentDto;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class-code self-enrolment (WP-5). A student joins a class by its code; this
 * is the
 * onboarding path that binds a user to a school and mints SCHOOL_STUDENT.
 */
@Service
public class EnrolmentService {

    private final SchoolClassRepository classRepository;
    private final SchoolEnrolmentRepository enrolmentRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    public EnrolmentService(SchoolClassRepository classRepository,
            SchoolEnrolmentRepository enrolmentRepository,
            UserRepository userRepository,
            SchoolRepository schoolRepository) {
        this.classRepository = classRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
    }

    @Transactional
    public SchoolEnrolment joinByClassCode(Long userId, String classCode) {
        // Deliberate cross-tenant lookup: the class code is the onboarding credential.
        SchoolClass schoolClass = classRepository.findByClassCode(classCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class code"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long targetSchoolId = schoolClass.getSchoolId();
        Long currentSchoolId = user.getSchoolId();
        if (currentSchoolId != null && !currentSchoolId.equals(targetSchoolId)) {
            // Already a member of a different school — cannot cross the tenant boundary.
            throw new IllegalStateException("This account already belongs to a different school");
        }

        // BT-9: self-join is a REQUEST — a teacher must approve it.
        var existing = enrolmentRepository.findByClassIdAndStudentId(schoolClass.getId(), userId);
        if (existing.isPresent()) {
            SchoolEnrolment e = existing.get();
            // Re-requesting after a rejection reopens it as PENDING; otherwise return
            // as-is.
            if (e.getStatus() == SchoolEnrolmentStatus.REJECTED) {
                e.setStatus(SchoolEnrolmentStatus.PENDING);
                return enrolmentRepository.save(e);
            }
            return e;
        }

        // Bind the user to the school and upgrade their role on first enrolment request, so they
        // become a SCHOOL_STUDENT of that school immediately — but CLASS access stays gated until a
        // teacher marks the enrolment ACTIVE. (A WIP refactor had dropped this; the business rule is
        // "joining/requesting a class converts a global STUDENT into a SCHOOL_STUDENT of the school".)
        if (currentSchoolId == null) {
            School school = schoolRepository.findById(targetSchoolId)
                    .orElseThrow(() -> new IllegalStateException("School no longer exists"));
            user.setSchool(school);
            if (user.getRole() == Role.STUDENT) {
                user.setRole(Role.SCHOOL_STUDENT);
            }
            userRepository.save(user);
        }

        SchoolEnrolment enrolment = new SchoolEnrolment(targetSchoolId, schoolClass.getId(), userId);
        enrolment.setStatus(SchoolEnrolmentStatus.PENDING);
        return enrolmentRepository.save(enrolment);
    }

    /**
     * The caller's own enrolments (any status, any school), each enriched with its class name.
     * Powers the student dashboard so a PENDING request is visible before a teacher approves it.
     */
    @Transactional(readOnly = true)
    public List<SchoolEnrolmentDto> myEnrolments(Long userId) {
        return enrolmentRepository.findByStudentId(userId).stream()
                .map(e -> SchoolEnrolmentDto.fromWithClass(
                        e, classRepository.findById(e.getClassId()).orElse(null)))
                .toList();
    }
}
