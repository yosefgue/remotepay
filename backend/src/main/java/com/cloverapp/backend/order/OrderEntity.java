package com.cloverapp.backend.order;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "link_token")
    private String linkToken;

    @Column(name = "clover_order_id")
    private String cloverOrderId;

    @Column(name = "title")
    private String title;

    @Column(name = "status")
    private String status = "DRAFT";

    @Column(name = "currency")
    private String currency = "USD";

    @Column(name = "subtotal_amount")
    private Long subtotalAmount = 0L;

    @Column(name = "tax_amount")
    private Long taxAmount = 0L;

    @Column(name = "total_amount")
    private Long totalAmount = 0L;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public OrderEntity() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getLinkToken() { return linkToken; }
    public void setLinkToken(String linkToken) { this.linkToken = linkToken; }

    public String getCloverOrderId() { return cloverOrderId; }
    public void setCloverOrderId(String cloverOrderId) { this.cloverOrderId = cloverOrderId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(Long subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public Long getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Long taxAmount) { this.taxAmount = taxAmount; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
