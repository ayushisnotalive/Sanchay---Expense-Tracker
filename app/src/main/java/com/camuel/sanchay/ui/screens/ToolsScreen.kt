package com.camuel.sanchay.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ReportGenerator
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.components.bouncyClickable
import com.camuel.sanchay.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: ExpenseViewModel,
    onSpendingMapClick: () -> Unit,
    onSavingsGoalsClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onInvestmentsClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onDebtTrackerClick: () -> Unit,
    onAIAssistantClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onSpendingMapClick()
        } else {
            Toast.makeText(context, "Location permission is required to view your spending heatmap.", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleHeatmapClick() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            onSpendingMapClick()
        } else {
            showLocationPermissionDialog = true
        }
    }

    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = LeafGreenPrimary)
                    Text("Location Required for Heatmap", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Sanchay's Spending Heatmap maps your local transactions to identify where you spend the most. To view your spending clusters, please enable location access.\n\nYour location data is processed 100% privately on your device and is never sent to any cloud server.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreenPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Enable Location", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLocationPermissionDialog = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
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
                            text = "Financial Tools",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Trackers, heatmaps, calculators & intelligence",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            val hasLocationPermission = remember {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }

            // 1. Featured: Spending Heatmap
            ToolCard(
                title = "Spending Heatmap",
                subtitle = "Geographical transaction heatmap & spending cluster density",
                icon = Icons.Default.Public,
                badge = if (hasLocationPermission) "Active" else "GPS Required",
                accentColor = LeafGreenPrimary,
                onClick = { handleHeatmapClick() }
            )

            // 2. Subscriptions & Bills
            ToolCard(
                title = "Subscriptions Tracker",
                subtitle = "Manage recurring billing cycles, renewals & automatic alerts",
                icon = Icons.Default.Subscriptions,
                accentColor = Color(0xFF6366F1),
                onClick = onSubscriptionsClick
            )

            // 3. Investment Portfolio
            ToolCard(
                title = "Investment Portfolio",
                subtitle = "Track equities, mutual funds, gold & crypto holdings in real-time",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = CashflowIncome,
                onClick = onInvestmentsClick
            )

            // 4. Split Bill Calculator
            ToolCard(
                title = "Split Bill Calculator",
                subtitle = "Fair multi-person bill splitting, tip calculations & individual dues",
                icon = Icons.AutoMirrored.Filled.CallSplit,
                accentColor = Color(0xFFF59E0B),
                onClick = onSplitBillClick
            )

            // 5. Debt & Loan Tracker
            ToolCard(
                title = "Debt & Loan Tracker",
                subtitle = "Record borrowed & lent money, repayment schedules & interest",
                icon = Icons.Default.CreditCard,
                accentColor = Color(0xFFEC4899),
                onClick = onDebtTrackerClick
            )

            // 6. Savings Goals
            ToolCard(
                title = "Savings Goals",
                subtitle = "Set target savings milestones with dedicated visual meters",
                icon = Icons.Default.Savings,
                accentColor = Color(0xFF14B8A6),
                onClick = onSavingsGoalsClick
            )

            // 7. AI Financial Advisor
            ToolCard(
                title = "AI Financial Advisor",
                subtitle = "Intelligent on-device simulations, budget coaching & insights",
                icon = Icons.Default.AutoAwesome,
                accentColor = LeafGreenDeep,
                onClick = onAIAssistantClick
            )

            // Section: Statement & Report Generators
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = LeafGreenPrimary, modifier = Modifier.size(20.dp))
                Text(
                    text = "Statement & Report Generators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 8. PDF Statement Generator
            val allExpenses by viewModel.allExpenses.collectAsState()
            ToolCard(
                title = "PDF Statement Generator",
                subtitle = "Generate executive PDF financial statements with audit-ready summaries",
                icon = Icons.Default.PictureAsPdf,
                badge = "PDF",
                accentColor = Color(0xFFEF4444),
                onClick = {
                    if (allExpenses.isEmpty()) {
                        Toast.makeText(context, "No transactions to export yet. Add expenses first!", Toast.LENGTH_SHORT).show()
                        return@ToolCard
                    }
                    Toast.makeText(context, "Generating PDF statement...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            ReportGenerator.exportToPDF(context, allExpenses)
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )

            // 9. Excel (XLSX) Spreadsheet Generator
            ToolCard(
                title = "Excel (XLSX) Spreadsheet",
                subtitle = "Generate multi-tab formatted Excel workbooks with categorization & formulas",
                icon = Icons.Default.TableChart,
                badge = "XLSX",
                accentColor = Color(0xFF10B981),
                onClick = {
                    if (allExpenses.isEmpty()) {
                        Toast.makeText(context, "No transactions to export yet. Add expenses first!", Toast.LENGTH_SHORT).show()
                        return@ToolCard
                    }
                    Toast.makeText(context, "Generating Excel spreadsheet...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            ReportGenerator.exportToExcel(context, allExpenses)
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )

            // 10. CSV Spreadsheet Export
            ToolCard(
                title = "CSV Spreadsheet Export",
                subtitle = "Standard CSV export compatible with any spreadsheet program",
                icon = Icons.Default.Description,
                badge = "CSV",
                accentColor = Color(0xFF3B82F6),
                onClick = {
                    if (allExpenses.isEmpty()) {
                        Toast.makeText(context, "No transactions to export yet. Add expenses first!", Toast.LENGTH_SHORT).show()
                        return@ToolCard
                    }
                    Toast.makeText(context, "Exporting CSV...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            ReportGenerator.exportToCSV(context, allExpenses)
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = accentColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
