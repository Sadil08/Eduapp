package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant-scoped for listing (by school); {@code findByToken} is the public accept path
 * (the invitee has no tenant yet, so the token itself is the authorization).
 */
@TenantScoped
public interface SchoolInviteRepository extends JpaRepository<SchoolInvite, Long> {

    Optional<SchoolInvite> findByToken(String token);

    List<SchoolInvite> findBySchoolId(Long schoolId);
}
