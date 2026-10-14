package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import com.camuel.sanchay.Debt
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtTrackerScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val debts by viewModel.allDebts.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Debt & Loans", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Filled.Add, "Add Debt") }
        }
    ) { padding ->
        if (debts.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No debts or loans tracked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(debts, key = { it.clientUuid.ifEmpty { it.id.toString() } }) { debt ->
                    DebtItem(
                        debt = debt,
                        currency = currency,
                        onSettle = { viewModel.updateDebt(debt.copy(isSettled = true)) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var person by remember { mutableStateOf("") }
        var amt by remember { mutableStateOf("") }
        var isLent by remember { mutableStateOf(true) }
        val textFieldColors = standardDialogTextFieldColors()

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("New Debt/Loan", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = person, 
                        onValueChange = { person = it }, 
                        label = { Text("Person Name") },
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
                        value = amt, 
                        onValueChange = { amt = it }, 
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isLent, 
                            onClick = { isLent = true },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Lent", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = !isLent, 
                            onClick = { isLent = false },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Borrowed", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amt.toDoubleOrNull() ?: 0.0
                    if (person.isNotBlank() && amount > 0) {
                        viewModel.insertDebt(Debt(personName = person.trim(), amount = amount, type = if (isLent) "Lent" else "Borrowed", dueDate = "TBD"))
                        showAddDialog = false
                    }
                }) { Text("Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }
}

@Composable
fun DebtItem(debt: Debt, currency: String, onSettle: () -> Unit) {
    val typeColor = if (debt.type == "Lent") LeafGreenPrimary else EnterpriseRose
    NeonCard(glowColor = typeColor) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text(debt.personName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = if (debt.type == "Lent") "Lent (Receivable)" else "Borrowed (Payable)",
                    color = typeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$currency${String.format(Locale.US, "%,.2f", debt.amount)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (!debt.isSettled) {
                    TextButton(onClick = onSettle) { Text("Settle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                } else {
                    Text("Settled", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
