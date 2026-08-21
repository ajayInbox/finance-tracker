package com.tracker.finance_app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tracker.finance_app.presentation.ui.account.AccountDetailsBottomSheet
import com.tracker.finance_app.presentation.ui.account.AccountsScreen
import com.tracker.finance_app.presentation.ui.account.AccountsViewModel
import com.tracker.finance_app.presentation.ui.account.AddAccountDialog
import com.tracker.finance_app.presentation.ui.auth.AuthViewModel
import com.tracker.finance_app.presentation.ui.auth.SignInScreen
import com.tracker.finance_app.presentation.ui.auth.SignUpScreen
import com.tracker.finance_app.presentation.ui.category.CategoryManagementScreen
import com.tracker.finance_app.presentation.ui.category.CategoryViewModel
import com.tracker.finance_app.presentation.ui.dashboard.DashboardScreen
import com.tracker.finance_app.presentation.ui.dashboard.DashboardViewModel
import com.tracker.finance_app.presentation.ui.settings.SettingsScreen
import com.tracker.finance_app.presentation.ui.settings.SettingsViewModel
import com.tracker.finance_app.presentation.ui.sms.SmsReviewScreen
import com.tracker.finance_app.presentation.ui.sms.SmsViewModel
import com.tracker.finance_app.presentation.ui.transaction.AddTransactionDialog
import com.tracker.finance_app.presentation.ui.transaction.AddTransactionScreen
import com.tracker.finance_app.presentation.ui.account.AddAccountScreen
import com.tracker.finance_app.presentation.ui.transaction.TransactionsScreen
import com.tracker.finance_app.presentation.ui.transaction.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared ViewModels scoped to the NavHost (not individual routes)
    val accountsViewModel: AccountsViewModel = hiltViewModel()
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()

    val accountsUiState by accountsViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Accounts.route,
        Screen.Transactions.route,
        Screen.CategoryManagement.route,
        Screen.SmsReview.route,
        Screen.Settings.route
    )

    val showFab = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Accounts.route,
        Screen.Transactions.route
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                com.tracker.finance_app.presentation.components.SpeedDialFab(
                    onAddExpense = { navController.navigate(Screen.AddTransaction.route) },
                    onAddIncome = { navController.navigate(Screen.AddTransaction.route) },
                    onTransfer = { 
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Coming soon")
                        }
                    },
                    onSyncSms = { navController.navigate(Screen.SmsReview.route) }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Accounts.route,
                        onClick = {
                            navController.navigate(Screen.Accounts.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Accounts") },
                        label = { Text("Accounts") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Transactions.route,
                        onClick = {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text("Transactions") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.CategoryManagement.route,
                        onClick = {
                            navController.navigate(Screen.CategoryManagement.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                        label = { Text("Categories") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.SmsReview.route,
                        onClick = {
                            navController.navigate(Screen.SmsReview.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.MarkEmailUnread, contentDescription = "SMS") },
                        label = { Text("SMS") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SignIn.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.SignIn.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                SignInScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onSignInSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateToSignIn = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) },
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToCategories = { navController.navigate(Screen.CategoryManagement.route) }
                )
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    viewModel = accountsViewModel,
                    onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    viewModel = transactionsViewModel,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
            composable(Screen.AddAccount.route) {
                AddAccountScreen(
                    viewModel = accountsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    viewModel = transactionsViewModel,
                    accounts = accountsUiState.accounts,
                    categories = categoryUiState.categories,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    viewModel = categoryViewModel,
                    onNavigateToAddGroup = { navController.navigate(Screen.AddCategoryGroup.route) }
                )
            }
            composable(Screen.AddCategoryGroup.route) {
                com.tracker.finance_app.presentation.ui.category.AddCategoryGroupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveGroup = { name, icon, colorHex ->
                        categoryViewModel.createCategoryGroup(name, icon, colorHex)
                    }
                )
            }
            composable(Screen.SmsReview.route) {
                val smsViewModel: SmsViewModel = hiltViewModel()
                SmsReviewScreen(viewModel = smsViewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onSignOut = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToCategories = {
                        navController.navigate(Screen.CategoryManagement.route)
                    },
                    onNavigateToAccounts = {
                        navController.navigate(Screen.Accounts.route)
                    },
                    onNavigateToSmsReview = {
                        navController.navigate(Screen.SmsReview.route)
                    }
                )
            }
        }
    }
}
