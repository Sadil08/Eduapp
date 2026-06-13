package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A tenant in the multi-tenant platform (PRODUCT_BUSINESS_PLAN.md §9). Every
 * school-tier row carries a {@code school_id} pointing here; global-tier users
 * and data have a null school. This entity is the tenant itself, not tenant-scoped.
 */
@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column
    private String country;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "plan_tier")
    @Enumerated(EnumType.STRING)
    private SchoolPlanTier planTier = SchoolPlanTier.PILOT;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchoolStatus status = SchoolStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public School() {
    }

    public School(String name, String country, String contactEmail) {
        this.name = name;
        this.country = country;
        this.contactEmail = contactEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public SchoolPlanTier getPlanTier() {
        return planTier;
    }

    public void setPlanTier(SchoolPlanTier planTier) {
        this.planTier = planTier;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
