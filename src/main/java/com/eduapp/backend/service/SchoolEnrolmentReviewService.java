package com.eduapp.backend.service;

import com.eduapp.backend.dto.SchoolEnrolmentDto;
import com.eduapp.backend.exception.BadRequestException;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.SchoolEnrolment;
import com.eduapp.backend.model.SchoolEnrolmentStatus;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.security.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BT-9: enrolment approval. A teacher of the class (or a SCHOOL_ADMIN) reviews pending join
 * requests and accepts/rejects them, and may directly enrol a student (immediately ACTIVE).
 * All operations are tenant-scoped via {@link TenantContext}.
 */
@Service
public class SchoolEnrolmentReviewService {

    private final SchoolEnrolmentRepository enrolmentRepository;
    private final SchoolClassService classService;
    private final UserRepository userRepository;

    public SchoolEnrolmentReviewService(SchoolEnrolmentRepository enrolmentRepository,
            SchoolClassService classService, UserRepository userRepository) {
        this.enrolmentRepository = enrolmentRepository;
        this.classService = classService;
        this.userRepository = userRepository;
    }

    private Long requireTenant() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context");
        }
        return schoolId;
    }

    /** SCHOOL_ADMIN manages any class in the tenant; a TEACHER only classes they teach (BT-4.2). */
    private void assertCanManage(Long classId, AuthenticatedUser caller) {
        if (caller.getRole() == Role.SCHOOL_ADMIN) {
            return;
        }
        if (caller.getRole() == Role.TEACHER && classService.isTeacherOfClass(classId, caller.getUserId())) {
            return;
        }
        throw new AccessDeniedException("You do not teach this class");
    }

    private SchoolEnrolment loadInTenant(Long classId, Long enrolmentId) {
        Long schoolId = requireTenant();
        SchoolEnrolment e = enrolmentRepository.findById(enrolmentId)
                .orElseThrow(() -> new BadRequestException("Enrolment not found"));
        if (!e.getSchoolId().equals(schoolId) || !e.getClassId().equals(classId)) {
            throw new BadRequestException("Enrolment not found in this class");
        }
        return e;
    }

    @Transactional(readOnly = true)
    public List<SchoolEnrolmentDto> list(Long classId, SchoolEnrolmentStatus status, AuthenticatedUser caller) {
        assertCanManage(classId, caller);
        Long schoolId = requireTenant();
        List<SchoolEnrolment> rows = (status != null)
                ? enrolmentRepository.findByClassIdAndSchoolIdAndStatus(classId, schoolId, status)
                : enrolmentRepository.findByClassIdAndSchoolId(classId, schoolId);
        return rows.stream()
                .map(e -> SchoolEnrolmentDto.from(e, userRepository.findById(e.getStudentId()).orElse(null)))
                .toList();
    }

    @Transactional
    public SchoolEnrolmentDto setStatus(Long classId, Long enrolmentId, SchoolEnrolmentStatus status,
            AuthenticatedUser caller) {
        assertCanManage(classId, caller);
        SchoolEnrolment e = loadInTenant(classId, enrolmentId);
        e.setStatus(status);
        enrolmentRepository.save(e);
        return SchoolEnrolmentDto.from(e, userRepository.findById(e.getStudentId()).orElse(null));
    }

    /** BT-9.4 / BT-4.3: teacher/admin directly enrols a student — immediately ACTIVE (no approval). */
    @Transactional
    public SchoolEnrolmentDto directEnrol(Long classId, Long studentId, AuthenticatedUser caller) {
        assertCanManage(classId, caller);
        Long schoolId = requireTenant();
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        if (student.getSchoolId() == null || !student.getSchoolId().equals(schoolId)) {
            throw new BadRequestException("Student is not a member of this school");
        }
        SchoolEnrolment e = enrolmentRepository.findByClassIdAndStudentId(classId, studentId)
                .orElseGet(() -> new SchoolEnrolment(schoolId, classId, studentId));
        e.setStatus(SchoolEnrolmentStatus.ACTIVE);
        enrolmentRepository.save(e);
        return SchoolEnrolmentDto.from(e, student);
    }
}
