package com.camuel.sanchay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.components.NeonButton
import com.camuel.sanchay.ui.components.AnimatedAmountCounter
import com.camuel.sanchay.ui.theme.*
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    var totalAmount by remember { mutableStateOf("") }
    var numPeople by remember { mutableStateOf("2") }
    var note by remember { mutableStateOf("Group split") }
    val currency by viewModel.currencySymbol.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Split Bill", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionHeader(Icons.Filled.Groups, "Group Details")
            
            val textFieldColors = standardDialogTextFieldColors()

            NeonCard {
                Column(Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = totalAmount, 
                        onValueChange = { totalAmount = it }, 
                        label = { Text("Total Bill Amount") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = numPeople, 
                        onValueChange = { numPeople = it }, 
                        label = { Text("Number of People") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                }
            }

            val total = totalAmount.toDoubleOrNull() ?: 0.0
            val people = numPeople.toIntOrNull() ?: 1
            val perPerson = if (people > 0) total / people else 0.0

            NeonCard(glowColor = MaterialTheme.colorScheme.primary) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Each Person Pays", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    AnimatedAmountCounter(
                        amount = perPerson,
                        currencySymbol = currency,
                        isStealthMode = false,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            NeonButton(onClick = {
                if (perPerson > 0) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    viewModel.insert(
                        Expense(
                            amount = String.format(Locale.US, "%.2f", perPerson),
                            category = "Food", // Default for splits usually
                            date = today,
                            paymentMethod = "Card",
                            note = "Split: $note (Total: $currency$totalAmount for $numPeople)"
                        )
                    )
                    onBack()
                }
            }) {
                Text("Post My Share as Expense", fontWeight = FontWeight.Bold)
            }
        }
    }
}
