package com.camuel.sanchay.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.camuel.sanchay.ui.components.bouncyClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ui.components.*
import com.camuel.sanchay.ui.theme.*
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/** Returns today as yyyy-MM-dd */
private fun todayStr(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/** Returns start-of-week (Monday) as yyyy-MM-dd */
private fun startOfWeekStr(): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

/** Returns first day of current month as yyyy-MM-dd */
private fun startOfMonthStr(): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAIAssistantClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onWalletsClick: () -> Unit = {}
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allWallets by viewModel.allWallets.collectAsState()
    val allInvestments by viewModel.allInvestments.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val allCategoryBudgets by viewModel.allCategoryBudgets.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isStealthMode by viewModel.isStealthMode.collectAsState()
    
    val currencies = listOf("₹", "$", "€", "£", "¥", "A$", "C$", "AED")
    var selectedTimeFilter by remember { mutableStateOf("Month") }

    val snackbarHostState = remember { SnackbarHostState() }

    // ── Filter date bounds ────────────────────────────────────────────
    val today = todayStr()
    val weekStart = startOfWeekStr()
    val monthStart = startOfMonthStr()

    // Filter expenses based on selected time tab
    val filteredExpenses = remember(allExpenses, selectedTimeFilter) {
        when (selectedTimeFilter) {
            "Day"   -> allExpenses.filter { it.date == today }
            "Week"  -> allExpenses.filter { it.date in weekStart..today }
            "Month" -> allExpenses.filter { it.date in monthStart..today }
            else    -> allExpenses
        }
    }

    // Filtered totals by transaction type
    val filteredExpenseTotal = remember(filteredExpenses) {
        filteredExpenses.filter { it.transactionType == "EXPENSE" }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }
    val filteredIncomeTotal = remember(filteredExpenses) {
        filteredExpenses.filter { it.transactionType == "INCOME" }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }

    // Scale budget so the ring makes sense per period
    val activeBudget = remember(selectedTimeFilter, monthlyBudget) {
        when (selectedTimeFilter) {
            "Day"   -> monthlyBudget / 30f
            "Week"  -> monthlyBudget / 4.3f
            else    -> monthlyBudget
        }
    }

    // Enterprise Net Worth & Liquid Cash
    val liquidCash = remember(allWallets) { allWallets.sumOf { it.balance } }
    val portfolioValue = remember(allInvestments) { allInvestments.sumOf { it.quantity * it.currentPrice } }
    val totalLiabilities = remember(allDebts) { allDebts.filter { !it.isSettled && it.type == "Borrowed" }.sumOf { it.amount } }
    val netWorth = remember(liquidCash, portfolioValue, totalLiabilities) { (liquidCash + portfolioValue) - totalLiabilities }

    val filterLabel = when (selectedTimeFilter) {
        "Day"  -> "Today"
        "Week" -> "This Week"
        else   -> "This Month"
    }

    // Group transactions by date
    val groupedExpenses = remember(filteredExpenses) {
        filteredExpenses.groupBy { it.date }
    }

    // Budget warning check
    val budgetExceeded = activeBudget > 0 && filteredExpenseTotal > activeBudget
    val budgetWarningMessage = when {
        budgetExceeded -> "Monthly budget limit exceeded! Tap to review details."
        activeBudget > 0 && (filteredExpenseTotal / activeBudget) >= 0.85 -> "Approaching 85% of your monthly budget limit."
        else -> null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            var showMoreMenu by remember { mutableStateOf(false) }
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (profilePhotoUri.isNotEmpty()) {
                            val imageModel = remember(profilePhotoUri) {
                                if (profilePhotoUri.startsWith("/")) java.io.File(profilePhotoUri) else profilePhotoUri
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (userName.isNotBlank()) userName.take(1).uppercase() else "S",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (userName.isNotBlank()) "Hi, $userName 👋" else "Good morning 👋", 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Wealth Tracker",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onAIAssistantClick) {
                        Icon(Icons.Default.AutoAwesome, "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onSettingsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isStealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = if (isStealthMode) CashflowWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(if (isStealthMode) "Privacy: Hidden" else "Privacy: Visible", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                },
                                onClick = {
                                    viewModel.toggleStealthMode()
                                    showMoreMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contextual Budget Warning Banner (From Ref 1)
            if (budgetWarningMessage != null) {
                item {
                    BudgetWarningBanner(
                        message = budgetWarningMessage,
                        onClick = { /* navigate to budget details */ }
                    )
                }
            }

            // Time Filters: Day / Week / Month
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Day", "Week", "Month").forEach { filter ->
                        val isSelected = selectedTimeFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .bouncyClickable { selectedTimeFilter = filter }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Hero Financial Card (As seen in Ref 1, Ref 2, Ref 4)
            item {
                HeroBalanceCard(
                    totalBalance = liquidCash,
                    totalIncome = filteredIncomeTotal,
                    totalExpense = filteredExpenseTotal,
                    currencySymbol = currencySymbol,
                    isStealthMode = isStealthMode,
                    deltaText = if (filteredIncomeTotal > filteredExpenseTotal) "+$currencySymbol${String.format(Locale.US, "%,.0f", filteredIncomeTotal - filteredExpenseTotal)} net saved $filterLabel" else null,
                    onVisibilityToggle = { viewModel.toggleStealthMode() }
                )
            }

            // Quick Search Section (Moved upside on main screen)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bouncyClickable { onSearchClick() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Search transactions, categories, notes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // My Wallets Horizontal Strip (As seen in Ref 1)
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onWalletsClick() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Wallets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${allWallets.size} accounts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Wallets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (allWallets.isEmpty()) {
                        Text("No wallets created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(allWallets, key = { it.clientUuid.ifEmpty { it.id.toString() } }) { wallet ->
                                WalletStripCard(
                                    name = wallet.name,
                                    balance = wallet.balance,
                                    currencySymbol = wallet.currency,
                                    colorHex = wallet.colorHex,
                                    isSelected = false,
                                    onClick = onWalletsClick
                                )
                            }
                        }
                    }
                }
            }

            // Spending Overview & Budget Progress
            item {
                NeonCard(
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Budget Progress — $filterLabel",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                MaskableAmountText(
                                    amount = filteredExpenseTotal,
                                    currencySymbol = currencySymbol,
                                    isStealthMode = isStealthMode,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Budget: $currencySymbol${String.format(Locale.US, "%,.0f", activeBudget.toDouble())}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val budgetPercent = if (activeBudget > 0) ((filteredExpenseTotal / activeBudget.toDouble()) * 100).toInt() else 0
                                Text(
                                    text = "$budgetPercent% utilized",
                                    fontSize = 11.sp,
                                    color = if (budgetPercent > 90) CashflowExpense else if (budgetPercent > 70) CashflowWarning else CashflowIncome,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val progress = if (activeBudget > 0) (filteredExpenseTotal / activeBudget.toDouble()).coerceIn(0.0, 1.0).toFloat() else 0f
                        val animatedProgress by animateFloatAsState(progress, tween(1000), label = "progress")
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                progress < 0.6f -> CashflowIncome
                                progress < 0.85f -> CashflowWarning
                                else -> CashflowExpense
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Smart Insight Card
            if (insights.isNotEmpty()) {
                item {
                    NeonCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lightbulb, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Smart Financial Insight", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(insights.first(), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transactions — $filterLabel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${filteredExpenses.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Date-Grouped Transaction Feed (From Ref 4)
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No transactions $filterLabel.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tap the center + button to log an entry.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                groupedExpenses.forEach { (date, itemsForDate) ->
                    item(key = "header_$date") {
                        val dateLabel = when (date) {
                            today -> "Today"
                            else -> date
                        }
                        val dayExpenseSum = itemsForDate
                            .filter { it.transactionType == "EXPENSE" }
                            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                        DateGroupHeader(
                            dateLabel = dateLabel,
                            dailyTotal = dayExpenseSum,
                            currencySymbol = currencySymbol
                        )
                    }
                    items(itemsForDate, key = { it.clientUuid.ifEmpty { it.id.toString() } }) { expense ->
                        Box(modifier = Modifier.animateItem()) {
                            ExpenseItem(
                                expense = expense, 
                                currencySymbol = currencySymbol,
                                isStealthMode = isStealthMode,
                                onDelete = { viewModel.delete(it) },
                                onUpdate = { viewModel.update(it) }
                            )
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
