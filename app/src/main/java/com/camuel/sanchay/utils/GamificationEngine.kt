package com.camuel.sanchay.utils

import com.camuel.sanchay.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Badge(val name: String, val description: String, val isUnlocked: Boolean)

object GamificationEngine {

    fun calculateCurrentStreak(expenses: List<Expense>): Int {
        if (expenses.isEmpty()) return 0

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val datesWithExpenses = expenses.mapNotNull {
            try { format.parse(it.date) } catch (e: Exception) { null }
        }.map { format.format(it) }.toSet()

        var streak = 0
        val cal = Calendar.getInstance()

        // Check backwards from today
        for (i in 0 until 365) {
            val dateStr = format.format(cal.time)
            
            // If the user DID NOT spend on a non-essential category, we count it as a "streak" day.
            // For simplicity in this mock, we just count consecutive days where total expenses < $50
            val dayExpenses = expenses.filter { it.date == dateStr }
            val dailyTotal = dayExpenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
            
            if (dailyTotal < 50.0) {
                streak++
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    fun getBadges(expenses: List<Expense>, streak: Int): List<Badge> {
        val badges = mutableListOf<Badge>()
        
        // Frugal Starter Badge
        badges.add(Badge("Frugal Starter", "Have a 3-day low-spending streak", streak >= 3))
        
        // Thrifty Master Badge
        badges.add(Badge("Thrifty Master", "Have a 7-day low-spending streak", streak >= 7))
        
        // Savings Champion Badge
        val totalDining = expenses.filter { it.category == "Dining" }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        badges.add(Badge("Home Chef", "Spend less than $100 on dining out", expenses.isNotEmpty() && totalDining < 100))

        return badges
    }
}
