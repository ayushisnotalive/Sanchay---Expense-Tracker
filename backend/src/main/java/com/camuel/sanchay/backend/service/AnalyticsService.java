package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.AnalyticsDto;
import com.camuel.sanchay.backend.entity.Expense;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.ExpenseRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public AnalyticsService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "analyticsSummary", key = "#userId")
    public AnalyticsDto getAnalyticsSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Expense> expenses = expenseRepository.findByUserAndDeletedFalseOrderByDateDesc(user);

        double total = 0.0;
        Map<String, Double> byCategory = new HashMap<>();
        Map<String, Double> byDate = new TreeMap<>();

        for (Expense e : expenses) {
            double amt = 0.0;
            try {
                amt = Double.parseDouble(e.getAmount());
            } catch (NumberFormatException ignored) {}

            total += amt;
            byCategory.put(e.getCategory(), byCategory.getOrDefault(e.getCategory(), 0.0) + amt);
            byDate.put(e.getDate(), byDate.getOrDefault(e.getDate(), 0.0) + amt);
        }

        AnalyticsDto dto = new AnalyticsDto();
        dto.setTotalExpenses(total);
        double budget = user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 1000.0;
        dto.setMonthlyBudget(budget);
        dto.setRemainingBudget(Math.max(0.0, budget - total));
        dto.setExpensesByCategory(byCategory);
        dto.setExpensesByDate(byDate);

        double dailyAvg = byDate.isEmpty() ? 0.0 : total / Math.max(1, byDate.size());
        dto.setDailyAverage(dailyAvg);

        String topCategory = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        dto.setTopSpendingCategory(topCategory);

        return dto;
    }
}
