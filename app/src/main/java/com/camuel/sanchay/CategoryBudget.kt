package com.camuel.sanchay

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "category_budgets",
    indices = [Index(value = ["categoryName"], unique = true)]
)
data class CategoryBudget(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var categoryName: String,
    var monthlyLimit: Double,
    var budgetType: String = "NEED", // "NEED", "WANT", "SAVINGS"
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true
)
