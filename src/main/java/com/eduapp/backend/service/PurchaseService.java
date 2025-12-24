package com.eduapp.backend.service;

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
import java.util.UUID;

@Service
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final CartService cartService;
    private final StudentBundleAccessRepository studentBundleAccessRepository;

    public PurchaseService(CartService cartService, StudentBundleAccessRepository studentBundleAccessRepository) {
        this.cartService = cartService;
        this.studentBundleAccessRepository = studentBundleAccessRepository;
    }

    @Transactional
    public void checkout(User user) {
        try {
            logger.info("Processing checkout for user: {}", user.getId());

            Cart cart = cartService.getMyCart(user.getId());

            if (cart.getBundles().isEmpty()) {
                logger.warn("User {} tried to checkout with an empty cart", user.getId());
                throw new RuntimeException("Cart is empty");
            }

            List<PaperBundle> bundles = new ArrayList<>(cart.getBundles());
            logger.info("User {} has {} bundles in cart", user.getId(), bundles.size());

            String paymentId = "MOCK-" + UUID.randomUUID().toString();

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
