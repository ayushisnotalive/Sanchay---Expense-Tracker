package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class BackupService {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final SavingsGoalService savingsGoalService;
    private final DebtService debtService;
    private final InvestmentService investmentService;

    public BackupService(
            ExpenseService expenseService,
            CategoryService categoryService,
            WalletService walletService,
            SubscriptionService subscriptionService,
            SavingsGoalService savingsGoalService,
            DebtService debtService,
            InvestmentService investmentService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.savingsGoalService = savingsGoalService;
        this.debtService = debtService;
        this.investmentService = investmentService;
    }

    public BackupPayloadDto exportBackup(Long userId) {
        return new BackupPayloadDto(
                1,
                Instant.now(),
                expenseService.getAllExpenses(userId),
                categoryService.getCategoriesForUser(userId),
                walletService.getWalletsForUser(userId),
                subscriptionService.getSubscriptions(userId),
                savingsGoalService.getSavingsGoals(userId),
                debtService.getAllDebts(userId),
                investmentService.getInvestments(userId)
        );
    }

    @Transactional
    public Map<String, Integer> importBackup(Long userId, BackupPayloadDto payload) {
        int walletsCount = 0;
        int categoriesCount = 0;
        int expensesCount = 0;
        int subscriptionsCount = 0;
        int savingsGoalsCount = 0;
        int debtsCount = 0;
        int investmentsCount = 0;

        if (payload.getWallets() != null) {
            for (WalletDto w : payload.getWallets()) {
                w.setId(null);
                walletService.createWallet(userId, w);
                walletsCount++;
            }
        }

        if (payload.getCategories() != null) {
            for (CategoryDto c : payload.getCategories()) {
                if (c.isCustom()) {
                    c.setId(null);
                    categoryService.createCustomCategory(userId, c);
                    categoriesCount++;
                }
            }
        }

        if (payload.getExpenses() != null) {
            for (ExpenseDto e : payload.getExpenses()) {
                e.setId(null);
                expenseService.createExpense(userId, e);
                expensesCount++;
            }
        }

        if (payload.getSubscriptions() != null) {
            for (SubscriptionDto s : payload.getSubscriptions()) {
                s.setId(null);
                subscriptionService.createSubscription(userId, s);
                subscriptionsCount++;
            }
        }

        if (payload.getSavingsGoals() != null) {
            for (SavingsGoalDto g : payload.getSavingsGoals()) {
                g.setId(null);
                savingsGoalService.createSavingsGoal(userId, g);
                savingsGoalsCount++;
            }
        }

        if (payload.getDebts() != null) {
            for (DebtDto d : payload.getDebts()) {
                d.setId(null);
                debtService.createDebt(userId, d);
                debtsCount++;
            }
        }

        if (payload.getInvestments() != null) {
            for (InvestmentDto i : payload.getInvestments()) {
                i.setId(null);
                investmentService.createInvestment(userId, i);
                investmentsCount++;
            }
        }

        Map<String, Integer> summary = new HashMap<>();
        summary.put("wallets", walletsCount);
        summary.put("categories", categoriesCount);
        summary.put("expenses", expensesCount);
        summary.put("subscriptions", subscriptionsCount);
        summary.put("savingsGoals", savingsGoalsCount);
        summary.put("debts", debtsCount);
        summary.put("investments", investmentsCount);
        return summary;
    }
}
