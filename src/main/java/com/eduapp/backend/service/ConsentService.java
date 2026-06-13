package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentConsentRecord;
import com.eduapp.backend.repository.StudentConsentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Records a user's layered consent (PRODUCT_BUSINESS_PLAN.md §10.2). Enforces the age
 * gate: an under-16 user cannot grant consent without recorded parental consent.
 */
@Service
public class ConsentService {

    private final StudentConsentRecordRepository repository;

    public ConsentService(StudentConsentRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StudentConsentRecord record(Long userId, String consentVersion,
            boolean analyticsSharing, boolean leaderboard, boolean research,
            String dataRetentionPreference, LocalDate dateOfBirth, boolean parentalConsentGranted) {

        if (StudentConsentRecord.requiresParentalConsent(dateOfBirth) && !parentalConsentGranted) {
            throw new IllegalArgumentException(
                    "Users under " + StudentConsentRecord.PARENTAL_CONSENT_AGE
                            + " require parental consent before sharing data");
        }

        StudentConsentRecord record = repository.findByUserId(userId).orElseGet(StudentConsentRecord::new);
        record.setUserId(userId);
        record.setConsentVersion(consentVersion != null ? consentVersion : "v1");
        record.setAnalyticsSharingConsented(analyticsSharing);
        record.setGlobalLeaderboardConsented(leaderboard);
        record.setResearchParticipationConsented(research);
        record.setDataRetentionPreference(dataRetentionPreference);
        record.setDateOfBirth(dateOfBirth);
        record.setParentalConsentGranted(parentalConsentGranted);
        return repository.save(record);
    }
}
