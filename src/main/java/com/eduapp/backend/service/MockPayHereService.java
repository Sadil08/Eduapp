package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock implementation of PaymentService that simulates PayHere gateway
 * behavior.
 * 
 * WALLET_DISABLED: This mock service replaces wallet-based payments.
 * Replace this with a real PayHere integration when ready:
 * - Use @Profile("production") on real impl and @Profile("!production") on this
 * mock
 * - Or simply replace this class with PayHereService that calls the real API
 */
@Service
public class MockPayHereService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MockPayHereService.class);

    @Override
    public PaymentResult initiatePayment(BigDecimal amount, String description, Long userId) {
        String paymentId = "PH-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        logger.info("[MOCK PayHere] Payment initiated - ID: {}, Amount: {}, User: {}, Description: {}",
                paymentId, amount, userId, description);

        // Mock: always succeeds
        return new PaymentResult(
                paymentId,
                PaymentResult.PaymentStatus.SUCCESS,
                amount,
                "MOCK-GW-" + UUID.randomUUID().toString().substring(0, 8),
                "Mock payment processed successfully");
    }

    @Override
    public PaymentResult verifyPayment(String paymentReference, BigDecimal expectedAmount) {
        logger.info("[MOCK PayHere] Verifying payment reference: {}, Expected amount: {}",
                paymentReference, expectedAmount);

        // Mock: always verifies successfully
        return new PaymentResult(
                paymentReference,
                PaymentResult.PaymentStatus.SUCCESS,
                expectedAmount,
                paymentReference,
                "Mock payment verified successfully");
    }

    @Override
    public PaymentResult getPaymentStatus(String paymentId) {
        logger.info("[MOCK PayHere] Checking status for payment: {}", paymentId);

        return new PaymentResult(
                paymentId,
                PaymentResult.PaymentStatus.SUCCESS,
                null,
                paymentId,
                "Mock payment status: completed");
    }
}
