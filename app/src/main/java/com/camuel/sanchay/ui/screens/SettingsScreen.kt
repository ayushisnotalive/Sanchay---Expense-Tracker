package com.camuel.sanchay.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.core.graphics.toColorInt
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ReportGenerator
import com.camuel.sanchay.ui.theme.*
import com.camuel.sanchay.ui.components.NeonCard
import com.camuel.sanchay.ui.components.NeonButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onSplitBillClick: () -> Unit = {},
    onDebtTrackerClick: () -> Unit = {},
    onSavingsGoalsClick: () -> Unit = {},
    onSubscriptionsClick: () -> Unit = {},
    onInvestmentsClick: () -> Unit = {},
    onTermsClick: () -> Unit = {}
) {
    val budget by viewModel.monthlyBudget.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val isTravelModeEnabled by viewModel.isTravelModeEnabled.collectAsState()
    val isDailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsState()
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    
    val userEmail by viewModel.userEmail.collectAsState()
    val isOneTimeLoginCompleted by viewModel.isOneTimeLoginCompleted.collectAsState()
    val accountStatus by viewModel.accountStatus.collectAsState()
    val scheduledDeletionDate by viewModel.scheduledDeletionDate.collectAsState()
    val deactivatedUntilDate by viewModel.deactivatedUntilDate.collectAsState()

    val selectedThemeColor = remember(accentColorHex) { Color(accentColorHex.toColorInt()) }
    
    val context = LocalContext.current

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInputText by remember { mutableStateOf("") }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editEmailInput by remember { mutableStateOf("") }
    var editNameInput by remember { mutableStateOf("") }

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var selectedDeactivateDuration by remember { mutableStateOf("3_DAYS") }
    var deactivateConfirmText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    // Local device image picker (Stores avatar permanently in app internal storage)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.saveCustomProfilePhoto(context, it)
                Toast.makeText(context, "Avatar saved to device storage", Toast.LENGTH_SHORT).show()
            }
        }
    )



    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.restoreFullBackup(context, it) { success, msg ->
                    Toast.makeText(context, if (success) "✅ $msg" else "❌ $msg", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
    
    val themeColors = listOf(LeafGreenPrimary, LeafGreenDeep, LeafGreenMedium, LeafGreenLight, SageGreen, OliveGreen)
    val currencies = listOf("₹", "$", "€", "£", "¥", "A$", "C$", "AED")

    val syncState by viewModel.syncState.collectAsState()
    val serverBaseUrl by viewModel.serverBaseUrl.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()
    var showServerDialog by remember { mutableStateOf(false) }
    var serverInputText by remember { mutableStateOf("") }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInputText by remember { mutableStateOf("") }

    val dialogTextFieldColors = standardDialogTextFieldColors()

    LaunchedEffect(showEditProfileDialog) {
        if (showEditProfileDialog) {
            editNameInput = userName
            editEmailInput = userEmail
        }
    }

    LaunchedEffect(showNameDialog) {
        if (showNameDialog) {
            nameInputText = userName
        }
    }

    LaunchedEffect(showBudgetDialog) {
        if (showBudgetDialog) {
            budgetInputText = if (budget > 0) budget.toInt().toString() else ""
        }
    }

    LaunchedEffect(showServerDialog) {
        if (showServerDialog) {
            serverInputText = serverBaseUrl
        }
    }

    // --- Edit Account Profile Dialog ---
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountCircle, null, tint = selectedThemeColor)
                    Text("Account Profile & Cloud Identity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Updates your cloud ledger identity. Avatar remains stored locally.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        singleLine = true,
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Alex Hunter") },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        colors = dialogTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmailInput,
                        onValueChange = { editEmailInput = it },
                        singleLine = true,
                        label = { Text("Email Address (Unique)") },
                        placeholder = { Text("e.g. alex@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(14.dp),
                        colors = dialogTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val email = editEmailInput.trim()
                        val name = editNameInput.trim()
                        if (email.isNotBlank() && email.contains("@") && email.contains(".")) {
                            viewModel.updateAccountProfile(
                                name = name.ifBlank { userName },
                                email = email
                            )
                            Toast.makeText(context, "Account profile updated successfully", Toast.LENGTH_SHORT).show()
                            showEditProfileDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save & Sync", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // --- Deactivation Dialog ---
    if (showDeactivateDialog) {
        val isDeactivateConfirmed = deactivateConfirmText.trim().equals("deactivate", ignoreCase = false)
        AlertDialog(
            onDismissRequest = {
                showDeactivateDialog = false
                deactivateConfirmText = ""
            },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PauseCircle, null, tint = CashflowWarning)
                    Text("Pause / Deactivate Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Pauses cloud sync and transactions. Select duration:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Duration Radio Options
                    listOf(
                        "3_DAYS" to "3 Days",
                        "7_DAYS" to "7 Days",
                        "UNTIL_MANUAL" to "Until I turn on"
                    ).forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDeactivateDuration = code }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDeactivateDuration == code,
                                onClick = { selectedDeactivateDuration = code },
                                colors = RadioButtonDefaults.colors(selectedColor = selectedThemeColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "To confirm deactivation, type \"deactivate\" below:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CashflowWarning
                    )

                    OutlinedTextField(
                        value = deactivateConfirmText,
                        onValueChange = { deactivateConfirmText = it },
                        singleLine = true,
                        label = { Text("Type 'deactivate'") },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = dialogTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = isDeactivateConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = CashflowWarning),
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        viewModel.deactivateAccount(selectedDeactivateDuration)
                        Toast.makeText(context, "Account deactivated successfully", Toast.LENGTH_SHORT).show()
                        showDeactivateDialog = false
                        deactivateConfirmText = ""
                    }
                ) {
                    Text("Confirm Deactivation", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    deactivateConfirmText = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // --- Deletion Dialog ---
    if (showDeleteDialog) {
        val isDeleteConfirmed = deleteConfirmText.trim().equals("delete", ignoreCase = false)
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteConfirmText = ""
            },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteForever, null, tint = CashflowExpense)
                    Text("Schedule Account Deletion", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Scheduled for permanent removal after 15 days. You can cancel or restore anytime before this period ends.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "To confirm deletion, type \"delete\" below:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CashflowExpense
                    )

                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        singleLine = true,
                        label = { Text("Type 'delete'") },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = dialogTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = isDeleteConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = CashflowExpense),
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        viewModel.scheduleAccountDeletion()
                        Toast.makeText(context, "Account scheduled for deletion in 15 days", Toast.LENGTH_LONG).show()
                        showDeleteDialog = false
                        deleteConfirmText = ""
                    }
                ) {
                    Text("Confirm 15-Day Deletion", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteConfirmText = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Configure Sync Server", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter backend base URL (Neon connected):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = serverInputText,
                        onValueChange = { serverInputText = it },
                        singleLine = true,
                        label = { Text("Server URL") },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (serverInputText.isNotBlank()) {
                        viewModel.updateServerBaseUrl(serverInputText.trim())
                    }
                    showServerDialog = false
                }) { Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = budgetInputText,
                        onValueChange = { budgetInputText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        label = { Text("Amount") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetInputText.toFloatOrNull()?.let { viewModel.setMonthlyBudget(it) }
                    showBudgetDialog = false
                }) { Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Edit Display Name", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInputText,
                        onValueChange = { nameInputText = it },
                        label = { Text("Enter your name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameInputText.isNotBlank()) {
                        viewModel.updateUserName(nameInputText.trim())
                    }
                    showNameDialog = false
                }) { Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- User Profile Section (Local Sovereignty) ---
            SectionHeader(Icons.Filled.AccountCircle, "User Profile & Identity")
            NeonCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, selectedThemeColor, CircleShape)
                                    .clickable { 
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePhotoUri.isNotEmpty()) {
                                    val imageModel = remember(profilePhotoUri) {
                                        if (profilePhotoUri.startsWith("/")) java.io.File(profilePhotoUri) else profilePhotoUri
                                    }
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }

                            // Overlay edit icon - fully unclipped outside circle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(selectedThemeColor)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit, 
                                    contentDescription = "Change photo", 
                                    tint = Color.White, 
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName.ifBlank { "Sanchay User" },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        nameInputText = userName
                                        showNameDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Edit Name", tint = selectedThemeColor, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "Local profile details",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Notice: Custom icon remains on device storage only
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Storage, null, tint = selectedThemeColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Stored privately on this device",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Account Profile & Neon Cloud Identity ---
            SectionHeader(Icons.Filled.AccountCircle, "Account Profile & Cloud Identity")
            NeonCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Registered Email", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            if (userEmail.isNotBlank()) {
                                Text(userEmail, fontSize = 13.sp, color = selectedThemeColor, fontWeight = FontWeight.Medium)
                            } else {
                                Text("No email configured", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (userEmail.isNotBlank()) {
                            Surface(
                                color = selectedThemeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Unique Identity",
                                    color = selectedThemeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Private email identity",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeonButton(
                            onClick = {
                                editNameInput = userName
                                editEmailInput = userEmail
                                showEditProfileDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                Text("Edit Account Info", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        if (userEmail.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.signOutAccount()
                                    Toast.makeText(context, "Account unlinked", Toast.LENGTH_SHORT).show()
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text("Sign Out", fontSize = 12.sp, color = CashflowExpense)
                            }
                        }
                    }
                }
            }

            // --- Account Lifecycle: Deactivation & Deletion ---
            SectionHeader(Icons.Filled.Warning, "Account Deactivation & Deletion")
            NeonCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Account Status", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        val (statusText, statusColor) = when (accountStatus) {
                            "DEACTIVATED_3_DAYS" -> "Paused (3 Days) until $deactivatedUntilDate" to CashflowWarning
                            "DEACTIVATED_7_DAYS" -> "Paused (7 Days) until $deactivatedUntilDate" to CashflowWarning
                            "DEACTIVATED_UNTIL_MANUAL_REACTIVATION" -> "Paused (Until turned on)" to CashflowWarning
                            "PENDING_DELETION" -> "Pending Deletion (15d) until $scheduledDeletionDate" to CashflowExpense
                            else -> "Active" to selectedThemeColor
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                statusText,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // If not active, show Reactivate Button
                    if (accountStatus != "ACTIVE") {
                        Button(
                            onClick = {
                                viewModel.reactivateAccount()
                                Toast.makeText(context, "Account reactivated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedThemeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Refresh, null, tint = Color.White)
                                Text("Reactivate Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Pause / Deactivate Button
                    OutlinedButton(
                        onClick = { showDeactivateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CashflowWarning)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.PauseCircle, null, tint = CashflowWarning, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Pause Account",
                                color = CashflowWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Delete Account Button (15 days range)
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CashflowExpense)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.DeleteForever, null, tint = CashflowExpense, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Delete Account (15d Grace)",
                                color = CashflowExpense,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // --- Cloud Synchronization (Kept Strictly in Settings) ---
            SectionHeader(Icons.Filled.CloudDownload, "Cloud Synchronization")
            NeonCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Backend Server Endpoint (Neon DB)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(serverBaseUrl, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = {
                            serverInputText = serverBaseUrl
                            showServerDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, "Edit Server URL", tint = selectedThemeColor)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        com.camuel.sanchay.ui.components.SyncStatusBadge(
                            syncState = syncState,
                            onSyncClick = { viewModel.triggerSync() }
                        )
                        TextButton(onClick = { viewModel.triggerSync() }) {
                            Text("Sync Now", color = selectedThemeColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- Preferences Section ---
            SectionHeader(Icons.Filled.AccountBalanceWallet, "Preferences")
            NeonCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Monthly Budget", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$currency${budget.toInt()}", 
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = selectedThemeColor,
                            modifier = Modifier.clickable { 
                                budgetInputText = budget.toInt().toString()
                                showBudgetDialog = true 
                            }
                        )
                        IconButton(onClick = { 
                            budgetInputText = budget.toInt().toString()
                            showBudgetDialog = true 
                        }) {
                            Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = budget,
                        onValueChange = { viewModel.setMonthlyBudget(it) },
                        valueRange = 100f..10000f,
                        colors = SliderDefaults.colors(thumbColor = selectedThemeColor, activeTrackColor = selectedThemeColor)
                    )
                }
            }

            NeonCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Travel Mode (Auto-Currency)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Switch currency based on location", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isTravelModeEnabled,
                        onCheckedChange = { viewModel.setTravelModeEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = selectedThemeColor)
                    )
                }
            }
            
            NeonCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Currency Symbol", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text(currency, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currencies.take(4).forEach { sym ->
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (sym == currency) selectedThemeColor.copy(0.15f) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (sym == currency) selectedThemeColor else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setCurrency(sym) },
                                contentAlignment = Alignment.Center
                            ) { Text(sym, color = if (sym == currency) selectedThemeColor else MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }

            // --- Security & Privacy ---
            SectionHeader(Icons.Filled.Lock, "Security & Privacy")
            NeonCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric App Lock", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Require fingerprint or face recognition on startup", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isBiometricLockEnabled,
                        onCheckedChange = { viewModel.setBiometricLockEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = selectedThemeColor)
                    )
                }
            }

            // --- Appearance ---
            SectionHeader(Icons.Filled.Palette, "Appearance")
            NeonCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Daily Reminder", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isDailyReminderEnabled,
                            onCheckedChange = { viewModel.setDailyReminderEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = selectedThemeColor)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        themeColors.forEach { color ->
                            val hex = "#%06X".format(0xFFFFFF and color.toArgb())
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).background(color.copy(0.2f))
                                    .border(if (color == selectedThemeColor) 3.dp else 1.dp, color, CircleShape)
                                    .clickable { viewModel.setAccentColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (color == selectedThemeColor) Icon(Icons.Filled.Check, null, tint = color)
                            }
                        }
                    }
                }
            }

            // --- Data Management & System Backup ---
            SectionHeader(Icons.Filled.CloudDownload, "Data Management & System Backup")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.exportFullBackup(context) { success, msg ->
                            if (!success) {
                                Toast.makeText(context, "❌ Export failed: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Create Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                NeonButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        restoreBackupLauncher.launch("application/json")
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Restore Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // --- Legal & Compliance ---
            SectionHeader(Icons.Filled.Lock, "Legal & Compliance")
            NeonCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Private financial ledger with zero tracking.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    NeonButton(onClick = onTermsClick) {
                        Text("Terms of Service & Privacy Policy", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}
