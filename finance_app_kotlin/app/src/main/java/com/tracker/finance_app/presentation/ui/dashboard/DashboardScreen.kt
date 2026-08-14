package com.tracker.finance_app.presentation.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.presentation.components.AnimatedCounter
import com.tracker.finance_app.presentation.components.GlowCard
import com.tracker.finance_app.presentation.components.ShimmerDashboard
import com.tracker.finance_app.presentation.components.charts.DonutChart
import com.tracker.finance_app.presentation.components.charts.PeriodSelector
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        in 17..20 -> "GOOD EVENING"
        else -> "GOOD NIGHT"
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        ) { innerPadding ->
            if (uiState.isLoading && !uiState.isRefreshing) {
                ShimmerDashboard(modifier = Modifier.padding(innerPadding))
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.loadDashboardData(isRefresh = true) },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        // Header: User Greeting & Avatar
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = greeting,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.userName ?: "User",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                IconButton(onClick = { viewModel.loadDashboardData(isRefresh = true) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                                }
                            }
                        }

                        // Top Net Worth Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "TOTAL NET WORTH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AnimatedCounter(
                                        targetValue = uiState.netWorthSummary?.netWorth ?: 0.0,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Assets",
                                                tint = Color(0xFF00C853),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Assets: ",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            AnimatedCounter(
                                                targetValue = uiState.netWorthSummary?.totalAssets ?: 0.0,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Liabilities",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Liabilities: ",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            AnimatedCounter(
                                                targetValue = uiState.netWorthSummary?.totalLiabilities ?: 0.0,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Link Account / Add Account Banner if accounts are empty
                        if (uiState.accounts.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = "Bank",
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("No accounts connected", style = MaterialTheme.typography.titleMedium)
                                            Text("Connect a bank or add a manual account.", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(onClick = onNavigateToAddAccount) {
                                            Text("Add")
                                        }
                                    }
                                }
                            }
                        }

                        // Income & Expense Summary Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GlowCard(
                                    glowColor = Color(0xFF00C853),
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Total Income", style = MaterialTheme.typography.labelMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AnimatedCounter(
                                            targetValue = uiState.summary?.totalIncome ?: 0.0,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF00C853)
                                        )
                                    }
                                }
                                GlowCard(
                                    glowColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Total Expense", style = MaterialTheme.typography.labelMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AnimatedCounter(
                                            targetValue = uiState.summary?.totalExpense ?: 0.0,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // Average Daily Expense
                        uiState.averageDailyExpense?.let { avgDaily ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Average Daily Expense",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AnimatedCounter(
                                            targetValue = avgDaily.average,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Category Breakdown / Spending Analysis Section
                        item {
                            Text(
                                text = "Spending Analysis",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        item {
                            PeriodSelector(
                                selectedPeriod = uiState.selectedPeriod,
                                onPeriodSelected = { viewModel.onPeriodChanged(it) }
                            )
                        }
                        
                        if (uiState.breakdowns.isEmpty()) {
                            item {
                                Text(
                                    text = "No category breakdowns available yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        DonutChart(breakdowns = uiState.breakdowns)
                                    }
                                }
                            }
                        }

                        // Recent Transactions
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
                        }

                        if (uiState.recentTransactions.isEmpty()) {
                            item {
                                Text(
                                    text = "No recent transactions.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(uiState.recentTransactions, key = { it.id }) { item ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    ListItem(
                                        headlineContent = { Text(item.description) },
                                        supportingContent = { Text(item.categoryName ?: "General") },
                                        trailingContent = {
                                            val isIncome = item.type == TransactionType.INCOME
                                            Text(
                                                text = "${if (isIncome) "+" else "-"}${Formatters.formatCurrency(item.amount)}",
                                                color = if (isIncome) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
