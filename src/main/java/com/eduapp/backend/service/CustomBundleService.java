package com.eduapp.backend.service;

import com.eduapp.backend.dto.CustomBundleDto;
import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.dto.PaymentResult;
import com.eduapp.backend.mapper.PaperMapper;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing custom bundles created by students.
 */
@Service
@Transactional
public class CustomBundleService {

    private static final Logger logger = LoggerFactory.getLogger(CustomBundleService.class);
    private static final int MAX_PAPERS_PER_BUNDLE = 5;
    private static final String PRICE_CONFIG_KEY = "custom_bundle_paper_price";

    private final CustomBundleRepository customBundleRepository;
    private final PaperRepository paperRepository;
    private final UserRepository userRepository;
    private final SystemConfigRepository systemConfigRepository;

    // WALLET_DISABLED: WalletService replaced with PaymentService
    // private final WalletService walletService;
    private final PaymentService paymentService;
    private final PaperMapper paperMapper;

    public CustomBundleService(CustomBundleRepository customBundleRepository,
            PaperRepository paperRepository,
            UserRepository userRepository,
            SystemConfigRepository systemConfigRepository,
            PaymentService paymentService,
            PaperMapper paperMapper) {
        this.customBundleRepository = customBundleRepository;
        this.paperRepository = paperRepository;
        this.userRepository = userRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.paymentService = paymentService;
        this.paperMapper = paperMapper;
    }

