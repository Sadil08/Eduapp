package com.eduapp.backend.dto;

/**
 * DTO for request to grant bundle access to a user.
 * Used by admin to give free access to specific users.
 */
public class GrantBundleAccessDto {
    private Long userId;
    private Long bundleId;
    private String reason;

    public GrantBundleAccessDto() {
    }

    public GrantBundleAccessDto(Long userId, Long bundleId, String reason) {
        this.userId = userId;
        this.bundleId = bundleId;
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(Long bundleId) {
        this.bundleId = bundleId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
