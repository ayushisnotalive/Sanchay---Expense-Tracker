package com.camuel.sanchay

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.camuel.sanchay.ui.BottomNavItem
import com.camuel.sanchay.ui.SanchayNavGraph
import com.camuel.sanchay.ui.components.NeonButton
import com.camuel.sanchay.ui.screens.OnboardingScreen
import com.camuel.sanchay.ui.screens.AccountRestrictedScreen
import com.camuel.sanchay.ui.screens.TermsAndConditionsScreen
import com.camuel.sanchay.ui.theme.SanchayTheme
import com.camuel.sanchay.utils.SubscriptionWorker
import com.camuel.sanchay.utils.ReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        expenseViewModel.checkAutoReactivation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        scheduleSubscriptionWorker()
        scheduleSyncWorker()
        scheduleSmartBudgetWorker()

        // Trigger background sync on startup
        expenseViewModel.triggerSync()

        lifecycleScope.launch {
            expenseViewModel.dailyReminderEnabled.collect {
                scheduleDailyReminderWorker()
            }
        }

        setContent {
            val accentColorHex by expenseViewModel.accentColorHex.collectAsState()
            val accentColor = remember(accentColorHex) { Color(accentColorHex.toColorInt()) }
            val isBiometricLockEnabled by expenseViewModel.isBiometricLockEnabled.collectAsState()
            val hasAcceptedTerms by expenseViewModel.hasAcceptedTerms.collectAsState()
            val accountStatus by expenseViewModel.accountStatus.collectAsState()
            val scheduledDeletionDate by expenseViewModel.scheduledDeletionDate.collectAsState()
            val deactivatedUntilDate by expenseViewModel.deactivatedUntilDate.collectAsState()
            val isAccountRestricted = accountStatus != "ACTIVE"
            var isAuthenticated by remember { mutableStateOf(!isBiometricLockEnabled) }

            LaunchedEffect(isBiometricLockEnabled) {
                if (isBiometricLockEnabled && !isAuthenticated) {
                    BiometricHelper.showBiometricPrompt(
                        activity = this@MainActivity,
                        onSuccess = { isAuthenticated = true },
                        onError = { /* Prompt stays locked until user unlocks */ }
                    )
                }
            }

            SanchayTheme(accentColor = accentColor) {
                if (!hasAcceptedTerms) {
                    OnboardingScreen(
                        viewModel = expenseViewModel,
                        onComplete = { /* Automatically transitions once hasAcceptedTerms becomes true */ }
                    )
                } else if (isAccountRestricted) {
                    AccountRestrictedScreen(
                        accountStatus = accountStatus,
                        scheduledDeletionDate = scheduledDeletionDate,
                        deactivatedUntilDate = deactivatedUntilDate,
                        onRestoreAccount = {
                            expenseViewModel.reactivateAccount()
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "Account successfully restored! Welcome back.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSignOut = {
                            expenseViewModel.signOutAccount()
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "Signed out successfully.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else if (isBiometricLockEnabled && !isAuthenticated) {
                    // Biometric Authentication Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Sanchay Vault Locked",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Biometric authentication is required to access your financial records.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            NeonButton(
                                onClick = {
                                    BiometricHelper.showBiometricPrompt(
                                        activity = this@MainActivity,
                                        onSuccess = { isAuthenticated = true },
                                        onError = { /* Keep locked */ }
                                    )
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Fingerprint, null)
                                    Text("Unlock with Biometrics", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val shortcutTarget = remember { intent?.getStringExtra("navigate_to") }
                    var shortcutHandled by remember { mutableStateOf(false) }

                    LaunchedEffect(shortcutTarget, isAuthenticated) {
                        if (isAuthenticated && !shortcutTarget.isNullOrBlank() && !shortcutHandled) {
                            shortcutHandled = true
                            navController.navigate(shortcutTarget)
                        }
                    }

                    val bottomBarScreens = listOf(
                        BottomNavItem.Home.route,
                        BottomNavItem.Analytics.route,
                        BottomNavItem.Wallets.route,
                        BottomNavItem.Tools.route
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = if (currentRoute in bottomBarScreens) WindowInsets(0.dp) else WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (currentRoute in bottomBarScreens) {
                                NavigationBar(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                    windowInsets = NavigationBarDefaults.windowInsets,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    tonalElevation = 4.dp
                                ) {
                                    // 1. Home
                                    val homeSelected = currentRoute == BottomNavItem.Home.route
                                    NavigationBarItem(
                                        icon = { Icon(BottomNavItem.Home.icon, contentDescription = "Home", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Home", fontSize = 11.sp, fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Normal) },
                                        selected = homeSelected,
                                        alwaysShowLabel = true,
                                        onClick = {
                                            if (currentRoute != BottomNavItem.Home.route) {
                                                navController.navigate(BottomNavItem.Home.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )

                                    // 2. Stats (Analytics)
                                    val statsSelected = currentRoute == BottomNavItem.Analytics.route
                                    NavigationBarItem(
                                        icon = { Icon(BottomNavItem.Analytics.icon, contentDescription = "Stats", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Stats", fontSize = 11.sp, fontWeight = if (statsSelected) FontWeight.Bold else FontWeight.Normal) },
                                        selected = statsSelected,
                                        alwaysShowLabel = true,
                                        onClick = {
                                            if (currentRoute != BottomNavItem.Analytics.route) {
                                                navController.navigate(BottomNavItem.Analytics.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )

                                    // 3. Central Action Button (+)
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                navController.navigate("add_expense") {
                                                    launchSingleTop = true
                                                }
                                            },
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White,
                                            shape = CircleShape,
                                            modifier = Modifier.size(46.dp),
                                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Transaction",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    // 4. Wallets
                                    val walletsSelected = currentRoute == BottomNavItem.Wallets.route
                                    NavigationBarItem(
                                        icon = { Icon(BottomNavItem.Wallets.icon, contentDescription = "Wallets", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Wallets", fontSize = 11.sp, fontWeight = if (walletsSelected) FontWeight.Bold else FontWeight.Normal) },
                                        selected = walletsSelected,
                                        alwaysShowLabel = true,
                                        onClick = {
                                            if (currentRoute != BottomNavItem.Wallets.route) {
                                                navController.navigate(BottomNavItem.Wallets.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )

                                    // 5. Tools
                                    val toolsSelected = currentRoute == BottomNavItem.Tools.route
                                    NavigationBarItem(
                                        icon = { Icon(BottomNavItem.Tools.icon, contentDescription = "Tools", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Tools", fontSize = 11.sp, fontWeight = if (toolsSelected) FontWeight.Bold else FontWeight.Normal) },
                                        selected = toolsSelected,
                                        alwaysShowLabel = true,
                                        onClick = {
                                            if (currentRoute != BottomNavItem.Tools.route) {
                                                navController.navigate(BottomNavItem.Tools.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = padding.calculateBottomPadding())
                        ) {
                            SanchayNavGraph(
                                navController = navController,
                                viewModel = expenseViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleSubscriptionWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SubscriptionWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "subscription_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleDailyReminderWorker() {
        val isEnabled = expenseViewModel.dailyReminderEnabled.value
        if (!isEnabled) {
            WorkManager.getInstance(this).cancelUniqueWork("daily_expense_reminder")
            return
        }

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS) // Show roughly in the evening
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_expense_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleSyncWorker() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<com.camuel.sanchay.utils.SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sanchay_cloud_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleSmartBudgetWorker() {
        val workRequest = PeriodicWorkRequestBuilder<com.camuel.sanchay.utils.SmartNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(6, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sanchay_smart_budget_alert",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