    /**
     * Get the user's current draft bundle (if any).
     */
    public CustomBundleDto getMyDraft(Long userId) {
        return customBundleRepository.findByCreatorIdAndStatus(userId, CustomBundleStatus.CREATED)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Get all bundles owned by a user (purchased or approved).
     */
    public List<CustomBundleDto> getMyBundles(Long userId) {
        return customBundleRepository.findByCreatorIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(bundle -> bundle.getStatus() == CustomBundleStatus.PURCHASED
                        || bundle.getStatus() == CustomBundleStatus.APPROVED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific custom bundle by ID.
     * Accessible by creator (any status) or public (if approved).
     */
    public CustomBundleDto getBundleById(Long bundleId, Long userId) {
        CustomBundle bundle = customBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        // Access check: Creator or Approved
        boolean isCreator = bundle.getCreator().getId().equals(userId);
        boolean isApproved = bundle.getStatus() == CustomBundleStatus.APPROVED;

        if (!isCreator && !isApproved) {
            throw new SecurityException("Access denied: You are not the creator and this bundle is not public.");
        }

        return toDto(bundle);
    }

    /**
     * Create a new custom bundle.
     * User can only have one CREATED bundle at a time.
     */
    public CustomBundleDto createBundle(Long userId, String name, String description) {
        // Check if user already has a CREATED bundle
        long existingDrafts = customBundleRepository.countByCreatorIdAndStatus(userId, CustomBundleStatus.CREATED);
        if (existingDrafts > 0) {
            throw new IllegalStateException("You already have a draft bundle. Please complete or delete it first.");
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CustomBundle bundle = new CustomBundle(creator, name, description);
        CustomBundle saved = customBundleRepository.save(bundle);

        logger.info("User {} created custom bundle: {}", userId, saved.getId());
        return toDto(saved);
    }

    /**
     * Add a paper to the custom bundle.
     */
    public CustomBundleDto addPaper(Long bundleId, Long paperId, Long userId) {
        CustomBundle bundle = getEditableBundle(bundleId, userId);

        if (bundle.getPapers().size() >= MAX_PAPERS_PER_BUNDLE) {
            throw new IllegalStateException(
                    "Cannot add more than " + MAX_PAPERS_PER_BUNDLE + " papers to a custom bundle");
        }

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        if (!bundle.getPapers().contains(paper)) {
            bundle.getPapers().add(paper);
            CustomBundle saved = customBundleRepository.save(bundle);
            logger.info("Added paper {} to custom bundle {}", paperId, bundleId);
            return toDto(saved);
        }

        return toDto(bundle);
    }

    /**
     * Remove a paper from the custom bundle.
     */
    public CustomBundleDto removePaper(Long bundleId, Long paperId, Long userId) {
        CustomBundle bundle = getEditableBundle(bundleId, userId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        bundle.getPapers().remove(paper);
        CustomBundle saved = customBundleRepository.save(bundle);

        logger.info("Removed paper {} from custom bundle {}", paperId, bundleId);
        return toDto(saved);
    }

    /**
     * Delete a draft bundle.
     */
    public void deleteBundle(Long bundleId, Long userId) {
        CustomBundle bundle = getEditableBundle(bundleId, userId);
        customBundleRepository.delete(bundle);
        logger.info("Deleted custom bundle {}", bundleId);
    }

    /**
     * Purchase a custom bundle.
     * Bundle becomes immediately usable after purchase.
     *
     * WALLET_DISABLED: Previously used walletService.debit().
     * Now verifies payment via PayHere gateway.
     */
    public CustomBundleDto purchaseBundle(Long bundleId, Long userId, String paymentReference) {
        CustomBundle bundle = getEditableBundle(bundleId, userId);

        if (bundle.getPapers().isEmpty()) {
            throw new IllegalStateException("Cannot purchase an empty bundle");
        }

        // Calculate total price
        BigDecimal pricePerPaper = getPricePerPaper();
        BigDecimal totalPrice = pricePerPaper.multiply(new BigDecimal(bundle.getPapers().size()));

        // WALLET_DISABLED: Previously debited wallet
        // User user = userRepository.findById(userId)
        // .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // walletService.debit(user, totalPrice, "Purchase of custom bundle: " +
        // bundle.getName());

        // Verify payment via PayHere gateway
        PaymentResult paymentResult = paymentService.verifyPayment(paymentReference, totalPrice);
        if (paymentResult.getStatus() != PaymentResult.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment verification failed: " + paymentResult.getMessage());
        }

        // Update bundle status
        bundle.setTotalPrice(totalPrice);
        bundle.setStatus(CustomBundleStatus.PURCHASED);
        bundle.setPurchasedAt(LocalDateTime.now());

        CustomBundle saved = customBundleRepository.save(bundle);
        logger.info("User {} purchased custom bundle {} for ${}", userId, bundleId, totalPrice);

        return toDto(saved);
    }

    /**
     * Get all bundles pending approval (admin).
     */
    public List<CustomBundleDto> getPendingApproval() {
        return customBundleRepository.findByStatusOrderByPurchasedAtAsc(CustomBundleStatus.PURCHASED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all approved bundles (admin).
     */
    public List<CustomBundleDto> getApprovedBundles() {
        return customBundleRepository.findApprovedBundles()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Approve a custom bundle (admin).
     * Makes it visible in the public bundles list.
     */
    public CustomBundleDto approveBundle(Long bundleId, Long adminId) {
        CustomBundle bundle = customBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        if (bundle.getStatus() != CustomBundleStatus.PURCHASED) {
            throw new IllegalStateException("Only PURCHASED bundles can be approved");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        bundle.setStatus(CustomBundleStatus.APPROVED);
        bundle.setApprovedAt(LocalDateTime.now());
        bundle.setApprovedBy(admin);

        CustomBundle saved = customBundleRepository.save(bundle);
        logger.info("Admin {} approved custom bundle {}", adminId, bundleId);

        return toDto(saved);
    }

    /**
     * Get current price per paper from system config.
     */
    public BigDecimal getPricePerPaper() {
        return systemConfigRepository.findByKey(PRICE_CONFIG_KEY)
                .map(config -> new BigDecimal(config.getValue()))
                .orElse(new BigDecimal("2.00")); // Default to $2
    }

    /**
     * Update price per paper (admin).
     */
    public void updatePricePerPaper(BigDecimal newPrice) {
        SystemConfig config = systemConfigRepository.findByKey(PRICE_CONFIG_KEY)
                .orElse(new SystemConfig(PRICE_CONFIG_KEY, newPrice.toString(),
                        "Price per paper when creating custom bundles"));

        config.setValue(newPrice.toString());
        systemConfigRepository.save(config);
        logger.info("Updated custom bundle paper price to ${}", newPrice);
    }

    /**
     * Get a bundle that can be edited (must be CREATED and owned by user).
     */
    private CustomBundle getEditableBundle(Long bundleId, Long userId) {
        CustomBundle bundle = customBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        if (!bundle.getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to edit this bundle");
        }

        if (bundle.getStatus() != CustomBundleStatus.CREATED) {
            throw new IllegalStateException("Only draft bundles can be edited");
        }

        return bundle;
    }

    /**
     * Convert entity to DTO.
     */
    private CustomBundleDto toDto(CustomBundle bundle) {
        CustomBundleDto dto = new CustomBundleDto();
        dto.setId(bundle.getId());
        dto.setCreatorId(bundle.getCreator().getId());
        dto.setCreatorName(bundle.getCreator().getName());
        dto.setName(bundle.getName());
        dto.setDescription(bundle.getDescription());
        dto.setStatus(bundle.getStatus().name());
        dto.setPaperIds(bundle.getPapers().stream().map(Paper::getId).collect(Collectors.toList()));
        dto.setPapers(paperMapper.toDtoList(bundle.getPapers()));
        dto.setTotalPrice(bundle.getTotalPrice());
        dto.setCreatedAt(bundle.getCreatedAt());
        dto.setPurchasedAt(bundle.getPurchasedAt());
        dto.setApprovedAt(bundle.getApprovedAt());

        if (bundle.getApprovedBy() != null) {
            dto.setApprovedById(bundle.getApprovedBy().getId());
            dto.setApprovedByName(bundle.getApprovedBy().getName());
        }

        return dto;
    }
}
