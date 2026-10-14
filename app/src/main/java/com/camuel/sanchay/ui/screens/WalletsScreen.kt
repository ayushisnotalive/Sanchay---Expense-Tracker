package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.Wallet
import com.camuel.sanchay.ui.components.MaskableAmountText
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.components.bouncyClickable
import com.camuel.sanchay.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val wallets by viewModel.allWallets.collectAsState()
    val baseCurrency by viewModel.currencySymbol.collectAsState()
    val isStealthMode by viewModel.isStealthMode.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<Wallet?>(null) }
    var walletToDelete by remember { mutableStateOf<Wallet?>(null) }

    val totalLiquidBalance = remember(wallets) {
        wallets.sumOf { it.balance }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Wallets & Accounts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("Manage liquid balances", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleStealthMode() }) {
                        Icon(
                            imageVector = if (isStealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Privacy Mode",
                            tint = if (isStealthMode) EnterpriseAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showTransferDialog = true }, enabled = wallets.size >= 2) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer Funds",
                            tint = if (wallets.size >= 2) MaterialTheme.colorScheme.primary else Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add Wallet")
                    Text("New Wallet", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Banner: Total Liquid Treasury
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(4.dp))
                    NeonCard(
                        glowColor = MaterialTheme.colorScheme.primary,
                        cornerRadius = 24.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EnterpriseEmerald.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = EnterpriseEmerald,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = "Total Liquid Cash",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${wallets.size} ${if (wallets.size == 1) "Account" else "Accounts"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            MaskableAmountText(
                                amount = totalLiquidBalance,
                                currencySymbol = baseCurrency,
                                isStealthMode = isStealthMode,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showTransferDialog = true },
                                    enabled = wallets.size >= 2,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LeafGreenPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Wallet", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Dedicated Currency Selection Option
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NeonCard(
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = "Currency",
                                        tint = LeafGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Primary Currency",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LeafGreenPrimary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeafGreenPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "Active: $baseCurrency",
                                        color = LeafGreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Select active currency for your accounts and transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val availableCurrencies = listOf("₹", "$", "€", "£", "¥", "A$", "C$", "AED", "CHF", "CAD", "SGD")
                                items(availableCurrencies, key = { it }) { sym ->
                                    val isSelected = sym == baseCurrency
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                            .bouncyClickable {
                                                viewModel.setCurrency(sym)
                                                Toast.makeText(context, "Currency set to $sym", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sym,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Configured Accounts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Tap card to edit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (wallets.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("No wallets configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tap '+ New Wallet' to add an account.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(wallets, key = { it.clientUuid.ifEmpty { it.id.toString() } }) { wallet ->
                        EnterpriseWalletCard(
                            wallet = wallet,
                            isStealthMode = isStealthMode,
                            canDelete = wallets.size > 1,
                            onClick = { walletToEdit = wallet },
                            onDelete = { walletToDelete = wallet }
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(88.dp))
                }
            }
        }
    }

    // ── Dialog: Delete Confirmation ───────────────────────────────────────────
    if (walletToDelete != null) {
        val target = walletToDelete!!
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Delete Wallet", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.name}'? Existing transactions linked to this wallet will remain in records.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWallet(target)
                    walletToDelete = null
                }) { Text("Delete", color = ExpenseColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    // ── Dialog: Add New Wallet ────────────────────────────────────────────────
    if (showAddDialog) {
        WalletFormDialog(
            title = "New Enterprise Account",
            initialName = "",
            initialBalance = 0.0,
            initialCurrency = "₹", // INR Default!
            initialColorHex = "#2E7D32", // Solid Leaf Green Default!
            onDismiss = { showAddDialog = false },
            onSave = { name, balance, currency, colorHex ->
                viewModel.insertWallet(
                    Wallet(
                        name = name.trim(),
                        balance = balance,
                        currency = currency,
                        colorHex = colorHex
                    )
                )
                showAddDialog = false
            }
        )
    }

    // ── Dialog: Edit Existing Wallet ──────────────────────────────────────────
    if (walletToEdit != null) {
        val current = walletToEdit!!
        WalletFormDialog(
            title = "Edit Account",
            initialName = current.name,
            initialBalance = current.balance,
            initialCurrency = current.currency,
            initialColorHex = current.colorHex,
            onDismiss = { walletToEdit = null },
            onSave = { name, balance, currency, colorHex ->
                current.name = name.trim()
                current.balance = balance
                current.currency = currency
                current.colorHex = colorHex
                current.isDirty = true
                viewModel.updateWallet(current)
                walletToEdit = null
            }
        )
    }

    // ── Dialog: Inter-Wallet Funds Transfer ───────────────────────────────────
    if (showTransferDialog && wallets.size >= 2) {
        TransferFundsDialog(
            wallets = wallets,
            onDismiss = { showTransferDialog = false },
            onTransfer = { from, to, amount, note ->
                viewModel.transferFunds(from, to, amount, note)
                showTransferDialog = false
            }
        )
    }
}

@Composable
fun EnterpriseWalletCard(
    wallet: Wallet,
    isStealthMode: Boolean,
    canDelete: Boolean = false,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val accentColor = remember(wallet.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(wallet.colorHex)) }
            .getOrDefault(EnterpriseEmerald)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = accentColor,
                shape = RoundedCornerShape(20.dp)
            )
            .bouncyClickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = wallet.currency,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp).padding(start = 4.dp)
                        ) {
                            Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = wallet.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            MaskableAmountText(
                amount = wallet.balance,
                currencySymbol = wallet.currency,
                isStealthMode = isStealthMode,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LeafGreenPrimary
            )
        }
    }
}

