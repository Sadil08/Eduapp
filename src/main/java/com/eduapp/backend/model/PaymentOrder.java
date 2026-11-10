package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A payment the server asked the gateway to collect. The amount is fixed at creation time from
 * server-side prices; nothing the client sends can change it.
 */
@Entity
@Table(name = "payment_orders")
public class PaymentOrder {

    public enum Kind { CART, CUSTOM_BUNDLE, EXTRA_ATTEMPTS }

    public enum Status { PENDING, PAID, FAILED, CANCELLED, CHARGEDBACK }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /** CART only: comma-separated bundle ids snapshotted when the order was created. */
    @Column(name = "bundle_ids", length = 1000)
    private String bundleIds;

    @Column(name = "custom_bundle_id")
    private Long customBundleId;

    @Column(name = "paper_id")
    private Long paperId;

    @Column(name = "origin_bundle_id")
    private Long originBundleId;

    @Column(name = "attempts_count")
    private Integer attemptsCount;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    public PaymentOrder() {
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getBundleIds() { return bundleIds; }
    public void setBundleIds(String bundleIds) { this.bundleIds = bundleIds; }
    public Long getCustomBundleId() { return customBundleId; }
    public void setCustomBundleId(Long customBundleId) { this.customBundleId = customBundleId; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getOriginBundleId() { return originBundleId; }
    public void setOriginBundleId(Long originBundleId) { this.originBundleId = originBundleId; }
    public Integer getAttemptsCount() { return attemptsCount; }
    public void setAttemptsCount(Integer attemptsCount) { this.attemptsCount = attemptsCount; }
    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }
}
