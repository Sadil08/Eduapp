package com.eduapp.backend.service;

import com.eduapp.backend.model.Cart;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.CartRepository;
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

    private final CartRepository cartRepository;
    private final StudentBundleAccessRepository studentBundleAccessRepository;

    public PurchaseService(CartRepository cartRepository, StudentBundleAccessRepository studentBundleAccessRepository) {
        this.cartRepository = cartRepository;
        this.studentBundleAccessRepository = studentBundleAccessRepository;
    }

    @Transactional
    public void checkout(User user) {
        logger.info("Processing checkout for user: {}", user.getId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        if (cart.getBundles().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<PaperBundle> bundles = cart.getBundles();
        String paymentId = "MOCK-" + UUID.randomUUID().toString(); // Mock payment ID

        for (PaperBundle bundle : bundles) {
            // Check if already purchased to avoid duplicates
            boolean alreadyOwned = studentBundleAccessRepository.existsByStudentIdAndBundleId(user.getId(),
                    bundle.getId());
            if (alreadyOwned) {
                logger.info("User {} already owns bundle {}, skipping", user.getId(), bundle.getId());
                continue;
            }

            StudentBundleAccess access = new StudentBundleAccess(user, bundle, paymentId);
            access.setPricePaid(bundle.getPrice());
            studentBundleAccessRepository.save(access);
        }

        // Clear cart
        cart.setBundles(new ArrayList<>());
        cartRepository.save(cart);

        logger.info("Checkout completed successfully for user: {}", user.getId());
    }
}
