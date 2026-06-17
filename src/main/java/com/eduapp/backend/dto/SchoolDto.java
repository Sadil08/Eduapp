package com.eduapp.backend.dto;

import com.eduapp.backend.model.School;

/** BT-1: School view for the admin console (no secrets). */
public record SchoolDto(
        Long id,
        String name,
        String country,
        String contactEmail,
        String status,
        String planTier) {

    public static SchoolDto from(School s) {
        return new SchoolDto(
                s.getId(), s.getName(), s.getCountry(), s.getContactEmail(),
                s.getStatus() != null ? s.getStatus().name() : null,
                s.getPlanTier() != null ? s.getPlanTier().name() : null);
    }
}
