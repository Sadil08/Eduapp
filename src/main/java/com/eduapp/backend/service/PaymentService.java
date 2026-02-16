package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaymentResult;

import java.math.BigDecimal;

/**
 * Abstraction for payment gateway operations.
 * Implementations can be swapped (e.g., MockPayHere → real PayHere, OnePay,
 * etc.)
 * 
 * WALLET_DISABLED: This interface replaces the wallet-based payment flow.
 * When wallet is re-enabled, purchase services can switch back to
 * WalletService.debit().
 */
public interface PaymentService {

    /**
     * Initiate a payment through the gateway.
     *
     * @param amount      The amount to charge
     * @param description Description of the purchase
     * @param userId      The user making the payment
     * @return PaymentResult with payment ID and status
     */
    PaymentResult initiatePayment(BigDecimal amount, String description, Long userId);

    /**
     * Verify a payment using the gateway reference from the frontend.
     *
     * @param paymentReference The reference returned by the payment gateway on the
     *                         frontend
     * @param expectedAmount   The expected amount that should have been paid
     * @return PaymentResult with verification status
     */
    PaymentResult verifyPayment(String paymentReference, BigDecimal expectedAmount);

    /**
     * Get the status of a previously initiated payment.
     *
     * @param paymentId The payment ID to look up
     * @return PaymentResult with current status
     */
    PaymentResult getPaymentStatus(String paymentId);
}
