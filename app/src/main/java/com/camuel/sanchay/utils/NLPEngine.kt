package com.camuel.sanchay.utils

import java.util.Locale

object NLPEngine {

    data class ParsedExpense(
        val amount: String?,
        val category: String?,
        val note: String?
    )

    fun parseCommand(input: String): ParsedExpense {
        val lowerInput = input.lowercase(Locale.US)

        // Amount extraction
        val amountRegex = """(\d+(\.\d+)?)""".toRegex()
        val amountMatch = amountRegex.find(input)
        val amount = amountMatch?.value

        var note = input
        if (amount != null) {
            note = input.replace(amount, "")
                .replace("₹", "")
                .replace("(?i)rs\\.?".toRegex(), "")
                .replace("(?i)inr".toRegex(), "")
                .replace("$", "")
                .replace("(?i)for".toRegex(), "")
                .trim()
        }

        // Expanded financial category mapping including Indian everyday spends
        val categoryMap = mapOf(
            "Food" to listOf("coffee", "tea", "chai", "lunch", "dinner", "breakfast", "swiggy", "zomato", "food", "restaurant", "cafe", "snack", "groceries", "grocery"),
            "Transport" to listOf("uber", "ola", "auto", "metro", "bus", "train", "cab", "petrol", "diesel", "fuel", "flight", "travel"),
            "Bills" to listOf("electricity", "wifi", "internet", "recharge", "rent", "water", "bill", "subscription", "broadband"),
            "Shopping" to listOf("amazon", "flipkart", "clothes", "shoes", "mall", "shopping"),
            "Health" to listOf("medicine", "doctor", "hospital", "pharmacy", "medical", "gym"),
            "Entertainment" to listOf("movie", "netflix", "cinema", "hotstar", "spotify", "games"),
            "Investment" to listOf("stocks", "mutual fund", "sip", "crypto", "gold", "zerodha", "groww")
        )

        var matchedCategory: String? = null
        for ((cat, keywords) in categoryMap) {
            if (keywords.any { lowerInput.contains(it) }) {
                matchedCategory = cat
                break
            }
        }

        val category = matchedCategory ?: "Other"

        return ParsedExpense(amount, category, note.ifBlank { category })
    }
}
