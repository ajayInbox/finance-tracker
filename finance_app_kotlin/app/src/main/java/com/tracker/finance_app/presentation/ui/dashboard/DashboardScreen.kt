package com.tracker.finance_app.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tracker.finance_app.presentation.components.ShimmerDashboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in Snackbar with retry action
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "Retry",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.loadDashboardData(isRefresh = true)
            }
            viewModel.dismissError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FCFB),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading && !uiState.isRefreshing) {
            ShimmerDashboard(modifier = Modifier.padding(innerPadding))
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.loadDashboardData(isRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val totalIncome = uiState.summary?.totalIncome ?: 0.0
                val totalExpense = uiState.summary?.totalExpense ?: 0.0
                val netSavings = uiState.summary?.netSavings ?: (totalIncome - totalExpense)

                if (uiState.accounts.isEmpty()) {
                    // DASHBOARD 1: New user / No accounts added yet (Expense tracking focus)
                    Dashboard1Content(
                        userName = uiState.userName,
                        totalExpense = totalExpense,
                        expenseTrend = uiState.expenseTrend,
                        breakdowns = uiState.breakdowns,
                        recentTransactions = uiState.recentTransactions,
                        onNavigateToAddAccount = onNavigateToAddAccount,
                        onNavigateToTransactions = onNavigateToTransactions,
                        modifier = modifier
                    )
                } else {
                    // DASHBOARD 2: Connected accounts / Full financial overview
                    Dashboard2Content(
                        userName = uiState.userName,
                        selectedMonth = uiState.selectedMonth,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netSavings = netSavings,
                        incomeTrend = uiState.incomeTrend,
                        expenseTrend = uiState.expenseTrend,
                        savingsTrend = uiState.savingsTrend,
                        monthlyBudget = uiState.monthlyBudget,
                        breakdowns = uiState.breakdowns,
                        accounts = uiState.accounts,
                        recentTransactions = uiState.recentTransactions,
                        onMonthSelected = { viewModel.onMonthSelected(it) },
                        onNavigateToAccounts = onNavigateToAccounts,
                        onNavigateToCategories = onNavigateToCategories,
                        onNavigateToTransactions = onNavigateToTransactions,
                        modifier = modifier
                    )
                }
            }
        }
    }
}
