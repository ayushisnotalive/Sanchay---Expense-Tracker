package com.camuel.sanchay.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ui.theme.*
import java.util.Locale

/**
 * Tactile spring-physics click modifier providing realistic micro-scale depression feedback.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glowColor: Color? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val resolvedBorder = borderColor ?: if (glowColor != null && glowColor != Color.Unspecified) {
        glowColor.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = resolvedBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

// Alias for semantic clarity
@Composable
fun ExecutiveCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) = NeonCard(modifier = modifier, cornerRadius = cornerRadius, glowColor = borderColor, borderColor = borderColor, content = content)

@Composable
fun AnimatedAmountCounter(
    amount: Double,
    currencySymbol: String,
    isStealthMode: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (isStealthMode) {
        Text(
            text = "$currencySymbol ••••••",
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier
        )
    } else {
        val animatedAmount by animateFloatAsState(
            targetValue = amount.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "animatedAmountCounter"
        )
        Text(
            text = "$currencySymbol${String.format(Locale.US, "%,.2f", animatedAmount.toDouble())}",
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier
        )
    }
}

@Composable
fun MaskableAmountText(
    amount: Double,
    currencySymbol: String,
    isStealthMode: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    AnimatedAmountCounter(
        amount = amount,
        currencySymbol = currencySymbol,
        isStealthMode = isStealthMode,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color
    )
}

@Composable
fun ExpenseItem(
    expense: Expense,
    currencySymbol: String,
    isStealthMode: Boolean = false,
    onDelete: (Expense) -> Unit = {},
    onUpdate: (Expense) -> Unit = {}
) {
    var showDetails by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    if (showDetails) {
        ExpenseDetailsDialog(
            expense = expense,
            currencySymbol = currencySymbol,
            onDismiss = { showDetails = false },
            onEdit = {
                showDetails = false
                showEdit = true
            },
            onDelete = {
                showDetails = false
                onDelete(expense)
            }
        )
    }

    if (showEdit) {
        EditExpenseDialog(
            expense = expense,
            onDismiss = { showEdit = false },
            onSave = { updatedExpense ->
                showEdit = false
                onUpdate(updatedExpense)
            }
        )
    }

    val categoryIcon = when (expense.category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Icons.Default.Fastfood
        "transport", "travel" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "bills", "utilities" -> Icons.Default.Receipt
        "entertainment" -> Icons.Default.Movie
        "health", "medical" -> Icons.Default.LocalHospital
        "investment" -> Icons.AutoMirrored.Filled.TrendingUp
        "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.AttachMoney
    }

    val categoryTint = when (expense.category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Color(0xFFC98A2C)
        "transport", "travel" -> Color(0xFF388E3C)
        "shopping" -> Color(0xFFB45339)
        "bills", "utilities" -> Color(0xFF557B62)
        "entertainment" -> Color(0xFF6B8E23)
        "health", "medical" -> Color(0xFF8D6E63)
        "investment" -> LeafGreenPrimary
        "transfer" -> LeafGreenDeep
        else -> MaterialTheme.colorScheme.primary
    }

    val parsedAmount = expense.amount.toDoubleOrNull() ?: 0.0

    NeonCard(
        cornerRadius = 16.dp,
        glowColor = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .bouncyClickable { showDetails = true }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = expense.category,
                        tint = categoryTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (expense.note.isNotBlank()) {
                            Text(
                                text = "• ${expense.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            val (amountColor, amountPrefix) = when (expense.transactionType) {
                "INCOME" -> CashflowIncome to "+"
                "TRANSFER" -> CashflowTransfer to ""
                else -> CashflowExpense to "-"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isStealthMode) {
                    Text(
                        text = "$currencySymbol ••••••",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                } else {
                    Text(
                        text = "$amountPrefix$currencySymbol${String.format(Locale.US, "%,.2f", parsedAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Details", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { expanded = false; showDetails = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { expanded = false; showEdit = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ExpenseColor) },
                            onClick = { expanded = false; onDelete(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailsDialog(
    expense: Expense,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val parsedAmount = expense.amount.toDoubleOrNull() ?: 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Expense Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Category:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(expense.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Amount:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currencySymbol${String.format(Locale.US, "%,.2f", parsedAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(expense.date, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Method:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(expense.paymentMethod, color = MaterialTheme.colorScheme.onSurface)
                }
                if (expense.note.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Note:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(expense.note, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (expense.tags.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tags:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(expense.tags.joinToString(", "), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Edit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Delete", color = ExpenseColor) }
                TextButton(onClick = onDismiss) { Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    )
}

@Composable
fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount) }
    var category by remember { mutableStateOf(expense.category) }
    var note by remember { mutableStateOf(expense.note) }
    val fieldColors = standardDialogTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Edit Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updated = expense.copy(
                    amount = amount.trim().ifBlank { expense.amount },
                    category = category.trim().ifBlank { expense.category },
                    note = note.trim()
                )
                onSave(updated)
            }) { Text("Save Changes", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "neonButtonBounce"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(1.dp, LeafGreenDeep),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = LeafGreenPrimary,
            contentColor = Color.White,
            disabledContainerColor = LeafGreenPrimary.copy(alpha = 0.38f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        text()
    }
}

@Composable
fun SyncStatusBadge(
    syncState: com.camuel.sanchay.services.SyncState,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) {
    val (statusText, statusColor) = when (syncState) {
        com.camuel.sanchay.services.SyncState.SYNCED -> "Cloud Synced" to LeafGreenPrimary
        com.camuel.sanchay.services.SyncState.SYNCING -> "Syncing..." to LeafGreenMedium
        com.camuel.sanchay.services.SyncState.OFFLINE -> "Offline Mode" to WarningColor
        com.camuel.sanchay.services.SyncState.ERROR -> "Sync Error" to ExpenseColor
        else -> "Ready to Sync" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onSyncClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis = 1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}

@Composable
fun HeroBalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencySymbol: String,
    isStealthMode: Boolean,
    deltaText: String? = null,
    onVisibilityToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF0A0F1D)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF475569).copy(alpha = 0.6f),
                        Color(0xFF1E293B).copy(alpha = 0.2f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )
                IconButton(
                    onClick = onVisibilityToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isStealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance Visibility",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            AnimatedAmountCounter(
                amount = totalBalance,
                currencySymbol = currencySymbol,
                isStealthMode = isStealthMode,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (!deltaText.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = deltaText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6EE7B7)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Nested Twin Cashflow Pills (From Ref 1 & Ref 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E3A34).copy(alpha = 0.7f))
                        .border(1.dp, CashflowIncome.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CashflowIncome.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = CashflowIncome,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text("Income", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                            AnimatedAmountCounter(
                                amount = totalIncome,
                                currencySymbol = currencySymbol,
                                isStealthMode = isStealthMode,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Spent Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF3F1D28).copy(alpha = 0.7f))
                        .border(1.dp, CashflowExpense.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CashflowExpense.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = CashflowExpense,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text("Total Spent", fontSize = 11.sp, color = Color(0xFFFECDD3))
                            AnimatedAmountCounter(
                                amount = totalExpense,
                                currencySymbol = currencySymbol,
                                isStealthMode = isStealthMode,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetWarningBanner(
    message: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF3F1D28) else Color(0xFFFFE4E6)
    val borderColor = if (isDark) CashflowExpense.copy(alpha = 0.5f) else Color(0xFFFECDD3)
    val textColor = if (isDark) Color(0xFFFECDD3) else Color(0xFF9F1239)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(CashflowExpense)
            )
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WalletStripCard(
    name: String,
    balance: Double,
    currencySymbol: String,
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .width(135.dp)
            .height(95.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%,.0f", balance)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun DateGroupHeader(
    dateLabel: String,
    dailyTotal: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "-$currencySymbol${String.format(Locale.US, "%,.2f", dailyTotal)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
    }
}
