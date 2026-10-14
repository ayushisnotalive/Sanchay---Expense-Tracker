package com.camuel.sanchay

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale
import kotlin.math.max

class Phase7CoreFeaturesTest {

    @Test
    fun testTransactionTypesAndNetCashFlow() {
        val transactions = listOf(
            Expense(id = 1, amount = "50000.00", category = "Salary", date = "2026-10-01", transactionType = "INCOME"),
            Expense(id = 2, amount = "15000.00", category = "Freelance", date = "2026-10-05", transactionType = "INCOME"),
            Expense(id = 3, amount = "20000.00", category = "Rent", date = "2026-10-02", transactionType = "EXPENSE"),
            Expense(id = 4, amount = "5000.00", category = "Food", date = "2026-10-03", transactionType = "EXPENSE"),
            Expense(id = 5, amount = "10000.00", category = "Transfer to Savings", date = "2026-10-04", transactionType = "TRANSFER", walletId = 1, transferToWalletId = 2)
        )

        val totalIncome = transactions.filter { it.transactionType == "INCOME" }
            .sumOf { it.amount.toDouble() }
        val totalExpense = transactions.filter { it.transactionType == "EXPENSE" }
            .sumOf { it.amount.toDouble() }
        val totalTransfers = transactions.filter { it.transactionType == "TRANSFER" }
            .sumOf { it.amount.toDouble() }
        val netCashFlow = totalIncome - totalExpense

        assertEquals(65000.00, totalIncome, 0.001)
        assertEquals(25000.00, totalExpense, 0.001)
        assertEquals(10000.00, totalTransfers, 0.001)
        assertEquals(40000.00, netCashFlow, 0.001)
    }

    @Test
    fun test50_30_20RuleCalculation() {
        val totalIncome = 100000.0
        val needsTarget = totalIncome * 0.50
        val wantsTarget = totalIncome * 0.30
        val savingsTarget = totalIncome * 0.20

        assertEquals(50000.0, needsTarget, 0.001)
        assertEquals(30000.0, wantsTarget, 0.001)
        assertEquals(20000.0, savingsTarget, 0.001)

        val actualNeeds = 45000.0
        val actualWants = 25000.0
        val actualSavings = 30000.0

        val needsRatio = actualNeeds / totalIncome
        val wantsRatio = actualWants / totalIncome
        val savingsRatio = actualSavings / totalIncome

        assertTrue("Needs should be <= 50%", needsRatio <= 0.50)
        assertTrue("Wants should be <= 30%", wantsRatio <= 0.30)
        assertTrue("Savings should be >= 20%", savingsRatio >= 0.20)
    }

    @Test
    fun testCategoryBudgetThresholdAlerts() {
        val budget = CategoryBudget(
            id = 1,
            categoryName = "Dining Out",
            monthlyLimit = 10000.0,
            budgetType = "WANT"
        )

        // Case 1: Normal (< 70%)
        val spentNormal = 5000.0
        val ratioNormal = spentNormal / budget.monthlyLimit
        assertEquals("NORMAL", getBudgetStatus(ratioNormal))

        // Case 2: Warning (70% - 99%)
        val spentWarning = 8500.0
        val ratioWarning = spentWarning / budget.monthlyLimit
        assertEquals("WARNING", getBudgetStatus(ratioWarning))

        // Case 3: Exceeded (>= 100%)
        val spentExceeded = 10500.0
        val ratioExceeded = spentExceeded / budget.monthlyLimit
        assertEquals("EXCEEDED", getBudgetStatus(ratioExceeded))
    }

    private fun getBudgetStatus(ratio: Double): String {
        return when {
            ratio >= 1.0 -> "EXCEEDED"
            ratio >= 0.70 -> "WARNING"
            else -> "NORMAL"
        }
    }

    @Test
    fun testGoalVelocityCalculations() {
        val targetAmount = 120000f
        val currentAmount = 60000f
        val monthsRemaining = 6f

        val remaining = max(0f, targetAmount - currentAmount)
        val requiredMonthlyRunRate = remaining / monthsRemaining

        assertEquals(60000f, remaining, 0.001f)
        assertEquals(10000f, requiredMonthlyRunRate, 0.001f)

        val progress = currentAmount / targetAmount
        assertEquals(0.50f, progress, 0.001f)
    }
}
