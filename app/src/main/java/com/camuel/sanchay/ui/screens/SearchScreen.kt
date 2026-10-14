package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ui.components.ExpenseItem
import com.camuel.sanchay.ui.theme.*
import com.camuel.sanchay.utils.NLPEngine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val allExpenses by viewModel.allExpenses.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()

    var quickCommand by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Live filter: blank query → show all, otherwise filter by category / note / tag / amount / date / payment
    val filteredExpenses = remember(query, allExpenses) {
        if (query.isBlank()) {
            allExpenses
        } else {
            allExpenses.filter { exp ->
                exp.category.contains(query, ignoreCase = true) ||
                exp.note.contains(query, ignoreCase = true) ||
                exp.amount.contains(query, ignoreCase = true) ||
                exp.paymentMethod.contains(query, ignoreCase = true) ||
                exp.date.contains(query, ignoreCase = true) ||
                exp.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Search & Quick Add", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ── Search bar ────────────────────────────────────────────
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search category, note, amount, #tag…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Quick-add bar ─────────────────────────────────────────
            OutlinedTextField(
                value = quickCommand,
                onValueChange = { quickCommand = it },
                placeholder = { Text("Quick add: 'Coffee 5' or 'Taxi 200'", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        val cmd = quickCommand.trim()
                        if (cmd.isNotBlank()) {
                            val parsed = NLPEngine.parseCommand(cmd)
                            if (parsed.amount != null) {
                                val category = parsed.category ?: "Other"
                                viewModel.insert(
                                    Expense(
                                        amount = parsed.amount,
                                        category = category,
                                        date = today,
                                        paymentMethod = "Cash",
                                        note = parsed.note ?: ""
                                    )
                                )
                                quickCommand = ""
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✅ Added $category — $currency${parsed.amount}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "⚠️ Try: 'Coffee 5' or 'Lunch 120'",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(Modifier.height(20.dp))

            // ── Header row ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (query.isBlank()) "All Transactions" else "Results for \"$query\"",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${filteredExpenses.size} entries",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Expense list ──────────────────────────────────────────
            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank()) "No transactions yet." else "No match for \"$query\".",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredExpenses, key = { it.clientUuid.ifEmpty { it.id.toString() } }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            currencySymbol = currency,
                            onDelete = { viewModel.delete(it) },
                            onUpdate = { viewModel.update(it) }
                        )
                    }
                }
            }
        }
    }
}
