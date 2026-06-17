package com.eduapp.backend.dto;

import com.eduapp.backend.model.Role;

/**
 * FE-A1.1: the authenticated user's profile, returned by {@code GET /api/auth/me}. The frontend
 * trusts ONLY this server-validated response for identity/role — never a client-decoded JWT.
 */
public record MeResponse(
        Long id,
        String name,
        String email,
        Role role,
        Long schoolId,
        String referralCode) {
}
