package com.tracker.finance_app.presentation.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.domain.model.AccountType
import com.tracker.finance_app.presentation.components.ShimmerList
import com.tracker.finance_app.presentation.components.SplitAmountText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    onNavigateToAddAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddAccount) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadData(isRefresh = true) },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                ShimmerList(modifier = Modifier.padding(16.dp))
            } else if (uiState.accounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "No accounts found. Tap '+' to create one.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.netWorthSummary?.let { summary ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Net Worth",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SplitAmountText(
                                        amount = summary.netWorth,
                                        integerFontSize = 32.sp,
                                        decimalFontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Assets: ₹${String.format("%.2f", summary.totalAssets)}")
                                        Text("Liabilities: ₹${String.format("%.2f", summary.totalLiabilities)}")
                                    }
                                }
                            }
                        }
                    }

                    items(uiState.accounts, key = { it.id }) { account ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteAccount(account.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = MaterialTheme.colorScheme.errorContainer
                                val iconColor = MaterialTheme.colorScheme.onErrorContainer
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, shape = CardDefaults.shape)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Account",
                                        tint = iconColor
                                    )
                                }
                            }
                        ) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                ListItem(
                                    leadingContent = {
                                        val iconBgColor = when (account.type) {
                                            AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> Color(0xFF2196F3) // Blue
                                            AccountType.CREDIT_CARD -> Color(0xFFFF9800) // Orange
                                            AccountType.CASH, AccountType.WALLET -> Color(0xFF10B981) // Emerald
                                            AccountType.INVESTMENT -> Color(0xFF9C27B0) // Purple
                                            AccountType.LOAN -> Color(0xFFF44336) // Red
                                            else -> Color.Gray
                                        }
                                        val icon = when (account.type) {
                                            AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> Icons.Default.AccountBalance
                                            AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                                            AccountType.CASH, AccountType.WALLET -> Icons.Default.Money
                                            AccountType.INVESTMENT -> Icons.Default.TrendingUp
                                            else -> Icons.Default.Warning
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(iconBgColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = account.type.label,
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    headlineContent = { Text(account.name) },
                                    supportingContent = {
                                        Text("${account.type.label} • ${account.institution ?: "Personal"}")
                                    },
                                    trailingContent = {
                                        SplitAmountText(
                                            amount = account.balance
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
