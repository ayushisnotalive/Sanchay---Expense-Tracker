package com.camuel.sanchay.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.Wallet
import com.camuel.sanchay.utils.CategorizationEngine
import com.camuel.sanchay.ui.components.bouncyClickable
import com.camuel.sanchay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class CategoryOption(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    // 3-Way Transaction Type Switch: EXPENSE, INCOME, TRANSFER
    var transactionType by remember { mutableStateOf("EXPENSE") }

    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryOption?>(null) }
    
    val globalCurrency by viewModel.currencySymbol.collectAsState()
    var selectedCurrency by remember(globalCurrency) { mutableStateOf(globalCurrency) }
    val currencies = listOf("₹", "$", "€", "£", "¥", "A$", "C$", "AED")
    var showCurrencyMenu by remember { mutableStateOf(false) }

    val wallets by viewModel.allWallets.collectAsState()
    var selectedWallet by remember { mutableStateOf(wallets.firstOrNull()) }
    var selectedTargetWallet by remember { mutableStateOf(wallets.getOrNull(1)) }
    var showWalletMenu by remember { mutableStateOf(false) }
    var showTargetWalletMenu by remember { mutableStateOf(false) }

    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    val currentMonthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
    val currentMonthExpenses = remember(allExpenses) {
        allExpenses.filter { it.transactionType == "EXPENSE" && it.date.startsWith(currentMonthPrefix) }
    }
    val currentMonthSpent = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }

    val numericEnteredAmount = remember(amount) {
        amount.toDoubleOrNull() ?: 0.0
    }

    val isBudgetSet = monthlyBudget > 0f
    val projectedMonthSpent = currentMonthSpent + numericEnteredAmount
    val isExceedingBudget = isBudgetSet && transactionType == "EXPENSE" && (projectedMonthSpent > monthlyBudget)
    val budgetRemaining = maxOf(0.0, monthlyBudget.toDouble() - currentMonthSpent)
    val overageAmount = projectedMonthSpent - monthlyBudget.toDouble()

    var showBudgetAdjustDialog by remember { mutableStateOf(false) }
    var newBudgetString by remember { mutableStateOf("") }

    LaunchedEffect(wallets) {
        if (selectedWallet == null && wallets.isNotEmpty()) {
            selectedWallet = wallets.firstOrNull()
        }
        if (selectedTargetWallet == null && wallets.size > 1) {
            selectedTargetWallet = wallets.getOrNull(1)
        }
    }
    
    var showScanner by remember { mutableStateOf(false) }
    var receiptImageUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Camera launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) showScanner = true
    }

    // Photo picker launcher for receipt attachments
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            receiptImageUri = uri.toString()
        }
    }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
    }

    val fusedLocationClient = remember {
        try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Throwable) {
            null
        }
    }
    val isTravelModeEnabled by viewModel.isTravelModeEnabled.collectAsState()

    LaunchedEffect(hasLocationPermission, isTravelModeEnabled) {
        if (hasLocationPermission && fusedLocationClient != null) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        currentLatitude = loc.latitude
                        currentLongitude = loc.longitude
                    }
                }
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLatitude = location.latitude
                            currentLongitude = location.longitude
                            if (isTravelModeEnabled) {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                        if (addresses.isNotEmpty()) {
                                            addresses[0].countryCode?.let { code ->
                                                val sym = getCurrencyForCountry(code)
                                                if (sym != null) selectedCurrency = sym
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            } catch (t: Throwable) { }
        }
    }

    // Category options based on type
    val expenseCategories = listOf(
        CategoryOption("Food", Icons.Filled.Fastfood),
        CategoryOption("Transport", Icons.Filled.Flight),
        CategoryOption("Bills", Icons.Filled.Receipt),
        CategoryOption("Shopping", Icons.Filled.ShoppingBag),
        CategoryOption("Health", Icons.Filled.LocalHospital),
        CategoryOption("Entertainment", Icons.Filled.Movie),
        CategoryOption("Other", Icons.Filled.MoreHoriz)
    )

    val incomeCategories = listOf(
        CategoryOption("Salary", Icons.Filled.Work),
        CategoryOption("Freelance", Icons.Filled.Laptop),
        CategoryOption("Investments", Icons.AutoMirrored.Filled.TrendingUp),
        CategoryOption("Gift", Icons.Filled.CardGiftcard),
        CategoryOption("Refund", Icons.AutoMirrored.Filled.Undo),
        CategoryOption("Rental", Icons.Filled.Home),
        CategoryOption("Other", Icons.Filled.MoreHoriz)
    )

    val savedCategories by viewModel.allCategories.collectAsState()
    val activeCategories = remember(transactionType, savedCategories) {
        if (transactionType == "INCOME") {
            incomeCategories
        } else {
            (expenseCategories + savedCategories.map { CategoryOption(it.name, Icons.Filled.Star) })
                .distinctBy { it.name.trim().lowercase(Locale.ROOT) }
        }
    }

    // Default select first category when switching mode
    LaunchedEffect(transactionType) {
        selectedCategory = activeCategories.firstOrNull()
    }
    
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var paymentMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    if (showScanner) {
        CameraScannerScreen(
            onAmountDetected = { detectedAmount, rawText ->
                amount = detectedAmount
                CategorizationEngine.suggestCategory(rawText).let { sug ->
                    activeCategories.find { it.name.equals(sug, ignoreCase = true) }?.let { selectedCategory = it }
                }
                showScanner = false
            },
            onBack = { showScanner = false }
        )
        return
    }

    val commonFieldColors = OutlinedTextFieldDefaults.colors(
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (transactionType) {
                            "INCOME" -> "Add Income"
                            "TRANSFER" -> "Transfer Funds"
                            else -> "Add Expense"
                        }, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    if (transactionType == "EXPENSE") {
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                showScanner = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Scan Receipt", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Segmented Toggle [ Expense | Income | Transfer ] ─────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("EXPENSE", "Expense", CashflowExpense),
                    Triple("INCOME", "Income", CashflowIncome),
                    Triple("TRANSFER", "Transfer", CashflowTransfer)
                ).forEach { (typeKey, label, color) ->
                    val isSelected = transactionType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) color else Color.Transparent)
                            .clickable { transactionType = typeKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Hero Amount Input ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (transactionType) {
                        "INCOME" -> "Income Amount"
                        "TRANSFER" -> "Transfer Amount"
                        else -> "Expense Amount"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box {
                        Text(
                            text = selectedCurrency,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCurrencyMenu = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false }
                        ) {
                            currencies.forEach { sym ->
                                DropdownMenuItem(
                                    text = { Text(sym) },
                                    onClick = {
                                        selectedCurrency = sym
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            if (input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                amount = input
                            }
                        },
                        placeholder = { Text("0.00", fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.wrapContentWidth().widthIn(min = 120.dp, max = 280.dp)
                    )
                }
            }

            // ── Budget Status & Warning Banner ───────────────────────────────
            if (transactionType == "EXPENSE" && isBudgetSet) {
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isExceedingBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (isExceedingBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (isExceedingBudget) Icons.Filled.Warning else Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (isExceedingBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isExceedingBudget) "Monthly Budget Limit Exceeded" else "Monthly Budget Tracker",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExceedingBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "$globalCurrency${String.format(Locale.getDefault(), "%.0f", currentMonthSpent)} / $globalCurrency${String.format(Locale.getDefault(), "%.0f", monthlyBudget)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Progress bar
                    val currentRatio = if (monthlyBudget > 0f) (currentMonthSpent / monthlyBudget.toDouble()).toFloat().coerceIn(0f, 1f) else 0f
                    val safeRatio = if (currentRatio.isNaN()) 0f else currentRatio
                    LinearProgressIndicator(
                        progress = { safeRatio },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (isExceedingBudget) MaterialTheme.colorScheme.error else LeafGreenPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (isExceedingBudget) {
                        Text(
                            text = "⚠️ Exceeds budget by $globalCurrency${String.format(Locale.getDefault(), "%.2f", overageAmount)}. Update budget to continue.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 15.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    newBudgetString = ((monthlyBudget + overageAmount.toFloat() + 500f) / 100).toInt().times(100).toString()
                                    showBudgetAdjustDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("Update Budget Limit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Text(
                            text = "Remaining allowance: $globalCurrency${String.format(Locale.getDefault(), "%.0f", budgetRemaining)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Wallet Selectors ──────────────────────────────────────────────
            if (transactionType == "TRANSFER") {
                // Transfer: From Wallet -> To Wallet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // From Wallet
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showWalletMenu = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("From: ${selectedWallet?.name ?: "Wallet"}", maxLines = 1, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = showWalletMenu, onDismissRequest = { showWalletMenu = false }) {
                            wallets.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text("${w.name} (${w.currency}${w.balance})") },
                                    onClick = { selectedWallet = w; showWalletMenu = false }
                                )
                            }
                        }
                    }

                    // To Wallet
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showTargetWalletMenu = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("To: ${selectedTargetWallet?.name ?: "Target"}", maxLines = 1, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = showTargetWalletMenu, onDismissRequest = { showTargetWalletMenu = false }) {
                            wallets.filter { it.id != selectedWallet?.id }.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text("${w.name} (${w.currency}${w.balance})") },
                                    onClick = { selectedTargetWallet = w; showTargetWalletMenu = false }
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal Account / Wallet Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showWalletMenu = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (transactionType == "INCOME") "Deposit into:" else "Account:")
                            Text(
                                text = "${selectedWallet?.name ?: "Select Wallet"} (${selectedWallet?.currency ?: ""}${selectedWallet?.balance ?: 0.0})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DropdownMenu(expanded = showWalletMenu, onDismissRequest = { showWalletMenu = false }) {
                        wallets.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} — Balance: ${w.currency}${w.balance}") },
                                onClick = { selectedWallet = w; showWalletMenu = false }
                            )
                        }
                    }
                }
            }

            // ── Category Selection (For Expense and Income) ───────────────────
            if (transactionType != "TRANSFER") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeCategories.forEach { category ->
                            val isCatSelected = selectedCategory?.name == category.name
                            val activeTint = if (transactionType == "INCOME") CashflowIncome else MaterialTheme.colorScheme.primary
                            Column(
                                modifier = Modifier
                                    .widthIn(min = 72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCatSelected) activeTint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isCatSelected) 2.dp else 1.dp,
                                        color = if (isCatSelected) activeTint else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .bouncyClickable { selectedCategory = category }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.name,
                                    tint = if (isCatSelected) activeTint else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = category.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCatSelected) activeTint else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ── Date and Note Fields ──────────────────────────────────────────
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date:")
                    Text(selectedDate, fontWeight = FontWeight.Bold)
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = commonFieldColors,
                shape = RoundedCornerShape(14.dp)
            )

            if (transactionType == "EXPENSE") {
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (#food, #trip)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = commonFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )

                // ── Receipt Photo Attachment ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable { photoPickerLauncher.launch("image/*") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Attach Receipt", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (receiptImageUri != null) "Receipt Attached ✅" else "Attach Receipt Photo",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (receiptImageUri != null) {
                        AsyncImage(
                            model = receiptImageUri,
                            contentDescription = "Receipt Preview",
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }

            // ── Confirm Action Button ─────────────────────────────────────────
            Button(
                onClick = {
                    val numericAmount = amount.toDoubleOrNull() ?: 0.0
                    if (numericAmount <= 0) return@Button

                    when (transactionType) {
                        "TRANSFER" -> {
                            val from = selectedWallet
                            val to = selectedTargetWallet
                            if (from != null && to != null && from.id != to.id) {
                                viewModel.transferFunds(from, to, numericAmount, note)
                                onBack()
                            }
                        }
                        "INCOME" -> {
                            val targetWallet = selectedWallet ?: wallets.firstOrNull()
                            val cat = selectedCategory?.name ?: "Other"
                            val newExpense = Expense(
                                amount = amount,
                                category = cat,
                                date = selectedDate,
                                paymentMethod = targetWallet?.name ?: "Direct Credit",
                                note = note,
                                latitude = currentLatitude,
                                longitude = currentLongitude,
                                walletId = targetWallet?.id ?: 1,
                                transactionType = "INCOME",
                                receiptImageUri = receiptImageUri
                            )
                            viewModel.saveExpenseWithConversion(amount, selectedCurrency, globalCurrency, newExpense)
                            onBack()
                        }
                        else -> {
                            // Expense
                            val tagList = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val newExpense = Expense(
                                amount = amount,
                                category = selectedCategory?.name ?: "Other",
                                date = selectedDate,
                                paymentMethod = paymentMethod,
                                note = note,
                                latitude = currentLatitude,
                                longitude = currentLongitude,
                                walletId = selectedWallet?.id ?: 1,
                                tags = tagList,
                                transactionType = "EXPENSE",
                                receiptImageUri = receiptImageUri
                            )
                            viewModel.saveExpenseWithConversion(amount, selectedCurrency, globalCurrency, newExpense)
                            onBack()
                        }
                    }
                },
                enabled = amount.isNotBlank() && (transactionType == "TRANSFER" || selectedCategory != null) && !isExceedingBudget,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (transactionType) {
                        "INCOME" -> CashflowIncome
                        "TRANSFER" -> CashflowTransfer
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    text = when {
                        isExceedingBudget -> "Monthly Budget Exceeded"
                        transactionType == "INCOME" -> "Confirm Income"
                        transactionType == "TRANSFER" -> "Confirm Transfer"
                        else -> "Confirm Expense"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isExceedingBudget && transactionType == "EXPENSE") {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🔒 Monthly budget limit reached.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showBudgetAdjustDialog) {
                AlertDialog(
                    onDismissRequest = { showBudgetAdjustDialog = false },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Update Monthly Budget", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Budget: $globalCurrency${String.format(Locale.getDefault(), "%.0f", monthlyBudget)} • Spent: $globalCurrency${String.format(Locale.getDefault(), "%.0f", currentMonthSpent)}\n\nEnter a new monthly limit:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = newBudgetString,
                                onValueChange = { newBudgetString = it },
                                label = { Text("New Monthly Budget ($globalCurrency)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth(),
                                colors = commonFieldColors
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newB = newBudgetString.toFloatOrNull()
                                if (newB != null && newB > 0f) {
                                    viewModel.setMonthlyBudget(newB)
                                    showBudgetAdjustDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Update Budget", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBudgetAdjustDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun getCurrencyForCountry(countryCode: String): String? {
    return when (countryCode.uppercase()) {
        "US", "CA", "AU", "NZ", "SG", "HK" -> "$"
        "GB" -> "£"
        "IN" -> "₹"
        "JP" -> "¥"
        "EU", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "GR", "PT", "FI", "IE" -> "€"
        "CN" -> "¥"
        "RU" -> "₽"
        "KR" -> "₩"
        "BR" -> "R$"
        "MX" -> "$"
        "AE" -> "AED"
        "SA" -> "SAR"
        "CH" -> "Fr"
        else -> null
    }
}
