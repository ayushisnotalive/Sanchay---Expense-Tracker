package com.camuel.sanchay

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var iconName: String,
    var isCustom: Boolean = true
)
