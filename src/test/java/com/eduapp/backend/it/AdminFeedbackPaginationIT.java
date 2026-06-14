package com.eduapp.backend.it;

import com.eduapp.backend.model.Review;
import com.eduapp.backend.model.ReviewStatus;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ReviewRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** SCALE-4 — admin feedback (reviews) listing is paginated, status-filterable, and ADMIN-only. */
class AdminFeedbackPaginationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private HttpHeaders authAs(Role role) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(role + "-" + u + "@f.test", "x", role + "-" + u);
        user.setRole(role);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId(), user.getReferralCode()));
        return h;
    }

    private User seedReviewAuthor() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("rev-" + u + "@f.test", "x", "rev-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("RA" + u);
        return userRepository.save(user);
    }

    @Test
    void adminReviews_arePagedFilteredAndAdminOnly() {
        User author = seedReviewAuthor();
        int pending = 12, approved = 3;
        for (int i = 0; i < pending; i++) {
            Review r = new Review(author, 5, "great " + UUID.randomUUID());
            r.setStatus(ReviewStatus.PENDING);
            reviewRepository.save(r);
        }
        for (int i = 0; i < approved; i++) {
            Review r = new Review(author, 4, "good " + UUID.randomUUID());
            r.setStatus(ReviewStatus.APPROVED);
            reviewRepository.save(r);
        }

        // Student cannot reach the admin endpoint.
        ResponseEntity<String> denied = rest.exchange("/api/admin/feedback/reviews",
                HttpMethod.GET, new HttpEntity<>(authAs(Role.STUDENT)), String.class);
        assertThat(denied.getStatusCode().value()).isIn(401, 403);

        HttpHeaders admin = authAs(Role.ADMIN);

        // First page has 10 of the (at least 15) reviews.
        ResponseEntity<Map> page0 = rest.exchange("/api/admin/feedback/reviews?page=0&size=10",
                HttpMethod.GET, new HttpEntity<>(admin), Map.class);
        assertThat(page0.getStatusCode().value()).isEqualTo(200);
        assertThat(((java.util.List<?>) page0.getBody().get("content"))).hasSize(10);
        assertThat(((Number) page0.getBody().get("totalElements")).longValue()).isGreaterThanOrEqualTo(15);

        // Status filter returns only pending; size cap keeps it bounded.
        ResponseEntity<Map> pendingPage = rest.exchange("/api/admin/feedback/reviews?status=PENDING&page=0&size=100",
                HttpMethod.GET, new HttpEntity<>(admin), Map.class);
        assertThat(((Number) pendingPage.getBody().get("totalElements")).longValue()).isGreaterThanOrEqualTo(pending);

        // Stats reflect the same counts.
        ResponseEntity<Map> stats = rest.exchange("/api/admin/feedback/stats",
                HttpMethod.GET, new HttpEntity<>(admin), Map.class);
        assertThat(((Number) stats.getBody().get("pendingReviews")).longValue()).isGreaterThanOrEqualTo(pending);
        assertThat(((Number) stats.getBody().get("approvedReviews")).longValue()).isGreaterThanOrEqualTo(approved);
    }
}
