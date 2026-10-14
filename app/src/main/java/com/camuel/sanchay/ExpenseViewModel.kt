package com.camuel.sanchay

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val prefs = application.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)

    val allExpenses: StateFlow<List<Expense>>
    val totalExpenses: StateFlow<Double?>
    val totalIncome: StateFlow<Double?>
    val netCashFlow: StateFlow<Double>

    val allSubscriptions: StateFlow<List<Subscription>>
    val allSavingsGoals: StateFlow<List<SavingsGoal>>
    val allWallets: StateFlow<List<Wallet>>
    val allDebts: StateFlow<List<Debt>>
    val allInvestments: StateFlow<List<Investment>>
    val allCategories: StateFlow<List<Category>>
    val allCategoryBudgets: StateFlow<List<CategoryBudget>>

    val expensesByCategory: StateFlow<Map<String, Float>>
    val incomeByCategory: StateFlow<Map<String, Float>>
    val expensesByDate: StateFlow<Map<String, Float>>

    private val _insights = MutableStateFlow<List<String>>(emptyList())
    val insights: StateFlow<List<String>> = _insights.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency", "₹") ?: "₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 25000f))
    val monthlyBudget: StateFlow<Float> = _monthlyBudget.asStateFlow()

    private val _isTravelModeEnabled = MutableStateFlow(prefs.getBoolean("travel_mode", false))
    val isTravelModeEnabled: StateFlow<Boolean> = _isTravelModeEnabled.asStateFlow()

    private val _dailyReminderEnabled = MutableStateFlow(prefs.getBoolean("daily_reminder", false))
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled.asStateFlow()

    private val _accentColorHex = MutableStateFlow(prefs.getString("accent_color", "#2E7D32") ?: "#2E7D32")
    val accentColorHex: StateFlow<String> = _accentColorHex.asStateFlow()

    private val _isStealthMode = MutableStateFlow(prefs.getBoolean("stealth_mode", false))
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Navigator") ?: "Navigator")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow(prefs.getString("profile_photo", "") ?: "")
    val profilePhotoUri: StateFlow<String> = _profilePhotoUri.asStateFlow()

    val syncManager = com.camuel.sanchay.services.SyncManager.getInstance(application)
    val syncState: StateFlow<com.camuel.sanchay.services.SyncState> = syncManager.syncState
    val lastSyncTimestamp: StateFlow<Long> = syncManager.lastSyncTimestamp

    private val _serverBaseUrl = MutableStateFlow(prefs.getString("server_base_url", "http://10.0.2.2:8080/") ?: "http://10.0.2.2:8080/")
    val serverBaseUrl: StateFlow<String> = _serverBaseUrl.asStateFlow()

    private val _isBiometricLockEnabled = MutableStateFlow(prefs.getBoolean("biometric_lock_enabled", false))
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    // --- Live Bank Account & Plaid API Integration ---
    val bankSyncService = com.camuel.sanchay.services.BankSyncService()

    private val _plaidClientId = MutableStateFlow(prefs.getString("plaid_client_id", "") ?: "")
    val plaidClientId: StateFlow<String> = _plaidClientId.asStateFlow()

    private val _plaidSecret = MutableStateFlow(prefs.getString("plaid_secret", "") ?: "")
    val plaidSecret: StateFlow<String> = _plaidSecret.asStateFlow()

    private val _plaidEnv = MutableStateFlow(prefs.getString("plaid_env", "sandbox") ?: "sandbox")
    val plaidEnv: StateFlow<String> = _plaidEnv.asStateFlow()

    private val _plaidAccessToken = MutableStateFlow(prefs.getString("plaid_access_token", "") ?: "")
    val plaidAccessToken: StateFlow<String> = _plaidAccessToken.asStateFlow()

    private val _connectedBankAccounts = MutableStateFlow<List<com.camuel.sanchay.services.BankAccount>>(emptyList())
    val connectedBankAccounts: StateFlow<List<com.camuel.sanchay.services.BankAccount>> = _connectedBankAccounts.asStateFlow()

    private val _isBankSyncing = MutableStateFlow(false)
    val isBankSyncing: StateFlow<Boolean> = _isBankSyncing.asStateFlow()

    private val _lastBankSyncTime = MutableStateFlow(prefs.getString("last_bank_sync_time", "Never") ?: "Never")
    val lastBankSyncTime: StateFlow<String> = _lastBankSyncTime.asStateFlow()

    private val _hasAcceptedTerms = MutableStateFlow(prefs.getBoolean("has_accepted_terms", false))
    val hasAcceptedTerms: StateFlow<Boolean> = _hasAcceptedTerms.asStateFlow()

    fun acceptTerms() {
        prefs.edit().putBoolean("has_accepted_terms", true).apply()
        _hasAcceptedTerms.value = true
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_lock_enabled", enabled).apply()
        _isBiometricLockEnabled.value = enabled
    }

    fun toggleStealthMode() {
        val next = !_isStealthMode.value
        prefs.edit().putBoolean("stealth_mode", next).apply()
        _isStealthMode.value = next
    }

    fun updateServerBaseUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString("server_base_url", cleanUrl).apply()
        _serverBaseUrl.value = cleanUrl
        com.camuel.sanchay.services.SanchayApiClient.updateBaseUrl(cleanUrl)
    }

    fun triggerSync() {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch {
            syncManager.performSync()
        }
    }

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", prefs.getString("google_user_email", "") ?: "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()
    val googleUserEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isOneTimeLoginCompleted = MutableStateFlow(prefs.getBoolean("is_one_time_login_completed", false))
    val isOneTimeLoginCompleted: StateFlow<Boolean> = _isOneTimeLoginCompleted.asStateFlow()

    private val _accountStatus = MutableStateFlow(prefs.getString("account_status", "ACTIVE") ?: "ACTIVE")
    val accountStatus: StateFlow<String> = _accountStatus.asStateFlow()

    private val _scheduledDeletionDate = MutableStateFlow(prefs.getString("scheduled_deletion_date", null))
    val scheduledDeletionDate: StateFlow<String?> = _scheduledDeletionDate.asStateFlow()

    private val _deactivatedUntilDate = MutableStateFlow(prefs.getString("deactivated_until_date", null))
    val deactivatedUntilDate: StateFlow<String?> = _deactivatedUntilDate.asStateFlow()

    fun registerUser(
        name: String,
        email: String,
        imageUri: android.net.Uri? = null,
        context: Context? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val cleanName = name.trim().ifBlank { "Sanchay User" }
        val cleanEmail = email.trim().lowercase(Locale.getDefault())

        if (imageUri != null && context != null) {
            try {
                val avatarFile = java.io.File(context.filesDir, "custom_user_avatar.jpg")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    java.io.FileOutputStream(avatarFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val localPath = avatarFile.absolutePath
                prefs.edit().putString("profile_photo", localPath).apply()
                _profilePhotoUri.value = localPath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        prefs.edit().apply {
            putString("user_name", cleanName)
            putString("user_email", cleanEmail)
            putString("google_user_email", cleanEmail)
            putBoolean("has_accepted_terms", true)
            putBoolean("is_one_time_login_completed", true)
            putString("account_status", "ACTIVE")
            remove("scheduled_deletion_date")
            remove("deactivated_until_date")
        }.apply()

        _userName.value = cleanName
        _userEmail.value = cleanEmail
        _hasAcceptedTerms.value = true
        _isOneTimeLoginCompleted.value = true
        _accountStatus.value = "ACTIVE"
        _scheduledDeletionDate.value = null
        _deactivatedUntilDate.value = null

        // Escape single quotes for SQL safety
        val safeEmail = cleanEmail.replace("'", "''")
        val safeName = cleanName.replace("'", "''")

        // 1. Direct Neon PostgreSQL over HTTPS (stores unique email and name)
        executeNeonSql("INSERT INTO users (id, email, name, status, \"isOneTimeLoginCompleted\", \"createdAt\", \"updatedAt\", \"lastLoginAt\") VALUES (gen_random_uuid(), '$safeEmail', '$safeName', 'ACTIVE', true, NOW(), NOW(), NOW()) ON CONFLICT (email) DO UPDATE SET name = '$safeName', \"lastLoginAt\" = NOW(), status = 'ACTIVE', \"scheduledDeletionAt\" = NULL, \"deactivatedUntil\" = NULL;")

        // 2. Also notify local backend if configured
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject().apply {
                    put("email", cleanEmail)
                    put("name", cleanName)
                }.toString()
                sendBackendPost("/api/auth/register", json)
            } catch (e: Exception) {}
        }

        onComplete?.invoke()
    }

    fun updateAccountProfile(name: String, email: String) {
        val cleanName = name.trim().ifBlank { "Sanchay User" }
        val cleanEmail = email.trim().lowercase(Locale.getDefault())

        prefs.edit().apply {
            putString("user_name", cleanName)
            putString("user_email", cleanEmail)
            putString("google_user_email", cleanEmail)
        }.apply()

        _userName.value = cleanName
        _userEmail.value = cleanEmail

        val safeEmail = cleanEmail.replace("'", "''")
        val safeName = cleanName.replace("'", "''")
        executeNeonSql("INSERT INTO users (id, email, name, status, \"isOneTimeLoginCompleted\", \"createdAt\", \"updatedAt\", \"lastLoginAt\") VALUES (gen_random_uuid(), '$safeEmail', '$safeName', 'ACTIVE', true, NOW(), NOW(), NOW()) ON CONFLICT (email) DO UPDATE SET name = '$safeName', \"lastLoginAt\" = NOW(), status = 'ACTIVE';")
    }

    fun recordGoogleLogin(email: String, displayName: String = "", photoUrl: String = "") {
        registerUser(
            name = displayName.ifBlank { "Sanchay User" },
            email = email
        )
    }

    fun signOutAccount() {
        prefs.edit().apply {
            remove("user_email")
            remove("google_user_email")
            putBoolean("is_one_time_login_completed", false)
            putBoolean("has_accepted_terms", false)
            putString("account_status", "ACTIVE")
            remove("scheduled_deletion_date")
            remove("deactivated_until_date")
        }.apply()
        _userEmail.value = ""
        _userName.value = "Sanchay User"
        _isOneTimeLoginCompleted.value = false
        _hasAcceptedTerms.value = false
        _accountStatus.value = "ACTIVE"
        _scheduledDeletionDate.value = null
        _deactivatedUntilDate.value = null
    }

    fun signOutGoogle() {
        signOutAccount()
    }

    fun checkAutoReactivation() {
        val status = _accountStatus.value
        if (status == "DEACTIVATED_3_DAYS" || status == "DEACTIVATED_7_DAYS") {
            val untilStr = _deactivatedUntilDate.value
            if (untilStr != null) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val untilDate = sdf.parse(untilStr)
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    if (untilDate != null && !today.before(untilDate)) {
                        reactivateAccount()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deactivateAccount(duration: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val untilStr: String? = when (duration) {
            "3_DAYS" -> {
                cal.add(Calendar.DAY_OF_YEAR, 3)
                sdf.format(cal.time)
            }
            "7_DAYS" -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
                sdf.format(cal.time)
            }
            else -> null
        }

        val status = when (duration) {
            "3_DAYS" -> "DEACTIVATED_3_DAYS"
            "7_DAYS" -> "DEACTIVATED_7_DAYS"
            else -> "DEACTIVATED_UNTIL_MANUAL_REACTIVATION"
        }

        prefs.edit().apply {
            putString("account_status", status)
            if (untilStr != null) putString("deactivated_until_date", untilStr) else remove("deactivated_until_date")
        }.apply()

        _accountStatus.value = status
        _deactivatedUntilDate.value = untilStr

        val cleanEmail = _userEmail.value.replace("'", "''").ifBlank { "user@sanchay.local" }
        when (duration) {
            "3_DAYS" -> executeNeonSql("UPDATE users SET status = 'DEACTIVATED_3_DAYS', \"deactivatedAt\" = NOW(), \"deactivatedUntil\" = NOW() + INTERVAL '3 days' WHERE email = '$cleanEmail';")
            "7_DAYS" -> executeNeonSql("UPDATE users SET status = 'DEACTIVATED_7_DAYS', \"deactivatedAt\" = NOW(), \"deactivatedUntil\" = NOW() + INTERVAL '7 days' WHERE email = '$cleanEmail';")
            else -> executeNeonSql("UPDATE users SET status = 'DEACTIVATED_UNTIL_MANUAL_REACTIVATION', \"deactivatedAt\" = NOW(), \"deactivatedUntil\" = NULL WHERE email = '$cleanEmail';")
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                sendBackendPost("/api/account/deactivate", "{\"email\":\"$cleanEmail\",\"confirmation\":\"deactivate\",\"duration\":\"$duration\"}")
            } catch (e: Exception) {}
        }
    }

    fun reactivateAccount() {
        prefs.edit().apply {
            putString("account_status", "ACTIVE")
            remove("scheduled_deletion_date")
            remove("deactivated_until_date")
        }.apply()

        _accountStatus.value = "ACTIVE"
        _scheduledDeletionDate.value = null
        _deactivatedUntilDate.value = null

        val cleanEmail = _userEmail.value.replace("'", "''").ifBlank { "user@sanchay.local" }
        executeNeonSql("UPDATE users SET status = 'ACTIVE', \"deactivatedAt\" = NULL, \"deactivatedUntil\" = NULL, \"scheduledDeletionAt\" = NULL WHERE email = '$cleanEmail';")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                sendBackendPost("/api/account/reactivate", "{\"email\":\"$cleanEmail\"}")
            } catch (e: Exception) {}
        }
    }

    fun scheduleAccountDeletion() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 15) // 15-day range
        val deletionDateStr = sdf.format(cal.time)

        prefs.edit().apply {
            putString("account_status", "PENDING_DELETION")
            putString("scheduled_deletion_date", deletionDateStr)
        }.apply()

        _accountStatus.value = "PENDING_DELETION"
        _scheduledDeletionDate.value = deletionDateStr

        val cleanEmail = _userEmail.value.replace("'", "''").ifBlank { "user@sanchay.local" }
        executeNeonSql("UPDATE users SET status = 'PENDING_DELETION', \"scheduledDeletionAt\" = NOW() + INTERVAL '15 days' WHERE email = '$cleanEmail';")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                sendBackendPost("/api/account/delete", "{\"email\":\"$cleanEmail\",\"confirmation\":\"delete\"}")
            } catch (e: Exception) {}
        }
    }

    private fun executeNeonSql(sql: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val neonUrl = com.camuel.sanchay.BuildConfig.NEON_SQL_URL
                val neonConn = com.camuel.sanchay.BuildConfig.NEON_DATABASE_URL
                if (neonUrl.isBlank() || neonConn.isBlank()) {
                    android.util.Log.d("NeonAuth", "Neon direct credentials not configured in local.properties; delegating to backend proxy")
                    return@launch
                }
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val jsonPayload = org.json.JSONObject().apply {
                    put("query", sql)
                }.toString()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = jsonPayload.toRequestBody(mediaType)
                val request = okhttp3.Request.Builder()
                    .url(neonUrl)
                    .addHeader("Neon-Connection-String", neonConn)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                android.util.Log.d("NeonAuth", "Neon SQL response code: ${response.code}")
                response.close()
            } catch (e: Exception) {
                android.util.Log.e("NeonAuth", "Neon SQL execution failed", e)
            }
        }
    }

    private fun sendBackendPost(path: String, jsonBody: String) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonBody.toRequestBody(mediaType)
            val url = _serverBaseUrl.value.trimEnd('/') + path
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {}
    }

    init {
        checkAutoReactivation()
        com.camuel.sanchay.services.SanchayApiClient.updateBaseUrl(_serverBaseUrl.value)
        val expenseDao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)

        allExpenses = repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalExpenses = repository.totalExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        totalIncome = repository.totalIncome.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        netCashFlow = kotlinx.coroutines.flow.combine(totalIncome, totalExpenses) { inc, exp ->
            (inc ?: 0.0) - (exp ?: 0.0)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
        
        allSubscriptions = repository.allSubscriptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSavingsGoals = repository.allSavingsGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allWallets = repository.allWallets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allDebts = repository.allDebts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allInvestments = repository.allInvestments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategoryBudgets = repository.allCategoryBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        expensesByCategory = allExpenses.map { expenses ->
            expenses.filter { it.transactionType == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toFloat()
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

        incomeByCategory = allExpenses.map { expenses ->
            expenses.filter { it.transactionType == "INCOME" }
                .groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toFloat()
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

        expensesByDate = allExpenses.map { expenses ->
            expenses.filter { it.transactionType == "EXPENSE" }
                .groupBy { it.date }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toFloat()
                }
                .toSortedMap()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

        viewModelScope.launch {
            allExpenses.collect { 
                updateInsights()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            ensureDefaultData()
        }
    }

    private suspend fun ensureDefaultData() {
        if (repository.getWalletCount() == 0) {
            repository.insertWallet(
                Wallet(
                    name = "Main Wallet",
                    balance = 0.0,
                    currency = _currencySymbol.value,
                    colorHex = "#10B981",
                    isDirty = false
                )
            )
        }
        if (repository.getCategoryCount() == 0) {
            val defaults = listOf(
                Pair("Food", "Fastfood"),
                Pair("Transport", "Flight"),
                Pair("Shopping", "ShoppingBag"),
                Pair("Bills", "Receipt"),
                Pair("Entertainment", "Movie"),
                Pair("Health", "LocalHospital"),
                Pair("Education", "School"),
                Pair("Investment", "TrendingUp"),
                Pair("Other", "MoreHoriz")
            )
            defaults.forEach { (catName, icon) ->
                repository.insertCategory(Category(name = catName, iconName = icon, isCustom = false))
            }
        }
    }

    private fun updateInsights() {
        val total = totalExpenses.value ?: 0.0
        val categories = expensesByCategory.value
        val budget = monthlyBudget.value
        _insights.value = InsightsEngine.generateInsights(categories, budget, total)
    }

    private val currencyCodeMap = mapOf(
        "₹" to "INR",
        "$" to "USD",
        "€" to "EUR",
        "£" to "GBP",
        "¥" to "JPY",
        "A$" to "AUD",
        "C$" to "CAD",
        "AED" to "AED",
        "S$" to "SGD"
    )

    fun saveExpenseWithConversion(
        originalAmountStr: String,
        expenseCurrencySymbol: String,
        baseCurrencySymbol: String,
        expense: Expense
    ) {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalAmount = originalAmountStr.toDoubleOrNull() ?: 0.0
                
                val convertedAmount = if (expenseCurrencySymbol == baseCurrencySymbol) {
                    originalAmount
                } else {
                    val fromCode = currencyCodeMap[expenseCurrencySymbol] ?: "USD"
                    val toCode = currencyCodeMap[baseCurrencySymbol] ?: "USD"
                    try {
                        val rate = CurrencyApi.getRate(fromCode, toCode)
                        originalAmount * rate
                    } catch (e: Exception) {
                        originalAmount
                    }
                }

                expense.amount = String.format(Locale.US, "%.2f", convertedAmount)
                if (expenseCurrencySymbol != baseCurrencySymbol) {
                    expense.note = if (expense.note.isNotBlank()) {
                        "${expense.note} (Original: $expenseCurrencySymbol$originalAmountStr)"
                    } else {
                        "(Original: $expenseCurrencySymbol$originalAmountStr)"
                    }
                }

                // Balance update based on transaction type
                val wallet = repository.getWalletById(expense.walletId) ?: allWallets.value.firstOrNull()
                if (wallet != null) {
                    expense.walletId = wallet.id
                    when (expense.transactionType) {
                        "INCOME" -> wallet.balance += convertedAmount
                        "EXPENSE" -> wallet.balance -= convertedAmount
                        "TRANSFER" -> {
                            wallet.balance -= convertedAmount
                            expense.transferToWalletId?.let { targetId ->
                                val targetWallet = repository.getWalletById(targetId)
                                if (targetWallet != null) {
                                    targetWallet.balance += convertedAmount
                                    targetWallet.isDirty = true
                                    repository.updateWallet(targetWallet)
                                }
                            }
                        }
                    }
                    wallet.isDirty = true
                    repository.updateWallet(wallet)
                }

                repository.insert(expense)
            } catch (e: Exception) {
                // If offline or calculation error, insert as-is
                expense.amount = originalAmountStr
                repository.insert(expense)
            }
        }
    }

    fun setCurrency(symbol: String) {
        prefs.edit().putString("currency", symbol).apply()
        _currencySymbol.value = symbol
    }

    fun setMonthlyBudget(budget: Float) {
        prefs.edit().putFloat("monthly_budget", budget).apply()
        _monthlyBudget.value = budget
    }

    fun setTravelModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("travel_mode", enabled).apply()
        _isTravelModeEnabled.value = enabled
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("daily_reminder", enabled).apply()
        _dailyReminderEnabled.value = enabled
    }

    fun setAccentColor(hex: String) {
        prefs.edit().putString("accent_color", hex).apply()
        _accentColorHex.value = hex
    }

    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun updateProfilePhoto(uri: String) {
        prefs.edit().putString("profile_photo", uri).apply()
        _profilePhotoUri.value = uri
    }

    fun saveCustomProfilePhoto(context: Context, sourceUri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val avatarFile = java.io.File(context.filesDir, "custom_user_avatar.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    java.io.FileOutputStream(avatarFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val localPath = avatarFile.absolutePath
                prefs.edit().putString("profile_photo", localPath).apply()
                _profilePhotoUri.value = localPath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insert(expense: Expense) {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch {
            repository.insert(expense)
        }
    }

    fun update(expense: Expense) {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun tagExpensesWithLocation(latitude: Double, longitude: Double) {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch {
            val list = allExpenses.value
            val unlocated = list.filter { it.latitude == null || it.longitude == null }
            val targetExpenses = if (unlocated.isNotEmpty()) unlocated else list
            targetExpenses.forEachIndexed { idx, expense ->
                val angle = (idx * 137.5) * (Math.PI / 180.0)
                val dist = 0.0008 + (idx % 6) * 0.0012
                val latOffset = dist * Math.cos(angle)
                val lngOffset = dist * Math.sin(angle)
                val updated = expense.copy(
                    latitude = latitude + latOffset,
                    longitude = longitude + lngOffset,
                    isDirty = true
                )
                repository.update(updated)
            }
        }
    }

    fun delete(expense: Expense) {
        if (_accountStatus.value != "ACTIVE") return
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    // Subscriptions
    fun insertSubscription(subscription: Subscription) {
        viewModelScope.launch { repository.insertSubscription(subscription) }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch { repository.deleteSubscription(subscription) }
    }

    // Savings Goals
    fun insertSavingsGoal(savingsGoal: SavingsGoal) {
        viewModelScope.launch { repository.insertSavingsGoal(savingsGoal) }
    }

    fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        viewModelScope.launch { repository.updateSavingsGoal(savingsGoal) }
    }

    fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        viewModelScope.launch { repository.deleteSavingsGoal(savingsGoal) }
    }

    // Wallets
    fun insertWallet(wallet: Wallet) {
        viewModelScope.launch { repository.insertWallet(wallet) }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch { repository.updateWallet(wallet) }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch { repository.deleteWallet(wallet) }
    }

    fun transferFunds(
        fromWallet: Wallet,
        toWallet: Wallet,
        amount: Double,
        note: String = "Transfer"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (amount <= 0 || fromWallet.id == toWallet.id) return@launch

            val convertedAmount = if (fromWallet.currency == toWallet.currency) {
                amount
            } else {
                val fromCode = currencyCodeMap[fromWallet.currency] ?: "INR"
                val toCode = currencyCodeMap[toWallet.currency] ?: "INR"
                try {
                    val rate = CurrencyApi.getRate(fromCode, toCode)
                    amount * rate
                } catch (e: Exception) {
                    amount
                }
            }

            fromWallet.balance -= amount
            fromWallet.isDirty = true
            repository.updateWallet(fromWallet)

            toWallet.balance += convertedAmount
            toWallet.isDirty = true
            repository.updateWallet(toWallet)

            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            repository.insert(
                Expense(
                    amount = String.format(java.util.Locale.US, "%.2f", amount),
                    category = "Transfer",
                    date = today,
                    paymentMethod = fromWallet.name,
                    note = "Transfer to ${toWallet.name}${if (note.isNotBlank()) ": $note" else ""}",
                    walletId = fromWallet.id,
                    transactionType = "TRANSFER",
                    transferToWalletId = toWallet.id
                )
            )
        }
    }

    // Category Budgets
    fun insertCategoryBudget(categoryBudget: CategoryBudget) {
        viewModelScope.launch { repository.insertCategoryBudget(categoryBudget) }
    }

    fun updateCategoryBudget(categoryBudget: CategoryBudget) {
        viewModelScope.launch { repository.updateCategoryBudget(categoryBudget) }
    }

    fun deleteCategoryBudget(categoryBudget: CategoryBudget) {
        viewModelScope.launch { repository.deleteCategoryBudget(categoryBudget) }
    }

    // Debts
    fun insertDebt(debt: Debt) {
        viewModelScope.launch { repository.insertDebt(debt) }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch { repository.updateDebt(debt) }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch { repository.deleteDebt(debt) }
    }

    // Investments
    fun insertInvestment(investment: Investment) {
        viewModelScope.launch { repository.insertInvestment(investment) }
    }

    fun updateInvestment(investment: Investment) {
        viewModelScope.launch { repository.updateInvestment(investment) }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch { repository.deleteInvestment(investment) }
    }

    // Categories
    fun insertCategory(category: Category) {
        viewModelScope.launch { repository.insertCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    // Full Database Backup & Restore
    fun exportFullBackup(context: Context, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = ExpenseDatabase.getDatabase(context)
                val path = com.camuel.sanchay.utils.BackupManager.createFullBackup(context, db)
                onComplete(true, path)
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Export failed")
            }
        }
    }

    fun restoreFullBackup(context: Context, uri: android.net.Uri, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = ExpenseDatabase.getDatabase(context)
                val result = com.camuel.sanchay.utils.BackupManager.restoreFullBackup(context, uri, db)
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    onComplete(true, "Restored $count records successfully")
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Restore failed")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Restore failed")
            }
        }
    }

    // --- Bank Account Actions ---
    fun savePlaidConfig(clientId: String, secret: String, env: String, accessToken: String) {
        prefs.edit()
            .putString("plaid_client_id", clientId.trim())
            .putString("plaid_secret", secret.trim())
            .putString("plaid_env", env.trim().lowercase())
            .putString("plaid_access_token", accessToken.trim())
            .apply()
        _plaidClientId.value = clientId.trim()
        _plaidSecret.value = secret.trim()
        _plaidEnv.value = env.trim().lowercase()
        _plaidAccessToken.value = accessToken.trim()
    }

    fun syncBankAccounts(useDemoIfUnconfigured: Boolean = true, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isBankSyncing.value = true
            try {
                val hasKeys = _plaidClientId.value.isNotBlank() && _plaidSecret.value.isNotBlank()
                val result = if (hasKeys) {
                    bankSyncService.fetchBankAccounts(
                        clientId = _plaidClientId.value,
                        secret = _plaidSecret.value,
                        env = _plaidEnv.value,
                        accessToken = _plaidAccessToken.value
                    )
                } else if (useDemoIfUnconfigured) {
                    bankSyncService.fetchSandboxDemoAccounts()
                } else {
                    com.camuel.sanchay.services.BankSyncResult(
                        success = false,
                        message = "Plaid API Keys not configured. Please enter PLAID_CLIENT_ID and PLAID_SECRET in Settings."
                    )
                }

                if (result.success) {
                    _connectedBankAccounts.value = result.accounts
                    if (!result.newAccessToken.isNullOrBlank()) {
                        savePlaidConfig(_plaidClientId.value, _plaidSecret.value, _plaidEnv.value, result.newAccessToken)
                    }
                    val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                    prefs.edit().putString("last_bank_sync_time", now).apply()
                    _lastBankSyncTime.value = now
                    onComplete(true, result.message)
                } else {
                    onComplete(false, result.message)
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Bank sync failed")
            } finally {
                _isBankSyncing.value = false
            }
        }
    }

    fun syncActualBalanceToWallet(account: com.camuel.sanchay.services.BankAccount, wallet: Wallet, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                wallet.balance = account.currentBalance
                wallet.isDirty = true
                repository.updateWallet(wallet)
                onComplete(true, "Wallet '${wallet.name}' updated to actual bank balance: ${wallet.currency}${String.format(java.util.Locale.US, "%,.2f", account.currentBalance)}")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to update balance")
            }
        }
    }

    fun linkAccountAsNewWallet(account: com.camuel.sanchay.services.BankAccount, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val newWallet = Wallet(
                    name = "${account.name} (••${account.mask ?: "Bank"})",
                    balance = account.currentBalance,
                    currency = account.currency,
                    colorHex = "#22C55E",
                    isDirty = true
                )
                repository.insertWallet(newWallet)
                onComplete(true, "Created wallet for ${account.name} with actual balance: ${account.currency}${String.format(java.util.Locale.US, "%,.2f", account.currentBalance)}")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to link bank account")
            }
        }
    }
}
