package com.camuel.sanchay.ui.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// Sanchay Dual-Theme Design System (Derived from /refers)
// Modern Clean Fintech Aesthetics - Full Light & OLED Dark Mode Parity
// ==============================================================================

// ── Light Theme Tokens ─────────────────────────────────────────────────────────
val LightBackground = Color(0xFFF8FAFC)         // Modern clean crisp canvas
val LightSurface = Color(0xFFFFFFFF)            // Pure white card surfaces
val LightSurfaceVariant = Color(0xFFF1F5F9)     // Clean subtle pill / chip container
val LightCard = Color(0xFFFFFFFF)               // Pure white elevated card
val LightCardBorder = Color(0xFFE2E8F0)         // Clean 1dp border
val LightContainer = Color(0xFFF1F5F9)          // Clean container
val LightTextPrimary = Color(0xFF0F172A)        // Slate 900 high contrast
val LightTextSecondary = Color(0xFF475569)      // Slate 600
val LightTextMuted = Color(0xFF94A3B8)          // Slate 400

// ── OLED Dark Theme Tokens ────────────────────────────────────────────────────
val DarkCanvasBackground = Color(0xFF0D0E12)    // Deep OLED pure dark background
val DarkSurfaceElevated = Color(0xFF16181F)     // Card surface
val DarkSurfaceInput = Color(0xFF20232D)        // Input pill container
val DarkCardSurface = Color(0xFF16181F)         // Elevated card surface
val DarkCardBorderStroke = Color(0xFF262A36)    // Subtle dark border
val DarkContainerSurface = Color(0xFF1C1F28)    // Deep container surface
val DarkTextPrimaryTitle = Color(0xFFF9FAFB)    // Crisp pure white text
val DarkTextSecondaryBody = Color(0xFF9CA3AF)   // Refined slate grey
val DarkTextMutedSub = Color(0xFF6B7280)        // Muted dark placeholder

// ── Cashflow Semantic Palette (Fintech Standard) ──────────────────────────────
val CashflowIncome = Color(0xFF10B981)          // Vibrant Emerald Green
val CashflowIncomeContainer = Color(0xFFD1FAE5) // Soft mint pill container
val CashflowExpense = Color(0xFFF43F5E)         // Vibrant Terracotta Rose
val CashflowExpenseContainer = Color(0xFFFFE4E6)// Soft rose pill container
val CashflowTransfer = Color(0xFF6366F1)        // Indigo Transfer
val CashflowTransferContainer = Color(0xFFE0E7FF)
val CashflowWarning = Color(0xFFF59E0B)         // Warm Amber Warning
val CashflowWarningContainer = Color(0xFFFEF3C7)

// ── Category Palette (50/30/20 & Spectrum Badges) ─────────────────────────────
val CategoryFood = Color(0xFFF97316)            // Tangerine
val CategoryTransport = Color(0xFF3B82F6)       // Ocean Blue
val CategoryShopping = Color(0xFFEC4899)       // Pink Violet
val CategoryBills = Color(0xFF8B5CF6)           // Deep Purple
val CategoryEntertainment = Color(0xFF06B6D4)   // Cyan
val CategoryHealth = Color(0xFF10B981)          // Emerald Green
val CategoryEducation = Color(0xFFF59E0B)       // Warm Amber
val CategoryInvestment = Color(0xFF14B8A6)      // Teal
val CategoryOther = Color(0xFF64748B)           // Slate

// ── Backward-Compatible Legacy Token Aliases ──────────────────────────────────
val SolidBeigeBackground = LightBackground
val SolidBeigeSurface = LightSurface
val SolidBeigeSurfaceVariant = LightSurfaceVariant
val SolidBeigeCard = LightCard
val SolidBeigeCardBorder = LightCardBorder
val SolidBeigeBorder = LightCardBorder
val SolidBeigeContainer = LightContainer
val SolidBeigeLight = Color(0xFFFAF8F2)
val SolidBeigeDark = Color(0xFFC8BCA6)

val LeafGreenPrimary = CashflowIncome
val LeafGreenDeep = Color(0xFF059669)
val LeafGreenMedium = Color(0xFF10B981)
val LeafGreenLight = Color(0xFF34D399)
val LeafGreenContainer = CashflowIncomeContainer
val LeafGreenBorder = CashflowIncome

val TextPrimary = LightTextPrimary
val TextSecondary = LightTextSecondary
val TextMuted = LightTextMuted

val ForestBackground = LightBackground
val ForestSurface = LightSurface
val ForestSurfaceVariant = LightSurfaceVariant
val ForestCard = LightCard
val ForestCardBorder = LightCardBorder

val SlateBackground = LightBackground
val SlateSurface = LightSurface
val SlateSurfaceVariant = LightSurfaceVariant
val SlateCard = LightCard
val SlateCardBorder = LightCardBorder

val WarmBeige = LightSurface
val WarmBeigeLight = LightTextPrimary
val WarmBeigeMuted = LightTextSecondary
val WarmBeigeDark = LightTextMuted
val WarmBeigeContainer = LightContainer
val WarmBeigeBorder = LightCardBorder

val ForestEmerald = CashflowIncome
val ForestGreenDeep = LeafGreenDeep
val SageGreen = Color(0xFF4C8C5A)
val OliveGreen = Color(0xFF558B2F)
val MintAccent = Color(0xFF81C784)

val EnterpriseEmerald = CashflowIncome
val EnterpriseIndigo = CashflowTransfer
val EnterpriseBlue = Color(0xFF2563EB)
val EnterpriseAmber = CashflowWarning
val EnterpriseRose = CashflowExpense
val EnterpriseTeal = Color(0xFF14B8A6)

val NeonCyan = Color(0xFF06B6D4)
val NeonPurple = Color(0xFF8B5CF6)
val NeonPink = Color(0xFFF43F5E)
val DarkBackground = DarkCanvasBackground
val DarkSurface = DarkSurfaceElevated
val DarkSurfaceVariant = DarkSurfaceInput

val Primary = CashflowIncome
val PrimaryVariant = LeafGreenDeep
val Secondary = CashflowTransfer
val Tertiary = SageGreen
val Background = LightBackground
val Surface = LightSurface
val Error = CashflowExpense
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = LightTextPrimary
val OnSurface = LightTextPrimary
val OnError = Color.White

val IncomeColor = CashflowIncome
val ExpenseColor = CashflowExpense
val WarningColor = CashflowWarning
