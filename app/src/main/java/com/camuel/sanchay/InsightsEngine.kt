package com.camuel.sanchay

import java.util.Locale

object InsightsEngine {

    fun generateInsights(
        expensesByCategory: Map<String, Float>,
        monthlyBudget: Float,
        totalSpent: Double
    ): List<String> {
        val insights = mutableListOf<String>()

        // Budget utilization insight
        val budgetUsagePercent = if (monthlyBudget > 0) (totalSpent / monthlyBudget * 100) else 0.0
        when {
            budgetUsagePercent > 100 -> insights.add("Critical: You are ${String.format(Locale.US, "%.1f", budgetUsagePercent - 100)}% over your monthly budget.")
            budgetUsagePercent > 85 -> insights.add("Warning: You've used ${String.format(Locale.US, "%.1f", budgetUsagePercent)}% of your budget. Slow down your spending.")
            budgetUsagePercent > 50 -> insights.add("Neutral: You've used ${String.format(Locale.US, "%.1f", budgetUsagePercent)}% of your budget halfway through the month.")
            else -> insights.add("Good: You're well within your budget. Keep it up!")
        }

        // Category-specific insights
        expensesByCategory.forEach { (category, amount) ->
            if (amount > (monthlyBudget * 0.4f)) {
                insights.add("Insight: $category spending is very high (${String.format(Locale.US, "%.1f", amount / monthlyBudget * 100)}% of total budget).")
            }
        }

        // Savings suggestion
        if (budgetUsagePercent < 70) {
            val potentialSavings = monthlyBudget - totalSpent
            if (potentialSavings > 0) {
                insights.add("Tip: You have ${String.format(Locale.US, "%.0f", potentialSavings)} left. Consider moving some to your Savings Goals.")
            }
        }

        return insights
    }
}
