package com.camuel.sanchay

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"]),
        Index(value = ["walletId"]),
        Index(value = ["isDirty"]),
        Index(value = ["transactionType"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var amount: String,
    var category: String,
    var date: String,
    var paymentMethod: String = "Cash",
    var note: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var walletId: Int = 1, // Default to first wallet
    var tags: List<String> = emptyList(),
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true,
    var syncedAt: Long? = null,
    var isDeleted: Boolean = false,
    var transactionType: String = "EXPENSE", // "EXPENSE", "INCOME", "TRANSFER"
    var transferToWalletId: Int? = null,
    var receiptImageUri: String? = null
)

@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["nextDueDate"])]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var amount: Float,
    var nextDueDate: String,
    var frequency: String, // e.g., "Monthly", "Yearly", "Weekly"
    var category: String,
    var walletId: Int = 1,
    var lastAutoPostDate: String? = null,
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var targetAmount: Float,
    var currentAmount: Float,
    var deadlineDate: String,
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true
)

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var balance: Double,
    var currency: String = "₹",
    var colorHex: String = "#10B981",
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true
)

@Entity(
    tableName = "debts",
    indices = [Index(value = ["isSettled"])]
)
data class Debt(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var personName: String,
    var amount: Double,
    var type: String, // "Lent" or "Borrowed"
    var dueDate: String,
    var isSettled: Boolean = false,
    var clientUuid: String = UUID.randomUUID().toString(),
    var isDirty: Boolean = true
)
