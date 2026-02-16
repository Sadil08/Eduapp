package com.eduapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // Check if mail sender is configured (simple check based on host availability
    // is hard,
    // but try-catch in send is better)

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String otp) {
        try {
            logger.info("Preparing to send Password Reset OTP to: {}", to);

            // Log OTP regardless for dev/testing
            logger.info("========================================");
            logger.info("PASSWORD RESET OTP for {}: {}", to, otp);
            logger.info("========================================");

            if (fromEmail != null && !fromEmail.isEmpty()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject("EduApp Password Reset Request");
                message.setText("Hello,\n\n" +
                        "You have requested to reset your password.\n" +
                        "Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\nEduApp Team");

                javaMailSender.send(message);
                logger.info("Password Reset Email sent successfully to {}", to);
            } else {
                logger.warn(
                        "SMTP not configured (spring.mail.username is empty). Email NOT sent. Check console for OTP.");
            }

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
