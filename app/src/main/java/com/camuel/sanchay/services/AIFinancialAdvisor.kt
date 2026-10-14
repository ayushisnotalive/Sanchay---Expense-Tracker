package com.camuel.sanchay.services

import com.camuel.sanchay.Expense
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

class AIFinancialAdvisor {

    private val apiKey = com.camuel.sanchay.BuildConfig.GEMINI_API_KEY

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            },
            systemInstruction = content {
                text("You are an institutional financial wealth advisor for the Sanchay (संचय) executive finance app. " +
                     "Your goal is to provide concise, actionable wealth and budgeting advice. " +
                     "Format currency amounts in Indian Rupees (₹) unless the user specifies another currency. " +
                     "Be data-driven, encouraging, and clear about cash flow optimization.")
            }
        )
    }

    suspend fun getAdvice(prompt: String, recentExpenses: List<Expense>): String {
        // 1. Try secure enterprise backend AI proxy first
        try {
            val response = SanchayApiClient.syncApiService.getAiAdvice(
                token = "",
                body = mapOf("prompt" to prompt)
            )
            if (response.success && !response.data.isNullOrBlank()) {
                return response.data
            }
        } catch (_: Exception) {
            // Backend offline or unreachable, gracefully continue to local fallback
        }

        // 2. Client-side Gemini fallback if API key is configured
        if (apiKey.isNotBlank() && apiKey != "YOUR_API_KEY") {
            try {
                val contextBuilder = StringBuilder()
                contextBuilder.append("Here is the user's recent expense data:\n")
                recentExpenses.take(10).forEach { expense ->
                    contextBuilder.append("- ${expense.date}: ${expense.amount} on ${expense.category} (${expense.note})\n")
                }
                contextBuilder.append("\nUser question: $prompt\n")

                val response = generativeModel.generateContent(contextBuilder.toString())
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (_: Exception) {
                // Fall through to rule-based analysis
            }
        }

        // 3. Offline intelligent financial rule engine
        return generateOfflineFinancialAdvice(prompt, recentExpenses)
    }

    private fun generateOfflineFinancialAdvice(prompt: String, expenses: List<Expense>): String {
        val totalSpent = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 } }
        val topCategory = categoryTotals.maxByOrNull { it.value }?.key ?: "General"

        return "📊 **Financial Assessment:**\n\n" +
                "You have recorded **$${String.format("%.2f", totalSpent)}** in recent transactions.\n" +
                "Your highest spending area is **$topCategory**.\n\n" +
                "💡 **Actionable Recommendation:** To build wealth effectively, aim to follow the 50/30/20 rule: " +
                "50% for Needs, 30% for Wants, and at least 20% toward Savings & Debt reduction. " +
                "Review your **$topCategory** charges to identify any recurring subscriptions or discretionary spends that can be trimmed."
    }
}
