package com.eduapp.backend.service;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.SchoolInviteRepository;
import com.eduapp.backend.repository.SchoolRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Teacher invitations (WP-5). Creation is tenant-scoped (the inviting SCHOOL_ADMIN's
 * school). Acceptance is the ONLY public path that mints a TEACHER — it is gated by the
 * single-use, expiring token, not by a role, since the invitee has no account yet.
 */
@Service
public class SchoolInviteService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int INVITE_TTL_DAYS = 7;

    private final SchoolInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    public SchoolInviteService(SchoolInviteRepository inviteRepository,
            UserRepository userRepository,
            SchoolRepository schoolRepository,
            PasswordEncoder passwordEncoder) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates a TEACHER invite for the current tenant. Returns the invite (token to be emailed). */
    @Transactional
    public SchoolInvite createTeacherInvite(String email) {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context — only a school-tier admin can invite teachers");
        }
        SchoolInvite invite = new SchoolInvite(schoolId, email, Role.TEACHER,
                generateToken(), LocalDateTime.now().plusDays(INVITE_TTL_DAYS));
        return inviteRepository.save(invite);
    }

    /** Accepts an invite by token, creating the invited user in the invite's school. */
    @Transactional
    public User acceptInvite(String token, String name, String rawPassword) {
        SchoolInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));
        if (!invite.isUsable()) {
            throw new IllegalArgumentException("Invite is no longer valid");
        }
        userRepository.findByEmail(invite.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("An account with this email already exists");
        });

        School school = schoolRepository.findById(invite.getSchoolId())
                .orElseThrow(() -> new IllegalStateException("School for invite no longer exists"));

        User user = new User(invite.getEmail(), passwordEncoder.encode(rawPassword), name);
        user.setRole(invite.getRole()); // TEACHER — minted only here, never via public registration
        user.setSchool(school);
        user.setReferralCode(generateReferralCode());
        User saved = userRepository.save(user);

        invite.setStatus(SchoolInviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        return saved;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateReferralCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.findByReferralCode(code).isPresent());
        return code;
    }
}
