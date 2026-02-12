package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaymentResult;
import com.eduapp.backend.model.Cart;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final CartService cartService;
    private final StudentBundleAccessRepository studentBundleAccessRepository;
    private final PaymentService paymentService;
    private final com.eduapp.backend.repository.UserRepository userRepository;

    // WALLET_DISABLED: WalletService dependency removed from constructor.
    // Re-add WalletService here when wallet is re-enabled.
    // private final WalletService walletService;

    public PurchaseService(CartService cartService, StudentBundleAccessRepository studentBundleAccessRepository,
            PaymentService paymentService, com.eduapp.backend.repository.UserRepository userRepository) {
        this.cartService = cartService;
        this.studentBundleAccessRepository = studentBundleAccessRepository;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void checkout(User user, String paymentReference) {

        try {
            logger.info("Processing checkout for user: {}", user.getId());

            Cart cart = cartService.getOrCreateCart(user.getId());

            if (cart.getBundles().isEmpty()) {
                logger.warn("User {} tried to checkout with an empty cart", user.getId());
                throw new RuntimeException("Cart is empty");
            }

            List<PaperBundle> bundles = new ArrayList<>(cart.getBundles());
            logger.info("User {} has {} bundles in cart", user.getId(), bundles.size());

            java.math.BigDecimal totalAmount = bundles.stream()
                    .map(PaperBundle::getPrice)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            logger.info("Total checkout amount: {}", totalAmount);

            // WALLET_DISABLED: Previously used walletService.debit(user, totalAmount,
            // description)
            // When re-enabling wallet, uncomment the line below and remove the PayHere
            // verification:
            // walletService.debit(user, totalAmount, "Purchase of " + bundles.size() + "
            // bundles");

            // Verify payment via PayHere gateway
            PaymentResult paymentResult = paymentService.verifyPayment(paymentReference, totalAmount);
            if (paymentResult.getStatus() != PaymentResult.PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment verification failed: " + paymentResult.getMessage());
            }

            String paymentId = paymentResult.getPaymentId();

            for (PaperBundle bundle : bundles) {
                logger.info("Processing bundle: {} (ID: {}) for user: {}", bundle.getName(), bundle.getId(),
                        user.getId());

                boolean alreadyOwned = studentBundleAccessRepository.existsByStudentIdAndBundleId(user.getId(),
                        bundle.getId());
                if (alreadyOwned) {
                    logger.info("User {} already owns bundle {}, skipping", user.getId(), bundle.getId());
                    continue;
                }

                try {
                    StudentBundleAccess access = new StudentBundleAccess(user, bundle, paymentId);
                    access.setPricePaid(bundle.getPrice());
                    StudentBundleAccess saved = studentBundleAccessRepository.save(access);
                    logger.info("Saved bundle access (ID: {}) for user: {}", saved.getId(), user.getId());
                } catch (Exception e) {
                    logger.error("Failed to save student bundle access for user: {} and bundle: {}. Error: {}",
                            user.getId(), bundle.getId(), e.getMessage(), e);
                    throw new RuntimeException("Purchase failed for bundle: " + bundle.getName(), e);
                }
            }

            // Clear cart
            cart.getBundles().clear();
            cartService.save(cart);

            logger.info("Checkout completed successfully for user: {}", user.getId());
        } catch (Exception e) {
            logger.error("Checkout failed for user: {}. Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
