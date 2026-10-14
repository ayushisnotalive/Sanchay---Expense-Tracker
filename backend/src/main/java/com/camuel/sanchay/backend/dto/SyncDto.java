package com.camuel.sanchay.backend.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SyncDto {

    public static class PushRequest {
        private List<ExpenseDto> expenses = new ArrayList<>();
        private List<CategoryDto> categories = new ArrayList<>();
        private List<WalletDto> wallets = new ArrayList<>();
        private List<SubscriptionDto> subscriptions = new ArrayList<>();
        private List<SavingsGoalDto> savingsGoals = new ArrayList<>();
        private List<DebtDto> debts = new ArrayList<>();
        private List<InvestmentDto> investments = new ArrayList<>();

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

    public static class PullResponse {
        private Instant serverTimestamp = Instant.now();
        private List<ExpenseDto> expenses = new ArrayList<>();
        private List<CategoryDto> categories = new ArrayList<>();
        private List<WalletDto> wallets = new ArrayList<>();
        private List<SubscriptionDto> subscriptions = new ArrayList<>();
        private List<SavingsGoalDto> savingsGoals = new ArrayList<>();
        private List<DebtDto> debts = new ArrayList<>();
        private List<InvestmentDto> investments = new ArrayList<>();

        public Instant getServerTimestamp() { return serverTimestamp; }
        public void setServerTimestamp(Instant serverTimestamp) { this.serverTimestamp = serverTimestamp; }
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
}
