package com.eduapp.backend.controller;

import com.eduapp.backend.dto.ConsentRequest;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.ConsentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Records the authenticated user's layered consent choices (WP-9). Any authenticated user
 * may set their own consent; the under-16 parental-consent gate is enforced in the service.
 */
@RestController
@RequestMapping("/api/consent")
public class ConsentController {

    private final ConsentService consentService;
    private final UserRepository userRepository;

    public ConsentController(ConsentService consentService, UserRepository userRepository) {
        this.consentService = consentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> record(@AuthenticationPrincipal UserDetails principal,
            @RequestBody ConsentRequest req) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var saved = consentService.record(user.getId(), req.getConsentVersion(),
                req.isAnalyticsSharing(), req.isLeaderboard(), req.isResearch(),
                req.getDataRetentionPreference(), req.getDateOfBirth(), req.isParentalConsentGranted());
        return ResponseEntity.ok(Map.of(
                "userId", saved.getUserId(),
                "analyticsSharingConsented", saved.isAnalyticsSharingConsented()));
    }
}
