package com.camuel.sanchay.backend.dto;

import java.util.Map;

public class AnalyticsDto {

    private Double totalExpenses;
    private Double monthlyBudget;
    private Double remainingBudget;
    private Double dailyAverage;
    private Map<String, Double> expensesByCategory;
    private Map<String, Double> expensesByDate;
    private String topSpendingCategory;

    public Double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }
    public Double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(Double monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    public Double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(Double remainingBudget) { this.remainingBudget = remainingBudget; }
    public Double getDailyAverage() { return dailyAverage; }
    public void setDailyAverage(Double dailyAverage) { this.dailyAverage = dailyAverage; }
    public Map<String, Double> getExpensesByCategory() { return expensesByCategory; }
    public void setExpensesByCategory(Map<String, Double> expensesByCategory) { this.expensesByCategory = expensesByCategory; }
    public Map<String, Double> getExpensesByDate() { return expensesByDate; }
    public void setExpensesByDate(Map<String, Double> expensesByDate) { this.expensesByDate = expensesByDate; }
    public String getTopSpendingCategory() { return topSpendingCategory; }
    public void setTopSpendingCategory(String topSpendingCategory) { this.topSpendingCategory = topSpendingCategory; }
}