@Composable
fun WalletFormDialog(
    title: String,
    initialName: String,
    initialBalance: Double,
    initialCurrency: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onSave: (name: String, balance: Double, currency: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var balanceStr by remember { mutableStateOf(if (initialBalance == 0.0) "" else initialBalance.toString()) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }

    val currencyOptions = listOf(
        "₹" to "INR (₹)",
        "$" to "USD ($)",
        "€" to "EUR (€)",
        "£" to "GBP (£)",
        "¥" to "JPY (¥)",
        "A$" to "AUD (A$)",
        "C$" to "CAD (C$)",
        "AED" to "AED"
    )

    val colorOptions = listOf(
        "#2E7D32" to LeafGreenPrimary,
        "#1B5E20" to LeafGreenDeep,
        "#388E3C" to LeafGreenMedium,
        "#4C8C5A" to SageGreen,
        "#558B2F" to OliveGreen,
        "#C05C46" to EnterpriseRose
    )

    val fieldColors = standardDialogTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. HDFC Salary, Cash)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Balance") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                )

                Text("Currency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currencyOptions.take(5).forEach { (symbol, label) ->
                        val isSelected = selectedCurrency == symbol
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCurrency = symbol },
                            label = { 
                                Text(
                                    symbol, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Card Color Theme", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (hex, color) ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) LeafGreenDeep else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bal = balanceStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    onSave(name.trim(), bal, selectedCurrency, selectedColorHex)
                }
            }) { Text("Save Account", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun TransferFundsDialog(
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onTransfer: (from: Wallet, to: Wallet, amount: Double, note: String) -> Unit
) {
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(if (wallets.size > 1) 1 else 0) }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val selectedFromWallet = wallets.getOrNull(fromIndex)
    val selectedToWallet = wallets.getOrNull(toIndex)
    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val hasInsufficientBalance = selectedFromWallet != null && enteredAmount > selectedFromWallet.balance

    val textFieldColors = standardDialogTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.primary)
                Text("Transfer Funds", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("From Account", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEachIndexed { index, w ->
                        FilterChip(
                            selected = fromIndex == index,
                            onClick = {
                                fromIndex = index
                                if (wallets.size > 1 && toIndex == index) {
                                    toIndex = (index + 1) % wallets.size
                                }
                            },
                            label = {
                                Text(
                                    text = "${w.name} (${w.currency})",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (selectedFromWallet != null) {
                    Text(
                        text = "Available: ${selectedFromWallet.currency} ${String.format(Locale.getDefault(), "%.2f", selectedFromWallet.balance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "to", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }

                Text("To Account", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEachIndexed { index, w ->
                        if (index != fromIndex) {
                            FilterChip(
                                selected = toIndex == index,
                                onClick = { toIndex = index },
                                label = {
                                    Text(
                                        text = "${w.name} (${w.currency})",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 13.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Transfer Amount (${selectedFromWallet?.currency ?: "₹"})") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    isError = hasInsufficientBalance,
                    colors = textFieldColors
                )

                if (hasInsufficientBalance) {
                    Text(
                        text = "⚠️ Amount exceeds available balance (${selectedFromWallet?.currency ?: "₹"} ${String.format(Locale.getDefault(), "%.2f", selectedFromWallet?.balance ?: 0.0)})",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reference / Note (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredAmount > 0 && fromIndex != toIndex && !hasInsufficientBalance && selectedFromWallet != null && selectedToWallet != null) {
                        onTransfer(selectedFromWallet, selectedToWallet, enteredAmount, note.trim())
                    }
                },
                enabled = enteredAmount > 0 && fromIndex != toIndex && !hasInsufficientBalance,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Confirm Transfer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Cancel")
            }
        }
    )
}
