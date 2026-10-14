package com.camuel.sanchay

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.Calendar
import kotlin.math.max

object StatisticsEngine {
    
    data class EconomicMetrics(
        val averageDailySpend: Double,
        val spendingVariance: Double,
        val standardDeviation: Double,
        val predictedEndOfMonthSpend: Double,
        val isOverBudget: Boolean,
        val healthScore: Int, // 0 - 100
        val burnRate: Double, // Average spend per day remaining to stay in budget
        val actualDailyBurn: Double // Actual average spend per day so far
    )

    fun calculateMetrics(expensesByDate: Map<String, Float>, monthlyBudget: Float): EconomicMetrics {
        if (expensesByDate.isEmpty()) {
            return EconomicMetrics(0.0, 0.0, 0.0, 0.0, false, 100, monthlyBudget.toDouble() / 30.0, 0.0)
        }

        val stats = DescriptiveStatistics()
        expensesByDate.values.forEach { amount ->
            stats.addValue(amount.toDouble())
        }

        val totalSpentSoFar = stats.sum
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = max(1, daysInMonth - currentDay + 1)

        val actualDailyBurn = totalSpentSoFar / currentDay
        val predictedEndOfMonthSpend = totalSpentSoFar + (actualDailyBurn * (daysInMonth - currentDay))
        
        // Burn Rate: How much can they spend per day for the rest of the month to hit EXACTLY the budget
        val remainingBudget = max(0.0, monthlyBudget.toDouble() - totalSpentSoFar)
        val allowedBurnRate = remainingBudget / daysRemaining

        // Health Score calculation
        // Penalize for being over budget and for high variance
        val budgetUsageRatio = totalSpentSoFar / monthlyBudget.toDouble()
        var score = 100
        
        // Penalty for usage (if we are 50% through the month but used 80% of budget, score drops)
        val expectedUsageRatio = currentDay.toDouble() / daysInMonth.toDouble()
        if (budgetUsageRatio > expectedUsageRatio) {
            score -= ((budgetUsageRatio - expectedUsageRatio) * 50).toInt()
        }
        
        // Hard penalty for being over budget
        if (totalSpentSoFar > monthlyBudget) {
            score -= 40
        }

        return EconomicMetrics(
            averageDailySpend = stats.mean,
            spendingVariance = stats.variance.takeIf { !it.isNaN() } ?: 0.0,
            standardDeviation = stats.standardDeviation.takeIf { !it.isNaN() } ?: 0.0,
            predictedEndOfMonthSpend = predictedEndOfMonthSpend,
            isOverBudget = totalSpentSoFar > monthlyBudget,
            healthScore = score.coerceIn(0, 100),
            burnRate = allowedBurnRate,
            actualDailyBurn = actualDailyBurn
        )
    }
}
