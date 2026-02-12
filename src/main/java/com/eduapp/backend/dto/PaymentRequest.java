package com.eduapp.backend.dto;

import java.math.BigDecimal;

/**
 * DTO for incoming payment requests from the frontend.
 * Contains the payment reference from the PayHere gateway.
 */
public class PaymentRequest {

    private String paymentReference;
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(String paymentReference, BigDecimal amount) {
        this.paymentReference = paymentReference;
        this.amount = amount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
