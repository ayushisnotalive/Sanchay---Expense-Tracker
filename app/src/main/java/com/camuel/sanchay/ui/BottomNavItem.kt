package com.camuel.sanchay.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("dashboard", "Home", Icons.Default.Home)
    object Analytics : BottomNavItem("analytics", "Stats", Icons.Default.BarChart)
    object Map : BottomNavItem("spending_map", "Map", Icons.Default.Public)
    object Wallets : BottomNavItem("wallets", "Wallets", Icons.Default.AccountBalanceWallet)
    object Tools : BottomNavItem("tools", "Tools", Icons.Default.Build)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}
