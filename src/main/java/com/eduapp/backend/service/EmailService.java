package com.eduapp.backend.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String otp);
}
