package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.Investment
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.theme.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(viewModel: ExpenseViewModel, onBack: () -> Unit) {
    val investments by viewModel.allInvestments.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Investment Tracker", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Investment")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val totalCurrentValue = investments.sumOf { it.quantity * it.currentPrice }
            val totalInvested = investments.sumOf { it.quantity * it.averageBuyPrice }
            val totalProfit = totalCurrentValue - totalInvested
            val profitPercentage = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0

            NeonCard {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Portfolio Value", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$currency${String.format(Locale.US, "%,.2f", totalCurrentValue)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val profitColor = if (totalProfit >= 0) LeafGreenPrimary else EnterpriseRose
                    val profitPrefix = if (totalProfit >= 0) "+" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (totalProfit >= 0) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = profitColor, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$profitPrefix$currency${String.format(Locale.US, "%,.2f", totalProfit)} ($profitPrefix${String.format(Locale.US, "%.2f", profitPercentage)}%)",
                            color = profitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (investments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No investments added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(investments, key = { it.id }) { investment ->
                        InvestmentItem(investment, currency, onDelete = { viewModel.deleteInvestment(it) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var symbol by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var buyPrice by remember { mutableStateOf("") }
        var currentPrice by remember { mutableStateOf("") }
        val textFieldColors = standardDialogTextFieldColors()

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Add Investment", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = symbol, 
                        onValueChange = { symbol = it.uppercase() }, 
                        label = { Text("Symbol (e.g. AAPL)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text, 
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Name (e.g. Apple Inc.)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = quantity, 
                        onValueChange = { quantity = it }, 
                        label = { Text("Quantity") }, 
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = buyPrice, 
                        onValueChange = { buyPrice = it }, 
                        label = { Text("Average Buy Price") }, 
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = currentPrice, 
                        onValueChange = { currentPrice = it }, 
                        label = { Text("Current Price") }, 
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val q = quantity.toDoubleOrNull() ?: 0.0
                    val bp = buyPrice.toDoubleOrNull() ?: 0.0
                    val cp = currentPrice.toDoubleOrNull() ?: 0.0

                    if (symbol.isNotBlank() && q > 0 && bp > 0) {
                        viewModel.insertInvestment(
                            Investment(
                                symbol = symbol.trim(),
                                name = name.trim().ifBlank { symbol.trim() },
                                quantity = q,
                                averageBuyPrice = bp,
                                currentPrice = if (cp > 0) cp else bp
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }
}

@Composable
fun InvestmentItem(investment: Investment, currency: String, onDelete: (Investment) -> Unit) {
    val totalInvested = investment.quantity * investment.averageBuyPrice
    val currentValue = investment.quantity * investment.currentPrice
    val profit = currentValue - totalInvested
    val profitPercentage = if (totalInvested > 0) (profit / totalInvested) * 100 else 0.0
    val profitColor = if (profit >= 0) LeafGreenPrimary else EnterpriseRose

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(investment.symbol, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(investment.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Qty: ${investment.quantity} @ $currency${investment.averageBuyPrice}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$currency${String.format(Locale.US, "%.2f", currentValue)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (profit >= 0) "+" else ""}${String.format(Locale.US, "%.2f", profitPercentage)}%",
                    color = profitColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onDelete(investment) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = EnterpriseRose)
            }
        }
    }
}
