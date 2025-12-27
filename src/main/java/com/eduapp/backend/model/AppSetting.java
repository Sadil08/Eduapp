package com.eduapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_settings")
public class AppSetting {

    @Id
    @Column(name = "settings_key")
    private String key;

    @Column(name = "settings_value", nullable = false)
    private String value;

    public AppSetting() {
    }

    public AppSetting(String key, String value) {
        this.key = key;
        this.value = value;
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
    }
}
