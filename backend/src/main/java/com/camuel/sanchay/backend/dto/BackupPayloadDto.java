package com.camuel.sanchay.backend.dto;

import java.time.Instant;
import java.util.List;

public class BackupPayloadDto {
    private int version = 1;
    private Instant exportTimestamp = Instant.now();
    private List<ExpenseDto> expenses;
    private List<CategoryDto> categories;
    private List<WalletDto> wallets;
    private List<SubscriptionDto> subscriptions;
    private List<SavingsGoalDto> savingsGoals;
    private List<DebtDto> debts;
    private List<InvestmentDto> investments;

    public BackupPayloadDto() {}

    public BackupPayloadDto(int version, Instant exportTimestamp, List<ExpenseDto> expenses, List<CategoryDto> categories, List<WalletDto> wallets, List<SubscriptionDto> subscriptions, List<SavingsGoalDto> savingsGoals, List<DebtDto> debts, List<InvestmentDto> investments) {
        this.version = version;
        this.exportTimestamp = exportTimestamp;
        this.expenses = expenses;
        this.categories = categories;
        this.wallets = wallets;
        this.subscriptions = subscriptions;
        this.savingsGoals = savingsGoals;
        this.debts = debts;
        this.investments = investments;
    }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getExportTimestamp() { return exportTimestamp; }
    public void setExportTimestamp(Instant exportTimestamp) { this.exportTimestamp = exportTimestamp; }
    public List<ExpenseDto> getExpenses() { return expenses; }
    public void setExpenses(List<ExpenseDto> expenses) { this.expenses = expenses; }
    public List<CategoryDto> getCategories() { return categories; }
    public void setCategories(List<CategoryDto> categories) { this.categories = categories; }
    public List<WalletDto> getWallets() { return wallets; }
    public void setWallets(List<WalletDto> wallets) { this.wallets = wallets; }
    public List<SubscriptionDto> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(List<SubscriptionDto> subscriptions) { this.subscriptions = subscriptions; }
    public List<SavingsGoalDto> getSavingsGoals() { return savingsGoals; }
    public void setSavingsGoals(List<SavingsGoalDto> savingsGoals) { this.savingsGoals = savingsGoals; }
    public List<DebtDto> getDebts() { return debts; }
    public void setDebts(List<DebtDto> debts) { this.debts = debts; }
    public List<InvestmentDto> getInvestments() { return investments; }
    public void setInvestments(List<InvestmentDto> investments) { this.investments = investments; }
}
