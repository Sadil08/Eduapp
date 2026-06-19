package com.eduapp.backend.service;

import com.eduapp.backend.dto.SchoolCreateRequest;
import com.eduapp.backend.exception.BadRequestException;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.School;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.RoleCreationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BT-1: school onboarding. Only an ADMIN may create a School + its first SCHOOL_ADMIN (enforced
 * via {@link RoleCreationPolicy}). The admin account is provisioned with a set-password OTP — no
 * default credentials.
 */
@Service
public class SchoolService {

    private static final Logger logger = LoggerFactory.getLogger(SchoolService.class);

    private final SchoolRepository schoolRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public SchoolService(SchoolRepository schoolRepository, UserService userService,
            UserRepository userRepository) {
        this.schoolRepository = schoolRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /** Creates the School and provisions its first SCHOOL_ADMIN. {@code creatorRole} is the caller's role. */
    @Transactional
    public School createSchool(SchoolCreateRequest req, Role creatorRole) {
        RoleCreationPolicy.assertCanCreate(creatorRole, Role.SCHOOL_ADMIN);
        School school = schoolRepository.save(new School(req.getName(), req.getCountry(), req.getContactEmail()));
        try {
            userService.provisionUser(req.getAdminEmail(), req.getAdminName(), Role.SCHOOL_ADMIN, school);
        } catch (BadRequestException e) {
            // e.g. admin email already in use — surface as a 400 and roll back the school.
            throw new BadRequestException("Could not provision school admin: " + e.getMessage());
        }
        logger.info("Created school {} ({}) with first SCHOOL_ADMIN {}", school.getId(), school.getName(),
                req.getAdminEmail());
        return school;
    }

    @Transactional(readOnly = true)
    public Page<School> list(Pageable pageable) {
        return schoolRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public School get(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("School not found"));
    }

    /**
     * Resend the set-password OTP to the school's admin(s) — for when the original timed out or
     * a fresh token is needed. Reuses the password-reset mechanism. Returns the emails notified.
     */
    @Transactional
    public List<String> resendAdminInvite(Long schoolId) {
        get(schoolId); // 404 if the school doesn't exist
        List<User> admins = userRepository.findBySchool_IdAndRole(schoolId, Role.SCHOOL_ADMIN);
        if (admins.isEmpty()) {
            throw new BadRequestException("This school has no admin to resend an invite to");
        }
        admins.forEach(a -> userService.forgotPassword(a.getEmail()));
        return admins.stream().map(User::getEmail).toList();
    }
}
