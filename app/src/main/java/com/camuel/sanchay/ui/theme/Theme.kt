package com.camuel.sanchay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CashflowIncome,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFF6EE7B7),
    secondary = CashflowTransfer,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = CashflowWarning,
    onTertiary = Color.Black,
    background = DarkCanvasBackground,
    surface = DarkSurfaceElevated,
    surfaceVariant = DarkSurfaceInput,
    surfaceContainer = DarkSurfaceElevated,
    onBackground = DarkTextPrimaryTitle,
    onSurface = DarkTextPrimaryTitle,
    onSurfaceVariant = DarkTextSecondaryBody,
    outline = DarkCardBorderStroke,
    outlineVariant = DarkCardBorderStroke,
    error = CashflowExpense,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CashflowIncome,
    onPrimary = Color.White,
    primaryContainer = CashflowIncomeContainer,
    onPrimaryContainer = Color(0xFF065F46),
    secondary = CashflowTransfer,
    onSecondary = Color.White,
    secondaryContainer = CashflowTransferContainer,
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = CashflowWarning,
    onTertiary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceContainer = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    outlineVariant = LightCardBorder,
    error = CashflowExpense,
    onError = Color.White
)

@Composable
fun standardDialogTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
fun SanchayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accentColor: Color = CashflowIncome,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(primary = accentColor)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
