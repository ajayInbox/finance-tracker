package com.tracker.finance_app.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.CategoryBreakdown
import com.tracker.finance_app.domain.model.Transaction

@Composable
fun Dashboard1Content(
    userName: String?,
    totalExpense: Double,
    expenseTrend: String,
    breakdowns: List<CategoryBreakdown>,
    recentTransactions: List<Transaction>,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                subtitle = "Let’s track your spending"
            )
        }

        // Spending Card
        item {
            FinCard(contentPadding = PaddingValues(18.dp)) {
                Text(
                    text = "This Month’s Spending",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinTextDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Row: Amount + Trend on left, Donut chart on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Formatters.formatCurrency(totalExpense),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FinTextDark,
                            letterSpacing = (-0.8).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total expenses",
                            fontSize = 13.sp,
                            color = FinTextMuted
                        )
                        if (expenseTrend.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = expenseTrend,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FinGreenBrand
                            )
                        }
                    }

                    SpendingDonutChart(
                        breakdowns = breakdowns,
                        totalExpense = totalExpense,
                        size = 118.dp,
                        strokeWidth = 17.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories Breakdown List
                if (breakdowns.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        breakdowns.take(5).forEachIndexed { index, item ->
                            CategoryItemRow(
                                categoryName = item.categoryName,
                                amount = item.totalAmount,
                                percentage = item.percentage,
                                colorIndex = index
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No category spending recorded this month",
                        fontSize = 12.sp,
                        color = FinTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = FinDivider, thickness = 1.dp)

                // Add an Account Banner Prompt
                AccountPromptBanner(
                    onClick = onNavigateToAddAccount
                )
            }
        }

        // Recent Transactions Section
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
                    text = "No recent transactions. Tap + to add an expense.",
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
                        accountName = null, // No accounts linked yet in D1
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
