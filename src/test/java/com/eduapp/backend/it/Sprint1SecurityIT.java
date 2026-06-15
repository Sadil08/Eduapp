package com.eduapp.backend.it;

import com.eduapp.backend.exception.BadRequestException;
import com.eduapp.backend.model.PasswordResetToken;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.PasswordResetTokenRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.UserService;
import com.eduapp.backend.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Sprint 1 (deep-audit P0): OTP hardening, input validation, wallet validation, no enumeration. */
class Sprint1SecurityIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserService userService;
    @Autowired WalletService walletService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordResetTokenRepository tokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User seedUser() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("s1-" + u + "@t.test", passwordEncoder.encode("correctpw1"), "s1-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    // ---------- N-S9 / N-S4 input validation ----------
    @Test
    void register_rejectsBadEmailAndShortPassword() {
        ResponseEntity<Map> resp = rest.postForEntity("/api/auth/register",
                Map.of("email", "not-an-email", "password", "x", "name", "X"), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsKey("fields");
    }

    // ---------- N-S1 OTP hashed at rest ----------
    @Test
    void otp_isStoredHashedNotPlaintext() {
        User user = seedUser();
        userService.forgotPassword(user.getEmail());
        PasswordResetToken token = tokenRepository.findByUser(user).orElseThrow();
        assertThat(token.getToken()).startsWith("$2"); // BCrypt hash, not a 6-digit code
        assertThat(token.getToken()).doesNotMatch("\\d{6}");
    }

    // ---------- N-S2 OTP brute-force lockout ----------
    @Test
    void otp_locksOutAfterFiveWrongAttempts() {
        User user = seedUser();
        userService.forgotPassword(user.getEmail());

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> userService.resetPassword(user.getEmail(), "000000", "newpassword1"))
                    .isInstanceOf(BadRequestException.class);
        }
        // After 5 wrong attempts the token is invalidated → it's gone.
        assertThat(tokenRepository.findByUser(user)).isEmpty();
    }

    // ---------- N-R1 wallet amount validation ----------
    @Test
    void wallet_rejectsNonPositiveTopUp() {
        User user = seedUser();
        assertThatThrownBy(() -> walletService.topUp(user.getId(), new BigDecimal("-5")))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> walletService.topUp(user.getId(), BigDecimal.ZERO))
                .isInstanceOf(BadRequestException.class);
        assertThat(userRepository.findById(user.getId()).orElseThrow().getWalletBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---------- N-S7 no user enumeration on login ----------
    @Test
    void login_unknownEmailAndWrongPassword_giveIdenticalError() {
        User user = seedUser();
        ResponseEntity<Map> unknown = rest.postForEntity("/api/auth/login",
                Map.of("email", "nobody-" + UUID.randomUUID() + "@t.test", "password", "whatever1"), Map.class);
        ResponseEntity<Map> wrongPw = rest.postForEntity("/api/auth/login",
                Map.of("email", user.getEmail(), "password", "wrongpassword1"), Map.class);

        assertThat(unknown.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(wrongPw.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(unknown.getBody().get("error")).isEqualTo(wrongPw.getBody().get("error"));
    }
}
