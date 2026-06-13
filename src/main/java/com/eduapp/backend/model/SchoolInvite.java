package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A single-use, expiring invitation for someone to join a school in a privileged role
 * (TEACHER). Created by a SCHOOL_ADMIN; accepted via the token to create the user.
 * This is the ONLY way privileged school roles are minted (public registration cannot).
 */
@Entity
@Table(name = "school_invites",
        uniqueConstraints = @UniqueConstraint(name = "uk_invite_token", columnNames = "token"))
public class SchoolInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchoolInviteStatus status = SchoolInviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SchoolInvite() {
    }

    public SchoolInvite(Long schoolId, String email, Role role, String token, LocalDateTime expiresAt) {
        this.schoolId = schoolId;
        this.email = email;
        this.role = role;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable() {
        return status == SchoolInviteStatus.PENDING && expiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SchoolInviteStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolInviteStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
