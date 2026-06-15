package com.eduapp.backend.it;

import com.eduapp.backend.dto.PaymentResult;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.CartService;
import com.eduapp.backend.service.PaymentService;
import com.eduapp.backend.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * N-R3 — a verified payment can be consumed only once. Replaying / reusing the same payment
 * reference for a second checkout must not grant additional (unpaid) bundles.
 */
class PurchaseIdempotencyIT extends AbstractIntegrationTest {

    @Autowired PurchaseService purchaseService;
    @Autowired CartService cartService;
    @Autowired UserRepository userRepository;
    @Autowired PaperBundleRepository bundleRepository;
    @Autowired StudentBundleAccessRepository accessRepository;

    @MockBean PaymentService paymentService;

    private User newUser() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("buy-" + u + "@t.test", "x", "buy-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("BUY" + u);
        return userRepository.save(user);
    }

    private PaperBundle newBundle(String name) {
        PaperBundle b = new PaperBundle();
        b.setName(name + "-" + UUID.randomUUID().toString().substring(0, 5));
        b.setPrice(new BigDecimal("100.00"));
        return bundleRepository.save(b);
    }

    @Test
    void reusingPaymentReference_doesNotGrantSecondTime() {
        User user = newUser();
        PaperBundle b1 = newBundle("B1");
        PaperBundle b2 = newBundle("B2");

        // Same verified payment (same paymentId) is returned for both checkout attempts.
        when(paymentService.verifyPayment(any(), any())).thenReturn(
                new PaymentResult("PID-FIXED", PaymentResult.PaymentStatus.SUCCESS,
                        new BigDecimal("100.00"), "REF-1", "ok"));

        // 1st checkout: bundle b1 -> granted, cart cleared.
        cartService.addToCart(user.getId(), b1.getId(), bundleRepository);
        purchaseService.checkout(user, "REF-1");
        assertThat(accessRepository.findByStudentId(user.getId())).hasSize(1);

        // 2nd checkout reusing the SAME payment: add b2, replay REF-1 -> must NOT grant b2.
        cartService.addToCart(user.getId(), b2.getId(), bundleRepository);
        purchaseService.checkout(user, "REF-1");

        assertThat(accessRepository.findByStudentId(user.getId()))
                .as("reused payment must not grant a second bundle")
                .hasSize(1);
    }
}
