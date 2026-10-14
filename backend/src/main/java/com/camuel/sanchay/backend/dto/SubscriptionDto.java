package com.camuel.sanchay.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubscriptionDto {
    private Long id;

    @NotBlank(message = "Subscription name is required")
    private String name;

    @NotNull(message = "Amount is required")
    private Float amount;

    @NotBlank(message = "Next due date is required")
    private String nextDueDate;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    @NotBlank(message = "Category is required")
    private String category;

    private Long walletId = 1L;
    private String lastAutoPostDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Float getAmount() { return amount; }
    public void setAmount(Float amount) { this.amount = amount; }
    public String getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(String nextDueDate) { this.nextDueDate = nextDueDate; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public String getLastAutoPostDate() { return lastAutoPostDate; }
    public void setLastAutoPostDate(String lastAutoPostDate) { this.lastAutoPostDate = lastAutoPostDate; }
}
