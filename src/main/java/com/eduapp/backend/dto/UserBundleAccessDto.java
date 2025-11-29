package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for user's bundle access information.
 * Shows which bundles a user has access to and how they got access.
 */
public class UserBundleAccessDto {
    private Long accessId;
    private Long bundleId;
    private String bundleName;
    private LocalDateTime purchasedAt;
    private Boolean grantedByAdmin;
    private Long grantedBy;
    private String grantReason;

    public UserBundleAccessDto() {
    }

    public UserBundleAccessDto(Long accessId, Long bundleId, String bundleName,
            LocalDateTime purchasedAt, Boolean grantedByAdmin,
            Long grantedBy, String grantReason) {
        this.accessId = accessId;
        this.bundleId = bundleId;
        this.bundleName = bundleName;
        this.purchasedAt = purchasedAt;
        this.grantedByAdmin = grantedByAdmin;
        this.grantedBy = grantedBy;
        this.grantReason = grantReason;
    }

    public Long getAccessId() {
        return accessId;
    }

    public void setAccessId(Long accessId) {
        this.accessId = accessId;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(Long bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Boolean getGrantedByAdmin() {
        return grantedByAdmin;
    }

    public void setGrantedByAdmin(Boolean grantedByAdmin) {
        this.grantedByAdmin = grantedByAdmin;
    }

    public Long getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    public String getGrantReason() {
        return grantReason;
    }

    public void setGrantReason(String grantReason) {
        this.grantReason = grantReason;
    }
}
