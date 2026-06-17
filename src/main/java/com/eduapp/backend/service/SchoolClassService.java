package com.eduapp.backend.service;

import com.eduapp.backend.model.SchoolClass;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * All operations are scoped to the current tenant taken from {@link TenantContext}.
 * No method ever accepts a caller-supplied school id, so a user in school A can never
 * read or write school B's classes.
 */
@Service
public class SchoolClassService {

    private final SchoolClassRepository repository;

    public SchoolClassService(SchoolClassRepository repository) {
        this.repository = repository;
    }

    private Long requireTenant() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context — this endpoint requires a school-tier user");
        }
        return schoolId;
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> listForCurrentTenant() {
        return repository.findBySchoolId(requireTenant());
    }

    @Transactional(readOnly = true)
    public SchoolClass getForCurrentTenant(Long id) {
        Long schoolId = requireTenant();
        return repository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found in this school"));
    }

    @Transactional
    public SchoolClass create(Long teacherId, String name, String subject, String yearGroup) {
        Long schoolId = requireTenant();
        SchoolClass sc = new SchoolClass(schoolId, teacherId, name, subject, yearGroup, generateUniqueClassCode());
        return repository.save(sc);
    }

    // BT-4.6: SCHOOL_ADMIN assigns/removes teachers on a class (tenant-scoped).
    @Transactional
    public SchoolClass assignTeacher(Long classId, Long teacherId) {
        SchoolClass sc = getForCurrentTenant(classId);
        sc.addTeacher(teacherId);
        return repository.save(sc);
    }

    @Transactional
    public SchoolClass removeTeacher(Long classId, Long teacherId) {
        SchoolClass sc = getForCurrentTenant(classId);
        sc.removeTeacher(teacherId);
        return repository.save(sc);
    }

    /** BT-4.2: ownership check — is the given user a teacher of this class (within the tenant)? */
    @Transactional(readOnly = true)
    public boolean isTeacherOfClass(Long classId, Long userId) {
        return repository.findByIdAndSchoolId(classId, requireTenant())
                .map(sc -> sc.isTaughtBy(userId))
                .orElse(false);
    }

    private String generateUniqueClassCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (repository.existsByClassCode(code));
        return code;
    }
}
