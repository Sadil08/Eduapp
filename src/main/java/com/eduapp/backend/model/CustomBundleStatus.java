package com.eduapp.backend.model;

/**
 * Status of a custom bundle created by a student.
 * 
 * CREATED - Draft state, bundle created but not yet purchased
 * PURCHASED - User has paid, bundle is usable, awaiting admin approval for public visibility
 * APPROVED - Admin approved, bundle can be shown in public bundles list
 */
public enum CustomBundleStatus {
    /**
     * Bundle is in draft state - created but not purchased
     */
    CREATED,
    
    /**
     * Bundle has been purchased by the user and is immediately usable.
     * Waiting for admin approval to appear in public bundles list.
     */
    PURCHASED,
    
    /**
     * Bundle has been approved by admin and can be displayed in the public bundles list.
     */
    APPROVED
}
