package com.camuel.sanchay.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camuel.sanchay.CategoryBudget
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.StatisticsEngine
import com.camuel.sanchay.ui.components.ExpenseItem
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

enum class DateFilterPeriod(val displayName: String) {
    ALL_TIME("All Time"),
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_7_DAYS("Last 7 Days")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val allCategoryBudgets by viewModel.allCategoryBudgets.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Trends", "Breakdown", "50/30/20", "Budgets", "Health")

    var selectedCashflowType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedDateFilter by remember { mutableStateOf(DateFilterPeriod.THIS_MONTH) }
    var showFilterDropdown by remember { mutableStateOf(false) }

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    // Date Filtering Logic
    val filteredByDateExpenses = remember(allExpenses, selectedDateFilter) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()

        when (selectedDateFilter) {
            DateFilterPeriod.ALL_TIME -> allExpenses
            DateFilterPeriod.THIS_MONTH -> {
                val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
                allExpenses.filter { it.date.startsWith(currentYearMonth) }
            }
            DateFilterPeriod.LAST_30_DAYS -> {
                val cal30 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
                val threshold = cal30.time
                allExpenses.filter {
                    try {
                        val d = sdf.parse(it.date)
                        d != null && !d.before(threshold)
                    } catch (e: Exception) { true }
                }
            }
            DateFilterPeriod.LAST_7_DAYS -> {
                val cal7 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                val threshold = cal7.time
                allExpenses.filter {
                    try {
                        val d = sdf.parse(it.date)
                        d != null && !d.before(threshold)
                    } catch (e: Exception) { true }
                }
            }
        }
    }

    // Categorized breakdown based on Cashflow Type
    val activeTransactions = remember(filteredByDateExpenses, selectedCashflowType) {
        filteredByDateExpenses.filter { it.transactionType == selectedCashflowType }
    }

    val distributionByCategory = remember(activeTransactions) {
        activeTransactions.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toFloat()
            }
    }

    val transactionsByDate = remember(activeTransactions) {
        activeTransactions.groupBy { it.date }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toFloat()
            }
            .toSortedMap()
    }

    val filteredExpensesForDrillDown = remember(activeTransactions, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) activeTransactions else activeTransactions.filter { it.category == selectedCategoryFilter }
    }

    val metrics = remember(transactionsByDate, monthlyBudget) {
        StatisticsEngine.calculateMetrics(transactionsByDate, monthlyBudget)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Financial Analytics",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Cashflow & Spending Intelligence",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Control Bar: Cashflow Segmented Toggle & Date Range Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Toggle: Expense vs Income
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("EXPENSE" to "Expenses", "INCOME" to "Income").forEach { (type, label) ->
                        val isSelected = selectedCashflowType == type
                        val activeColor = if (type == "EXPENSE") CashflowExpense else CashflowIncome
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) activeColor else Color.Transparent)
                                .clickable {
                                    selectedCashflowType = type
                                    selectedCategoryFilter = null
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Date Filter Dropdown Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { showFilterDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedDateFilter.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DateFilterPeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = period.displayName,
                                        fontWeight = if (selectedDateFilter == period) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedDateFilter == period) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedDateFilter = period
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (activeTransactions.isEmpty() && selectedTab != 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No $selectedCashflowType transactions recorded for ${selectedDateFilter.displayName}.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    when (selectedTab) {
                        0 -> TrendsTab(
                            activeTransactions = activeTransactions,
                            transactionsByDate = transactionsByDate,
                            monthlyBudget = monthlyBudget,
                            currency = currency,
                            type = selectedCashflowType
                        )
                        1 -> BreakdownTab(
                            distributionByCategory = distributionByCategory,
                            currency = currency,
                            selectedFilter = selectedCategoryFilter,
                            selectedCashflowType = selectedCashflowType,
                            onCategoryClick = { selectedCategoryFilter = it }
                        )
                        2 -> Rule50_30_20Tab(
                            allExpenses = filteredByDateExpenses,
                            categoryBudgets = allCategoryBudgets,
                            currency = currency
                        )
                        3 -> CategoryBudgetsTab(
                            categoryBudgets = allCategoryBudgets,
                            expenses = filteredByDateExpenses,
                            currency = currency,
                            onAddBudgetClick = { showAddBudgetDialog = true },
                            onDeleteBudget = { viewModel.deleteCategoryBudget(it) }
                        )
                        4 -> HealthTab(metrics, currency, monthlyBudget)
                    }

                    // Transaction Drill-down (except for Budgets tab)
                    if (selectedTab != 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedCategoryFilter == null) "Recorded Activity" else "$selectedCategoryFilter Drilldown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedCategoryFilter != null) {
                                TextButton(onClick = { selectedCategoryFilter = null }) {
                                    Text("Clear Filter", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        filteredExpensesForDrillDown.take(6).forEach { expense ->
                            ExpenseItem(
                                expense = expense,
                                currencySymbol = currency,
                                onDelete = { viewModel.delete(it) },
                                onUpdate = { viewModel.update(it) }
                            )
                        }

                        if (filteredExpensesForDrillDown.size > 6) {
                            Text(
                                text = "Showing top 6 of ${filteredExpensesForDrillDown.size} transactions",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddBudgetDialog) {
        var categoryName by remember { mutableStateOf("") }
        var limitAmount by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("NEED") } // "NEED", "WANT", "SAVINGS"

        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    "Set Category Budget",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val fieldColors = standardDialogTextFieldColors()
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Category Name (e.g. Food, Fuel, Rent)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = limitAmount,
                        onValueChange = { limitAmount = it },
                        label = { Text("Monthly Limit ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors
                    )

                    Text(
                        "50/30/20 Classification",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "NEED" to "Need (50%)",
                            "WANT" to "Want (30%)",
                            "SAVINGS" to "Savings (20%)"
                        ).forEach { (type, label) ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitAmount.toDoubleOrNull() ?: 0.0
                        if (categoryName.isNotBlank() && limit > 0) {
                            viewModel.insertCategoryBudget(
                                CategoryBudget(
                                    categoryName = categoryName.trim(),
                                    monthlyLimit = limit,
                                    budgetType = selectedType
                                )
                            )
                            showAddBudgetDialog = false
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Limit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

data class DailyPaceStat(
    val dateStr: String,
    val dayOfWeek: String,
    val dayOfMonth: String,
    val amount: Float,
    val count: Int
)

@Composable
fun TrendsTab(
    activeTransactions: List<Expense>,
    transactionsByDate: Map<String, Float>,
    monthlyBudget: Float,
    currency: String,
    type: String
) {
    val themeColor = if (type == "INCOME") CashflowIncome else LeafGreenPrimary

    // Build rolling 7-day chronological window ending today
    val dailyStats = remember(transactionsByDate, activeTransactions) {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())

        val list = mutableListOf<DailyPaceStat>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dateKey = sdfDate.format(cal.time)
            val isToday = (i == 6)
            val dayName = if (isToday) "Today" else sdfDay.format(cal.time)
            val dayNum = sdfDayNum.format(cal.time)
            val amt = transactionsByDate[dateKey] ?: 0f
            val count = activeTransactions.count { it.date == dateKey }
            list.add(DailyPaceStat(dateKey, dayName, dayNum, amt, count))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val total7DayAmount = remember(dailyStats) { dailyStats.sumOf { it.amount.toDouble() }.toFloat() }
    val avgDailyBurn = remember(total7DayAmount) { total7DayAmount / 7f }
    val maxDailyAmount = remember(dailyStats) { maxOf(1f, dailyStats.maxOf { it.amount }) }
    val peakDay = remember(dailyStats) { dailyStats.maxByOrNull { it.amount } }

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    // ── 1. Daily Spending Pace Analytics ──────────────────────────────────
    Text(
        text = if (type == "INCOME") "Inflow Pace Trend" else "Daily Spending Pace",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    NeonCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = if (type == "INCOME") "Average Inflow Pace" else "Average Daily Burn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency ${String.format(Locale.getDefault(), "%.1f", avgDailyBurn)} / day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
                if (type == "EXPENSE" && monthlyBudget > 0f) {
                    val targetDaily = monthlyBudget / 30f
                    val isWithinTarget = avgDailyBurn <= targetDaily
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isWithinTarget) LeafGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isWithinTarget) LeafGreenPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isWithinTarget) "Pace on Track" else "Pace Accelerated",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            color = if (isWithinTarget) LeafGreenDeep else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Canvas Smooth Line / Area Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 12.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val chartHeight = h - bottomPadding - topPadding

                    // Horizontal reference gridlines
                    val gridY0 = topPadding + chartHeight
                    val gridY50 = topPadding + (chartHeight * 0.5f)
                    val gridY100 = topPadding

                    val gridColor = Color.LightGray.copy(alpha = 0.35f)
                    drawLine(gridColor, Offset(0f, gridY0), Offset(w, gridY0), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, gridY50), Offset(w, gridY50), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, gridY100), Offset(w, gridY100), strokeWidth = 1.dp.toPx())

                    // Calculate point offsets
                    val stepX = if (dailyStats.size > 1) w / (dailyStats.size - 1) else w
                    val points = dailyStats.mapIndexed { i, stat ->
                        val x = i * stepX
                        val normalized = if (maxDailyAmount > 0f) (stat.amount / maxDailyAmount).coerceIn(0f, 1f) else 0f
                        val y = gridY0 - (normalized * chartHeight)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val midX = (p0.x + p1.x) / 2
                                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                        }

                        // Gradient area below curve
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(points.last().x, gridY0)
                            lineTo(points.first().x, gridY0)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(themeColor.copy(alpha = 0.32f), Color.Transparent),
                                startY = topPadding,
                                endY = gridY0
                            )
                        )

                        // Stroke line
                        drawPath(
                            path = path,
                            color = themeColor,
                            style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Draw point markers
                        points.forEachIndexed { i, pt ->
                            val isSelected = selectedDayIndex == i
                            val isPeak = dailyStats[i].amount == maxDailyAmount && maxDailyAmount > 0f
                            val pointRadius = if (isSelected || isPeak) 6.dp.toPx() else 4.dp.toPx()
                            val pointColor = if (isSelected || isPeak) themeColor else themeColor.copy(alpha = 0.85f)

                            drawCircle(
                                color = Color.White,
                                radius = pointRadius + 2.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = pointColor,
                                radius = pointRadius,
                                center = pt
                            )
                        }
                    }
                }
            }

            // Day labels below line chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyStats.forEachIndexed { index, stat ->
                    val isSelected = selectedDayIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedDayIndex = if (selectedDayIndex == index) null else index }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stat.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stat.dayOfMonth,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── 2. Activity Volume (I/O) ──────────────────────────────────────────
    Text(
        text = "Activity Volume",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    NeonCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Volume Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Total Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency ${String.format(Locale.getDefault(), "%.2f", total7DayAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${dailyStats.sumOf { it.count }} txns",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }

            // Proportional Bar Chart for 7 Days
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEachIndexed { index, stat ->
                    val isSelected = selectedDayIndex == index
                    val fillFraction = if (maxDailyAmount > 0f) (stat.amount / maxDailyAmount).coerceIn(0.04f, 1f) else 0.04f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedDayIndex = if (selectedDayIndex == index) null else index }
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Label above bar
                        if (stat.amount > 0f) {
                            Text(
                                text = if (stat.amount >= 1000f) "${(stat.amount / 1000).toInt()}k" else stat.amount.toInt().toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Bar Capsule
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((100 * fillFraction).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(
                                    if (stat.amount > 0f) {
                                        Brush.verticalGradient(
                                            listOf(themeColor, themeColor.copy(alpha = 0.5f))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) themeColor else Color.Transparent,
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day & Date labels
                        Text(
                            text = stat.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stat.dayOfMonth,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Detail Card on Day Click
            selectedDayIndex?.let { idx ->
                val sel = dailyStats[idx]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${sel.dayOfWeek}, ${sel.dateStr}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                            Text(
                                text = "${sel.count} transaction(s) recorded",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%.2f", sel.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownTab(
    distributionByCategory: Map<String, Float>,
    currency: String,
    selectedFilter: String?,
    selectedCashflowType: String,
    onCategoryClick: (String?) -> Unit
) {
    Text(
        text = if (selectedCashflowType == "INCOME") "Income Sources" else "Spending Distribution",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    NeonCard {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val palette = listOf(
                Color(0xFF10B981),
                Color(0xFF6366F1),
                Color(0xFFF59E0B),
                Color(0xFFEC4899),
                Color(0xFF06B6D4),
                Color(0xFF8B5CF6),
                Color(0xFFF97316),
                Color(0xFF14B8A6)
            )

            val total = distributionByCategory.values.sum()
            var startAngle = -90f

            var animationPlayed by remember { mutableStateOf(false) }
            val animationProgress by animateFloatAsState(
                targetValue = if (animationPlayed) 1f else 0f,
                animationSpec = tween(durationMillis = 1200),
                label = "pieAnimation"
            )

            LaunchedEffect(Unit) {
                animationPlayed = true
            }

            Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 3.2f
                    val center = Offset(size.width / 2, size.height / 2)

                    if (total > 0f) {
                        distributionByCategory.values.forEachIndexed { i, v ->
                            val sweep = (v / total) * 360f * animationProgress
                            val percentage = (v / total * 100).toInt()

                            drawArc(
                                color = palette[i % palette.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 28.dp.toPx()),
                                size = Size(r * 2, r * 2),
                                topLeft = Offset(center.x - r, center.y - r)
                            )

                            if (animationProgress > 0.8f && percentage > 7) {
                                val midAngle = startAngle + sweep / 2
                                val labelRadius = r + 24.dp.toPx()
                                val x = center.x + labelRadius * cos(Math.toRadians(midAngle.toDouble())).toFloat()
                                val y = center.y + labelRadius * sin(Math.toRadians(midAngle.toDouble())).toFloat()

                                drawContext.canvas.nativeCanvas.drawText(
                                    "$percentage%",
                                    x, y,
                                    Paint().apply {
                                        color = palette[i % palette.size].toArgb()
                                        textSize = 10.sp.toPx()
                                        textAlign = Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                )
                            }
                            startAngle += sweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total $selectedCashflowType", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(
                        "$currency${String.format(Locale.US, "%,.0f", total.toDouble())}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Legend
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                distributionByCategory.forEach { (cat, amt) ->
                    val idx = distributionByCategory.keys.indexOf(cat)
                    val isSelected = selectedFilter == cat
                    val percentage = if (total > 0f) (amt / total * 100).toInt() else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onCategoryClick(if (isSelected) null else cat) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(palette[idx % palette.size], CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = cat,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$percentage%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "$currency${String.format(Locale.US, "%,.0f", amt.toDouble())}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Rule50_30_20Tab(
    allExpenses: List<Expense>,
    categoryBudgets: List<CategoryBudget>,
    currency: String
) {
    val totalIncome = allExpenses.filter { it.transactionType == "INCOME" }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val totalExpense = allExpenses.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

    // Map categories to Need, Want, Savings
    val budgetTypeMap = remember(categoryBudgets) {
        categoryBudgets.associate { it.categoryName.lowercase(Locale.ROOT) to it.budgetType }
    }

    val defaultNeedCategories = setOf("food", "grocery", "groceries", "rent", "bills", "utilities", "health", "medical", "transport", "fuel", "education", "insurance")
    val defaultSavingsCategories = setOf("investment", "savings", "mutual fund", "gold", "crypto", "debt repayment", "emergency fund")

    var needsSpent = 0.0
    var wantsSpent = 0.0
    var savingsSpent = 0.0

    allExpenses.filter { it.transactionType == "EXPENSE" }.forEach { exp ->
        val amt = exp.amount.toDoubleOrNull() ?: 0.0
        val catLower = exp.category.lowercase(Locale.ROOT)
        val assignedType = budgetTypeMap[catLower]

        when {
            assignedType == "NEED" || (assignedType == null && defaultNeedCategories.contains(catLower)) -> {
                needsSpent += amt
            }
            assignedType == "SAVINGS" || (assignedType == null && defaultSavingsCategories.contains(catLower)) -> {
                savingsSpent += amt
            }
            else -> {
                wantsSpent += amt
            }
        }
    }

    val baseIncome = if (totalIncome > 0) totalIncome else max(totalExpense, 1.0)
    val needsTarget = baseIncome * 0.50
    val wantsTarget = baseIncome * 0.30
    val savingsTarget = baseIncome * 0.20

    val needsRatio = (needsSpent / baseIncome).toFloat()
    val wantsRatio = (wantsSpent / baseIncome).toFloat()
    val savingsRatio = (savingsSpent / baseIncome).toFloat()

    Text(
        text = "50/30/20 Budget Optimization",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    // Summary Card
    NeonCard(glowColor = MaterialTheme.colorScheme.primary) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Optimal Cashflow Standard",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "50% Needs, 30% Wants, 20% Wealth & Savings calculated against your inflow ($currency${String.format(Locale.US, "%,.0f", baseIncome)}).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Needs Pillar
            PillarBreakdownRow(
                title = "Needs (50% Target)",
                subtitle = "Essentials, Rent, Utilities, Food",
                actualAmount = needsSpent,
                targetAmount = needsTarget,
                actualRatio = needsRatio,
                targetRatio = 0.50f,
                currency = currency,
                accentColor = Color(0xFF3B82F6) // Blue
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Wants Pillar
            PillarBreakdownRow(
                title = "Wants (30% Target)",
                subtitle = "Dining, Shopping, Entertainment, Hobbies",
                actualAmount = wantsSpent,
                targetAmount = wantsTarget,
                actualRatio = wantsRatio,
                targetRatio = 0.30f,
                currency = currency,
                accentColor = Color(0xFFF59E0B) // Amber
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Savings Pillar
            PillarBreakdownRow(
                title = "Savings & Investments (20% Target)",
                subtitle = "Emergency Fund, Investments, Debt Payoff",
                actualAmount = savingsSpent,
                targetAmount = savingsTarget,
                actualRatio = savingsRatio,
                targetRatio = 0.20f,
                currency = currency,
                accentColor = Color(0xFF10B981) // Green
            )
        }
    }

    // Recommendation Card
    val advice = when {
        wantsRatio > 0.35f -> "⚠️ Your discretionary 'Wants' spending is ${String.format(Locale.US, "%.1f", wantsRatio * 100)}% (exceeding the 30% rule). Consider trimming entertainment or shopping to boost savings."
        needsRatio > 0.55f -> "⚠️ Your 'Needs' take up ${String.format(Locale.US, "%.1f", needsRatio * 100)}% of your income. Look for recurring utility/bill subscription optimizations."
        savingsRatio >= 0.20f -> "🎉 Outstanding discipline! You are exceeding the 20% savings rule. Your wealth trajectory is on pace."
        else -> "💡 Balanced spending pattern. Increasing automated deposits will bring you to the 20% savings target."
    }

    NeonCard {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = advice,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PillarBreakdownRow(
    title: String,
    subtitle: String,
    actualAmount: Double,
    targetAmount: Double,
    actualRatio: Float,
    targetRatio: Float,
    currency: String,
    accentColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(actualRatio * 100).toInt()}% (${currency}${String.format(Locale.US, "%,.0f", actualAmount)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (actualRatio / targetRatio).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun CategoryBudgetsTab(
    categoryBudgets: List<CategoryBudget>,
    expenses: List<Expense>,
    currency: String,
    onAddBudgetClick: () -> Unit,
    onDeleteBudget: (CategoryBudget) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Category Budget Limits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = onAddBudgetClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Set Limit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (categoryBudgets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category budgets set yet.\nTap 'Set Limit' above to cap specific spending categories.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categoryBudgets.forEach { budget ->
                val spent = expenses
                    .filter { it.transactionType == "EXPENSE" && it.category.equals(budget.categoryName, ignoreCase = true) }
                    .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

                val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                val statusColor = when {
                    ratio >= 1.0f -> CashflowExpense
                    ratio >= 0.70f -> CashflowWarning
                    else -> LeafGreenPrimary
                }

                NeonCard(glowColor = statusColor) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = budget.categoryName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = budget.budgetType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteBudget(budget) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$currency${String.format(Locale.US, "%,.0f", spent)} of $currency${String.format(Locale.US, "%,.0f", budget.monthlyLimit)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${(ratio * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthTab(metrics: StatisticsEngine.EconomicMetrics, currency: String, budget: Float) {
    Text(
        text = "Financial Health Score",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    val healthColor = if (metrics.healthScore > 70) LeafGreenPrimary else if (metrics.healthScore > 40) CashflowWarning else CashflowExpense
    NeonCard(glowColor = healthColor) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                CircularProgressIndicator(
                    progress = { metrics.healthScore.toFloat() / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = healthColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 7.dp
                )
                Text(
                    text = "${metrics.healthScore}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = if (metrics.healthScore > 70) "Healthy Cashflow" else "Action Recommended",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (metrics.isOverBudget) "You have exceeded your monthly budget allocation." else "You are tracking well within your safety parameters.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }

    Text(
        text = "Daily Burn Analysis",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        NeonCard(modifier = Modifier.weight(1f)) {
            Column(Modifier.padding(14.dp)) {
                Text("Actual Daily Burn", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(
                    "$currency${String.format(Locale.US, "%,.0f", metrics.actualDailyBurn.toDouble())}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        NeonCard(modifier = Modifier.weight(1f), glowColor = LeafGreenDeep) {
            Column(Modifier.padding(14.dp)) {
                Text("Target Run-Rate", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(
                    "$currency${String.format(Locale.US, "%,.0f", metrics.burnRate.toDouble())}",
                    color = LeafGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }

    NeonCard(glowColor = LeafGreenPrimary) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = LeafGreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = "To stay within your $currency${String.format(Locale.US, "%,.0f", budget.toDouble())} plan, keep average daily spending under $currency${String.format(Locale.US, "%.2f", metrics.burnRate)}.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}
