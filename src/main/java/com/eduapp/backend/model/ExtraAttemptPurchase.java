package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing extra attempt purchases for papers.
 * Tracks when users purchase additional attempts beyond the free limit.
 */
@Entity
@Table(name = "extra_attempt_purchases")
public class ExtraAttemptPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column(nullable = false)
    private Integer attemptsGranted;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt = LocalDateTime.now();

    @Column(name = "payment_id")
    private String paymentId;

    // Constructors
    public ExtraAttemptPurchase() {
    }

    public ExtraAttemptPurchase(User user, Paper paper, Integer attemptsGranted, BigDecimal pricePaid,
            String paymentId) {
        this.user = user;
        this.paper = paper;
        this.attemptsGranted = attemptsGranted;
        this.pricePaid = pricePaid;
        this.paymentId = paymentId;
        this.purchasedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public Integer getAttemptsGranted() {
        return attemptsGranted;
    }

    public void setAttemptsGranted(Integer attemptsGranted) {
        this.attemptsGranted = attemptsGranted;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(BigDecimal pricePaid) {
        this.pricePaid = pricePaid;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
