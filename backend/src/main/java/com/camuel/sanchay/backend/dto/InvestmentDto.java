package com.camuel.sanchay.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InvestmentDto {
    private Long id;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    private Double quantity;

    @NotNull(message = "Average buy price is required")
    private Double averageBuyPrice;

    @NotNull(message = "Current price is required")
    private Double currentPrice;

    private Long walletId = 1L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(Double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
}
