package com.camuel.sanchay

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var symbol: String, // e.g., "AAPL", "BTC"
    var name: String, // e.g., "Apple Inc."
    var quantity: Double,
    var averageBuyPrice: Double,
    var currentPrice: Double, // This would ideally be fetched from an API, but for basic we can just let user update it
    var walletId: Int = 1
)
