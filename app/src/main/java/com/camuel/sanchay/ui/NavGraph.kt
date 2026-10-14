package com.camuel.sanchay.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.camuel.sanchay.ExpenseViewModel
import com.camuel.sanchay.ui.screens.AddExpenseScreen
import com.camuel.sanchay.ui.screens.DashboardScreen
import com.camuel.sanchay.ui.screens.SettingsScreen
import com.camuel.sanchay.ui.screens.AnalyticsScreen
import com.camuel.sanchay.ui.screens.SavingsGoalsScreen
import com.camuel.sanchay.ui.screens.SubscriptionsScreen
import com.camuel.sanchay.ui.screens.InvestmentsScreen
import com.camuel.sanchay.ui.screens.WalletsScreen
import com.camuel.sanchay.ui.screens.DebtTrackerScreen
import com.camuel.sanchay.ui.screens.SplitBillScreen
import com.camuel.sanchay.ui.screens.SearchScreen
import com.camuel.sanchay.ui.screens.SpendingMapScreen

private val rootTabRoutes = setOf("dashboard", "tools", "analytics", "wallets")

@Composable
fun SanchayNavGraph(
    navController: NavHostController,
    viewModel: ExpenseViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        enterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in rootTabRoutes && toRoute in rootTabRoutes) {
                fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))
            } else if (toRoute == "add_expense") {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250))
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250))
            }
        },
        exitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in rootTabRoutes && toRoute in rootTabRoutes) {
                fadeOut(animationSpec = tween(180))
            } else if (fromRoute == "add_expense") {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(200))
            } else {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(200))
            }
        },
        popEnterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in rootTabRoutes && toRoute in rootTabRoutes) {
                fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250))
            }
        },
        popExitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in rootTabRoutes && toRoute in rootTabRoutes) {
                fadeOut(animationSpec = tween(180))
            } else if (fromRoute == "add_expense") {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(200))
            } else {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(200))
            }
        }
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onAddExpenseClick = { navController.navigate("add_expense") },
                onSearchClick = { navController.navigate("search") },
                onAIAssistantClick = { navController.navigate("ai_assistant") },
                onSettingsClick = { navController.navigate("settings") },
                onWalletsClick = { navController.navigate("wallets") }
            )
        }
        composable("tools") {
            com.camuel.sanchay.ui.screens.ToolsScreen(
                viewModel = viewModel,
                onSpendingMapClick = { navController.navigate("spending_map") },
                onSavingsGoalsClick = { navController.navigate("savings_goals") },
                onSubscriptionsClick = { navController.navigate("subscriptions") },
                onInvestmentsClick = { navController.navigate("investments") },
                onSplitBillClick = { navController.navigate("split_bill") },
                onDebtTrackerClick = { navController.navigate("debt_tracker") },
                onAIAssistantClick = { navController.navigate("ai_assistant") }
            )
        }
        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("spending_map") {
            SpendingMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("split_bill") {
            SplitBillScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("add_expense") {
            AddExpenseScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSplitBillClick = { navController.navigate("split_bill") },
                onDebtTrackerClick = { navController.navigate("debt_tracker") },
                onSavingsGoalsClick = { navController.navigate("savings_goals") },
                onSubscriptionsClick = { navController.navigate("subscriptions") },
                onInvestmentsClick = { navController.navigate("investments") },
                onTermsClick = { navController.navigate("terms") }
            )
        }
        composable("analytics") {
            AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("wallets") {
            WalletsScreen(viewModel = viewModel)
        }
        composable("debt_tracker") {
            DebtTrackerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("subscriptions") {
            SubscriptionsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("savings_goals") {
            SavingsGoalsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("investments") {
            InvestmentsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("ai_assistant") {
            com.camuel.sanchay.ui.screens.AIAssistantScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("terms") {
            com.camuel.sanchay.ui.screens.TermsAndConditionsScreen(
                onAccept = { navController.popBackStack() }
            )
        }
    }
}
