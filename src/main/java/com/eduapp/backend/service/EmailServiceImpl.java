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

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

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
            // N-S3: never log the OTP value — it's a credential. (Was logged unconditionally.)

            if (fromEmail != null && !fromEmail.isEmpty()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                String firstUrl = frontendUrl.split(",")[0].trim();
                String activateLink = firstUrl + "/set-password?email="
                        + java.net.URLEncoder.encode(to, java.nio.charset.StandardCharsets.UTF_8);
                message.setSubject("EduApp — set your password");
                message.setText("Hello,\n\n" +
                        "Use this one-time code to set your password:\n" +
                        "OTP: " + otp + "\n\n" +
                        "Set your password here: " + activateLink + "\n" +
                        "(First-time school admins & teachers: this activates your account. " +
                        "Then sign in normally with your email and new password.)\n\n" +
                        "This OTP is valid for 10 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\nEduApp Team");

                javaMailSender.send(message);
                logger.info("Password Reset Email sent successfully to {}", to);
            } else {
                logger.warn(
                        "SMTP not configured (spring.mail.username is empty). Password-reset email NOT sent.");
            }

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
