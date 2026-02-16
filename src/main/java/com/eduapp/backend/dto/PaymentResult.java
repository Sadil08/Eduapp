package com.eduapp.backend.dto;

import java.math.BigDecimal;

/**
 * DTO representing the result of a payment operation.
 * Used by PaymentService implementations.
 */
public class PaymentResult {

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    private String paymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String gatewayReference;
    private String message;

    public PaymentResult() {
    }

    public PaymentResult(String paymentId, PaymentStatus status, BigDecimal amount, String gatewayReference,
            String message) {
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.gatewayReference = gatewayReference;
        this.message = message;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
