package com.eduapp.backend.dto;

import java.time.LocalDate;

public class ConsentRequest {
    private String consentVersion;
    private boolean analyticsSharing;
    private boolean leaderboard;
    private boolean research;
    private String dataRetentionPreference;
    private LocalDate dateOfBirth;
    private boolean parentalConsentGranted;

    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    public boolean isAnalyticsSharing() { return analyticsSharing; }
    public void setAnalyticsSharing(boolean analyticsSharing) { this.analyticsSharing = analyticsSharing; }
    public boolean isLeaderboard() { return leaderboard; }
    public void setLeaderboard(boolean leaderboard) { this.leaderboard = leaderboard; }
    public boolean isResearch() { return research; }
    public void setResearch(boolean research) { this.research = research; }
    public String getDataRetentionPreference() { return dataRetentionPreference; }
    public void setDataRetentionPreference(String dataRetentionPreference) { this.dataRetentionPreference = dataRetentionPreference; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public boolean isParentalConsentGranted() { return parentalConsentGranted; }
    public void setParentalConsentGranted(boolean parentalConsentGranted) { this.parentalConsentGranted = parentalConsentGranted; }
}
