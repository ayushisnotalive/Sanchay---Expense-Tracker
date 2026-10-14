package com.camuel.sanchay.utils

object CategorizationEngine {
    private val keywordToCategory = mapOf(
        // Food
        "starbucks" to "Food",
        "mcdonalds" to "Food",
        "burger king" to "Food",
        "subway" to "Food",
        "pizza" to "Food",
        "cafe" to "Food",
        "restaurant" to "Food",
        "walmart" to "Food", // Walmart often groceries
        "kroger" to "Food",
        "whole foods" to "Food",
        "trader joe" to "Food",
        "uber eats" to "Food",
        "doordash" to "Food",

        // Travel
        "uber" to "Travel",
        "lyft" to "Travel",
        "delta" to "Travel",
        "american airlines" to "Travel",
        "united airlines" to "Travel",
        "hotel" to "Travel",
        "airbnb" to "Travel",
        "booking.com" to "Travel",
        "expedia" to "Travel",
        "taxi" to "Travel",
        "train" to "Travel",
        "amtrak" to "Travel",

        // Bills
        "electric" to "Bills",
        "water" to "Bills",
        "gas" to "Bills",
        "internet" to "Bills",
        "xfinity" to "Bills",
        "at&t" to "Bills",
        "verizon" to "Bills",
        "t-mobile" to "Bills",
        "insurance" to "Bills",
        "geico" to "Bills",
        "state farm" to "Bills",
        
        // Shopping
        "amazon" to "Shopping",
        "target" to "Shopping",
        "best buy" to "Shopping",
        "apple" to "Shopping",
        "nike" to "Shopping",
        "adidas" to "Shopping",
        "clothing" to "Shopping",
        "mall" to "Shopping",
        
        // Health
        "pharmacy" to "Health",
        "cvs" to "Health",
        "walgreens" to "Health",
        "hospital" to "Health",
        "clinic" to "Health",
        "doctor" to "Health",
        "dentist" to "Health"
    )

    fun suggestCategory(text: String): String {
        val lowerText = text.lowercase()
        
        for ((keyword, category) in keywordToCategory) {
            if (lowerText.contains(keyword)) {
                return category
            }
        }
        
        return "Other"
    }
}
