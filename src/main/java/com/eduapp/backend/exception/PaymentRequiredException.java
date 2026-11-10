package com.eduapp.backend.exception;

/**
 * Thrown when an operation that grants paid content is attempted without a payment.
 */
public class PaymentRequiredException extends RuntimeException {
    public PaymentRequiredException(String message) {
        super(message);
    }
}
