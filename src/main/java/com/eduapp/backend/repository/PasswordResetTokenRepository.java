package com.eduapp.backend.repository;

import com.eduapp.backend.model.PasswordResetToken;
import com.eduapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    // Cleanup expired tokens
    void deleteByExpiryDateLessThan(LocalDateTime now);
}
