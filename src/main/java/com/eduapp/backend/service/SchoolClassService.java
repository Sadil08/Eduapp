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

    private String generateUniqueClassCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (repository.existsByClassCode(code));
        return code;
    }
}
