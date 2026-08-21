package com.tracker.finance_app.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.CategoryBreakdown
import com.tracker.finance_app.domain.model.Transaction

@Composable
fun Dashboard2Content(
    userName: String?,
    selectedMonth: String,
    totalIncome: Double,
    totalExpense: Double,
    netSavings: Double,
    incomeTrend: String,
    expenseTrend: String,
    savingsTrend: String,
    monthlyBudget: Double,
    breakdowns: List<CategoryBreakdown>,
    accounts: List<Account>,
    recentTransactions: List<Transaction>,
    onMonthSelected: (String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Map account id to account name for transaction subtitle
    val accountMap = accounts.associate { it.id to it.name }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Top Bar
        item {
            DashboardTopBar()
        }

        // Greeting
        item {
            DashboardGreeting(
                userName = userName,
                subtitle = "Here’s your financial overview"
            )
        }

        // Card 1: This Month Overview (Metrics & Budget)
        item {
            FinCard(contentPadding = PaddingValues(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Month Overview",
                        fontSize = 15.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = FinTextDark
                    )
                    MonthSelectorChip(
                        selectedMonth = selectedMonth,
                        onMonthSelected = onMonthSelected
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ThreeMetricsRow(
                    income = totalIncome,
                    expense = totalExpense,
                    netSavings = netSavings,
                    incomeTrend = incomeTrend,
                    expenseTrend = expenseTrend,
                    savingsTrend = savingsTrend
                )

                Spacer(modifier = Modifier.height(14.dp))

                BudgetProgressBar(
                    spent = totalExpense,
                    budgetLimit = monthlyBudget
                )
            }
        }

        // Card 2: Spending by Category
        item {
            FinCard(contentPadding = PaddingValues(18.dp)) {
                SectionHeading(
                    title = "Spending by Category",
                    actionText = "View All",
                    onActionClick = onNavigateToCategories
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (breakdowns.isEmpty()) {
                    Text(
                        text = "No category breakdowns available this month.",
                        fontSize = 12.sp,
                        color = FinTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SpendingDonutChart(
                            breakdowns = breakdowns,
                            totalExpense = totalExpense,
                            size = 130.dp,
                            strokeWidth = 20.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            breakdowns.take(6).forEachIndexed { index, item ->
                                CategoryItemRow(
                                    categoryName = item.categoryName,
                                    amount = item.totalAmount,
                                    percentage = item.percentage,
                                    colorIndex = index
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Accounts
        item {
            FinCard(contentPadding = PaddingValues(18.dp)) {
                SectionHeading(
                    title = "Accounts",
                    actionText = "View All",
                    onActionClick = onNavigateToAccounts
                )

                Spacer(modifier = Modifier.height(6.dp))

                accounts.take(4).forEachIndexed { index, account ->
                    AccountItemRow(
                        account = account,
                        onClick = onNavigateToAccounts
                    )
                    if (index < accounts.size - 1 && index < 3) {
                        HorizontalDivider(color = FinDivider, thickness = 1.dp)
                    }
                }
            }
        }

        // Section 4: Recent Transactions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeading(
                title = "Recent Transactions",
                actionText = "See All",
                onActionClick = onNavigateToTransactions
            )
        }

        if (recentTransactions.isEmpty()) {
            item {
                Text(
                    text = "No recent transactions found.",
                    fontSize = 13.sp,
                    color = FinTextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            itemsIndexed(recentTransactions, key = { _, tx -> tx.id }) { index, transaction ->
                Column {
                    TransactionItemRow(
                        transaction = transaction,
                        accountName = accountMap[transaction.accountId] ?: transaction.merchantName,
                        onClick = onNavigateToTransactions
                    )
                    if (index < recentTransactions.size - 1) {
                        HorizontalDivider(color = FinDivider, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
