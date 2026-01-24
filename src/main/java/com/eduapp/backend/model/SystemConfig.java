package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing system-wide configuration key-value pairs.
 * Used for admin-configurable settings like custom bundle pricing.
 */
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public SystemConfig() {
    }

    public SystemConfig(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
