package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin user management.
 * Provides user listing, bundle access management, and attempt limit control.
 */
@Service
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final StudentBundleAccessRepository accessRepository;
    private final PaperBundleRepository bundleRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final PaperRepository paperRepository;

    public AdminUserService(UserRepository userRepository,
            StudentBundleAccessRepository accessRepository,
            PaperBundleRepository bundleRepository,
            StudentPaperAttemptRepository attemptRepository,
            PaperRepository paperRepository) {
        this.userRepository = userRepository;
        this.accessRepository = accessRepository;
        this.bundleRepository = bundleRepository;
        this.attemptRepository = attemptRepository;
        this.paperRepository = paperRepository;
    }

    /**
     * Get all users with statistics
     */
    public List<AdminUserDto> getAllUsers() {
        logger.info("Fetching all users with statistics");

        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            int bundleCount = accessRepository.findByStudentId(user.getId()).size();
            int attemptCount = (int) attemptRepository.countByStudentId(user.getId());

            return new AdminUserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole() != null ? user.getRole().name() : null,
                    user.getCreatedAt(),
                    bundleCount,
                    attemptCount);
        }).collect(Collectors.toList());
    }

    /**
     * Get user details
     */
    public AdminUserDto getUserDetails(Long userId) {
        logger.info("Fetching details for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int bundleCount = accessRepository.findByStudentId(userId).size();
        int attemptCount = (int) attemptRepository.countByStudentId(userId);

        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getCreatedAt(),
                bundleCount,
                attemptCount);
    }

    /**
     * Get user's bundle access list
     */
    public List<UserBundleAccessDto> getUserBundleAccess(Long userId) {
        logger.info("Fetching bundle access for user ID: {}", userId);

        List<StudentBundleAccess> accesses = accessRepository.findByStudentId(userId);

        return accesses.stream().map(access -> new UserBundleAccessDto(
                access.getId(),
                access.getBundle().getId(),
                access.getBundle().getName(),
                access.getPurchasedAt(),
                access.getGrantedByAdmin(),
                access.getGrantedBy(),
                access.getGrantReason())).collect(Collectors.toList());
    }

    /**
     * Grant bundle access to a user (admin action)
     */
    @Transactional
    public StudentBundleAccess grantBundleAccess(Long userId, Long bundleId, Long adminId, String reason) {
        logger.info("Admin {} granting bundle {} access to user {}", adminId, bundleId, userId);

        // Check if access already exists
        StudentBundleAccess existing = accessRepository.findByStudentIdAndBundleId(userId, bundleId);
        if (existing != null) {
            logger.warn("User already has access to this bundle");
            throw new IllegalArgumentException("User already has access to this bundle");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PaperBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        StudentBundleAccess access = new StudentBundleAccess();
        access.setStudent(user);
        access.setBundle(bundle);
        access.setPurchasedAt(LocalDateTime.now());
        access.setGrantedByAdmin(true);
        access.setGrantedBy(adminId);
        access.setGrantReason(reason);
        access.setPaymentId(null); // No payment for admin-granted access

        StudentBundleAccess saved = accessRepository.save(access);
        logger.info("Bundle access granted: user {}, bundle {}", user.getUsername(), bundle.getName());

        return saved;
    }

    /**
     * Revoke bundle access from a user
     */
    @Transactional
    public void revokeBundleAccess(Long userId, Long bundleId) {
        logger.info("Revoking bundle {} access from user {}", bundleId, userId);

        StudentBundleAccess access = accessRepository.findByStudentIdAndBundleId(userId, bundleId);
        if (access == null) {
            throw new IllegalArgumentException("User does not have access to this bundle");
        }

        accessRepository.delete(access);
        logger.info("Bundle access revoked for user ID: {}", userId);
    }

    /**
     * Get user's attempt information across all accessible papers
     */
    public List<UserAttemptInfoDto> getUserAttempts(Long userId) {
        logger.info("Fetching attempt information for user ID: {}", userId);

        // Get all bundles user has access to
        List<StudentBundleAccess> accesses = accessRepository.findByStudentId(userId);

        return accesses.stream()
                .flatMap(access -> access.getBundle().getPapers().stream())
                .map(paper -> {
                    int attemptsMade = attemptRepository.countByStudentIdAndPaperId(userId, paper.getId());
                    int maxAttempts = paper.getMaxFreeAttempts() != null ? paper.getMaxFreeAttempts() : 0;

                    return new UserAttemptInfoDto(
                            userId,
                            paper.getId(),
                            paper.getName(),
                            attemptsMade,
                            maxAttempts);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update attempt limit for a user on a specific paper
     * Note: This modifies the paper's maxFreeAttempts for all users.
     * For per-user limits, would need PaperAttemptLimit entity.
     */
    @Transactional
    public void updateAttemptLimit(Long userId, Long paperId, int newLimit) {
        logger.info("Updating attempt limit for user {} on paper {} to {}", userId, paperId, newLimit);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        // For now, we'll update the paper's global limit
        // TODO: Implement per-user limits with PaperAttemptLimit entity
        var paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        paper.setMaxFreeAttempts(newLimit);
        paperRepository.save(paper);

        logger.info("Attempt limit updated for paper: {}", paper.getName());
        logger.warn("Note: This changes the limit for ALL users. Consider implementing per-user limits.");
    }
}
