package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolInvite;

import java.time.LocalDateTime;

public class SchoolInviteDto {
    private Long id;
    private String email;
    private String role;
    private String status;
    private String token; // returned to the admin so it can be delivered to the invitee
    private LocalDateTime expiresAt;

    public static SchoolInviteDto from(SchoolInvite i) {
        SchoolInviteDto d = new SchoolInviteDto();
        d.id = i.getId();
        d.email = i.getEmail();
        d.role = i.getRole().name();
        d.status = i.getStatus().name();
        d.token = i.getToken();
        d.expiresAt = i.getExpiresAt();
        return d;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
