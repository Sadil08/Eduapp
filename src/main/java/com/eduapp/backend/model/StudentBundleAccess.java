package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_bundle_accesses")
public class StudentBundleAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "bundle_id", nullable = false)
    private PaperBundle bundle;

    private LocalDateTime purchasedAt = LocalDateTime.now();

    @Column
    private String paymentId;

    /**
     * Indicates whether this access was granted by an admin (true) or purchased by
     * student (false)
     */
    @Column(name = "granted_by_admin")
    private Boolean grantedByAdmin = false;

    /**
     * Admin user ID who granted access (if grantedByAdmin is true)
     */
    @Column(name = "granted_by")
    private Long grantedBy;

    /**
     * Reason for granting access (audit trail)
     */
    @Column(name = "grant_reason")
    private String grantReason;

    public StudentBundleAccess() {
    }

    public StudentBundleAccess(User student, PaperBundle bundle, String paymentId) {
        this.student = student;
        this.bundle = bundle;
        this.paymentId = paymentId;
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public PaperBundle getBundle() {
        return bundle;
    }

    public void setBundle(PaperBundle bundle) {
        this.bundle = bundle;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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