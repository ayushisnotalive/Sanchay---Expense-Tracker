package com.camuel.sanchay.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_user", columnList = "user_id, next_due_date")
})
public class Subscription extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Float amount;

    @Column(name = "next_due_date", nullable = false, length = 32)
    private String nextDueDate;

    @Column(nullable = false, length = 32)
    private String frequency; // Monthly, Yearly, Weekly

    @Column(nullable = false, length = 64)
    private String category;

    @Column(name = "wallet_id")
    private Long walletId = 1L;

    @Column(name = "last_auto_post_date", length = 32)
    private String lastAutoPostDate;

    public Subscription() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getLastAutoPostDate() {
        return lastAutoPostDate;
    }

    public void setLastAutoPostDate(String lastAutoPostDate) {
        this.lastAutoPostDate = lastAutoPostDate;
    }
}
