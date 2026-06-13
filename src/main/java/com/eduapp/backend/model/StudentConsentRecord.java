package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * A user's layered consent choices (PRODUCT_BUSINESS_PLAN.md §10.2). Layer 1 (platform
 * operation) is implied by having an account; the optional layers below are explicit.
 * Analytics export queries filter on {@code analyticsSharingConsented}; under-16 users
 * require recorded parental consent.
 */
@Entity
@Table(name = "student_consent_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_consent_user", columnNames = "user_id"))
public class StudentConsentRecord {

    public static final int PARENTAL_CONSENT_AGE = 16;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "consent_version", nullable = false)
    private String consentVersion;

    @Column(name = "analytics_sharing_consented", nullable = false)
    private boolean analyticsSharingConsented = false;

    @Column(name = "global_leaderboard_consented", nullable = false)
    private boolean globalLeaderboardConsented = false;

    @Column(name = "research_participation_consented", nullable = false)
    private boolean researchParticipationConsented = false;

    @Column(name = "data_retention_preference")
    private String dataRetentionPreference;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "parental_consent_granted", nullable = false)
    private boolean parentalConsentGranted = false;

    @Column(name = "consented_at", nullable = false)
    private LocalDateTime consentedAt = LocalDateTime.now();

    public StudentConsentRecord() {
    }

    public static boolean requiresParentalConsent(LocalDate dateOfBirth) {
        return dateOfBirth != null && Period.between(dateOfBirth, LocalDate.now()).getYears() < PARENTAL_CONSENT_AGE;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion;
    }

    public boolean isAnalyticsSharingConsented() {
        return analyticsSharingConsented;
    }

    public void setAnalyticsSharingConsented(boolean analyticsSharingConsented) {
        this.analyticsSharingConsented = analyticsSharingConsented;
    }

    public boolean isGlobalLeaderboardConsented() {
        return globalLeaderboardConsented;
    }

    public void setGlobalLeaderboardConsented(boolean globalLeaderboardConsented) {
        this.globalLeaderboardConsented = globalLeaderboardConsented;
    }

    public boolean isResearchParticipationConsented() {
        return researchParticipationConsented;
    }

    public void setResearchParticipationConsented(boolean researchParticipationConsented) {
        this.researchParticipationConsented = researchParticipationConsented;
    }

    public String getDataRetentionPreference() {
        return dataRetentionPreference;
    }

    public void setDataRetentionPreference(String dataRetentionPreference) {
        this.dataRetentionPreference = dataRetentionPreference;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isParentalConsentGranted() {
        return parentalConsentGranted;
    }

    public void setParentalConsentGranted(boolean parentalConsentGranted) {
        this.parentalConsentGranted = parentalConsentGranted;
    }

    public LocalDateTime getConsentedAt() {
        return consentedAt;
    }

    public void setConsentedAt(LocalDateTime consentedAt) {
        this.consentedAt = consentedAt;
    }
}
