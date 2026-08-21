package com.tracker.finance_app.presentation.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType
import com.tracker.finance_app.domain.model.CategoryBreakdown
import com.tracker.finance_app.domain.model.Transaction
import com.tracker.finance_app.domain.model.TransactionType

// Brand Design Colors
val FinGreenBrand = Color(0xFF087B35)
val FinGreenLight = Color(0xFF22AA5B)
val FinGreenBg = Color(0xFFEBF8EF)
val FinGreenBgAlt = Color(0xFFE7F6ED)
val FinRedExpense = Color(0xFFF0444D)
val FinRedBg = Color(0xFFFFE9EB)
val FinBlue = Color(0xFF3D84DD)
val FinBlueBg = Color(0xFFEAF3FF)
val FinOrange = Color(0xFFFFAD27)
val FinOrangeBg = Color(0xFFFFF3DC)
val FinPurple = Color(0xFF8F67D5)
val FinPurpleBg = Color(0xFFF0EAFF)
val FinGray = Color(0xFFCFD4DA)
val FinGrayBg = Color(0xFFEEF1F3)
val FinTextDark = Color(0xFF152033)
val FinTextMuted = Color(0xFF778196)
val FinCardBorder = Color(0xFFEDF1EF)
val FinDivider = Color(0xFFEDF0F4)

val CategoryColorPalette = listOf(
    FinGreenLight,
    FinRedExpense,
    FinBlue,
    FinOrange,
    FinPurple,
    FinGray
)

val CategoryBgPalette = listOf(
    FinGreenBgAlt,
    FinRedBg,
    FinBlueBg,
    FinOrangeBg,
    FinPurpleBg,
    FinGrayBg
)

/**
 * Standard FinFlow Card Container with rounded corners (24dp), white background, and subtle border
 */
@Composable
fun FinCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x141F3F34), spotColor = Color(0x141F3F34)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, FinCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Top App Bar with FinFlow Brand logo mark [F] and Notifications Icon with unread indicator
 */
@Composable
fun DashboardTopBar(
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(FinGreenLight, FinGreenBrand)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
            Text(
                text = "FinFlow",
                color = FinGreenBrand,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = FinTextDark,
                modifier = Modifier.size(26.dp)
            )
            // Unread notification red dot
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF3340))
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

/**
 * Greeting Section: "Good morning, Ajay 👋" with subtitle
 */
@Composable
fun DashboardGreeting(
    userName: String?,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greetingPrefix = when (currentHour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good evening"
    }

    val firstName = userName?.split(" ")?.firstOrNull()?.ifBlank { "there" } ?: "there"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$greetingPrefix, $firstName 👋",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = FinTextDark,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = FinTextMuted
        )
    }
}

/**
 * Section Header with title and action link (e.g. "See All", "View All")
 */
@Composable
fun SectionHeading(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = FinTextDark
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinGreenBrand,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

/**
 * Custom Donut Chart for FinFlow spending breakdown
 */
@Composable
fun SpendingDonutChart(
    breakdowns: List<CategoryBreakdown>,
    totalExpense: Double,
    size: Dp = 120.dp,
    strokeWidth: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(breakdowns) {
        animationPlayed = true
    }

    val sweepAngleProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donutProgress"
    )

    val total = if (totalExpense > 0) totalExpense else breakdowns.sumOf { it.totalAmount }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            if (breakdowns.isEmpty() || total <= 0) {
                drawArc(
                    color = FinGreenBg,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    size = canvasSize,
                    topLeft = topLeft
                )
            } else {
                var currentAngle = -90f
                breakdowns.forEachIndexed { index, item ->
                    val sweep = ((item.totalAmount / total) * 360f).toFloat() * sweepAngleProgress
                    if (sweep > 0) {
                        drawArc(
                            color = CategoryColorPalette[index % CategoryColorPalette.size],
                            startAngle = currentAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = canvasSize,
                            topLeft = topLeft
                        )
                        currentAngle += sweep
                    }
                }
            }
        }

        // Center Content: Total amount and "Total"
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = Formatters.formatCurrency(total),
                fontSize = if (size > 130.dp) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = FinTextDark
            )
            Text(
                text = "Total",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = FinTextMuted
            )
        }
    }
}

/**
 * Category breakdown row item
 */
@Composable
fun CategoryItemRow(
    categoryName: String,
    amount: Double,
    percentage: Double,
    colorIndex: Int,
    iconEmoji: String = getEmojiForCategory(categoryName),
    modifier: Modifier = Modifier
) {
    val iconBg = CategoryBgPalette[colorIndex % CategoryBgPalette.size]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconEmoji,
                    fontSize = 11.sp
                )
            }
            Text(
                text = categoryName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FinTextDark,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = Formatters.formatCurrency(amount),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinTextDark
            )
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = FinTextMuted
            )
        }
    }
}

/**
 * "Add an account" banner prompt (Dashboard 1)
 */
@Composable
fun AccountPromptBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(FinGreenBgAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Bank",
                tint = FinGreenBrand,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Add an account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = FinTextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Track your complete financial picture",
                fontSize = 11.sp,
                color = FinTextMuted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Go",
            tint = Color(0xFF8A96A8),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Month pill selector chip (e.g. "May ⌄")
 */
@Composable
fun MonthSelectorChip(
    selectedMonth: String,
    monthsList: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
    onMonthSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FinGreenBg,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedMonth,
                    color = FinGreenBrand,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Month",
                    tint = FinGreenBrand,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            monthsList.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 3-Column Metrics Grid: Income | Expense | Net Savings (Dashboard 2)
 */
@Composable
fun ThreeMetricsRow(
    income: Double,
    expense: Double,
    netSavings: Double,
    incomeTrend: String = "",
    expenseTrend: String = "",
    savingsTrend: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Income Metric
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(text = "Income", fontSize = 11.sp, color = FinTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Formatters.formatCurrency(income),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FinGreenBrand
            )
            if (incomeTrend.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = incomeTrend,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinGreenBrand
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .height(54.dp)
                .padding(vertical = 4.dp),
            color = FinDivider
        )

        // Expense Metric
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = "Expense", fontSize = 11.sp, color = FinTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Formatters.formatCurrency(expense),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FinRedExpense
            )
            if (expenseTrend.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expenseTrend,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinRedExpense
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .height(54.dp)
                .padding(vertical = 4.dp),
            color = FinDivider
        )

        // Net Savings Metric
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(text = "Net Savings", fontSize = 11.sp, color = FinTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Formatters.formatCurrency(netSavings),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FinGreenBrand
            )
            if (savingsTrend.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = savingsTrend,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinGreenBrand
                )
            }
        }
    }
}

/**
 * Monthly Budget Progress Bar with percentage and limits
 */
@Composable
fun BudgetProgressBar(
    spent: Double,
    budgetLimit: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetLimit > 0) (spent / budgetLimit).coerceIn(0.0, 1.0).toFloat() else 0f
    val percentage = (progress * 100).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Budget",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinTextDark
            )
            Text(
                text = "${Formatters.formatCurrency(spent)} / ${Formatters.formatCurrency(budgetLimit)}",
                fontSize = 11.sp,
                color = FinTextMuted
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Custom Gradient Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE9EDF0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FinGreenLight, FinGreenBrand)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$percentage% used",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (percentage > 90) FinRedExpense else FinGreenBrand
        )
    }
}

/**
 * Account Row Item in Accounts Card (Dashboard 2)
 */
@Composable
fun AccountItemRow(
    account: Account,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (iconBg, iconTint, iconVector) = when (account.type) {
        AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> Triple(FinGreenBgAlt, FinGreenBrand, Icons.Default.AccountBalance)
        AccountType.CREDIT_CARD -> Triple(FinPurpleBg, FinPurple, Icons.Default.CreditCard)
        AccountType.CASH -> Triple(FinOrangeBg, FinOrange, Icons.Default.Payments)
        AccountType.WALLET -> Triple(FinBlueBg, FinBlue, Icons.Default.AccountBalanceWallet)
        AccountType.INVESTMENT -> Triple(FinGreenBgAlt, FinGreenBrand, Icons.Default.TrendingUp)
        AccountType.LOAN -> Triple(FinRedBg, FinRedExpense, Icons.Default.AccountBalance)
        AccountType.UNKNOWN -> Triple(FinGrayBg, Color.DarkGray, Icons.Default.AccountBalance)
    }

    val isCredit = account.type == AccountType.CREDIT_CARD || account.category == AccountCategory.LIABILITY

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = account.name,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinTextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = account.type.label,
                fontSize = 10.sp,
                color = FinTextMuted
            )
        }

        Text(
            text = "${if (isCredit) "−" else ""}${Formatters.formatCurrency(account.balance)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) FinRedExpense else FinTextDark
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Details",
            tint = Color(0xFF8A96A8),
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * Transaction Item Row for Recent Transactions
 */
@Composable
fun TransactionItemRow(
    transaction: Transaction,
    accountName: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val iconEmoji = if (isIncome) "↓" else getEmojiForCategory(transaction.categoryName ?: transaction.description)
    val iconBg = if (isIncome) FinGreenBgAlt else getBgForCategory(transaction.categoryName ?: transaction.description)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconEmoji,
                fontSize = 15.sp,
                color = if (isIncome) FinGreenBrand else FinTextDark,
                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description.ifBlank { transaction.categoryName ?: "Transaction" },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinTextDark
            )
            Spacer(modifier = Modifier.height(3.dp))
            val dateText = formatTransactionDate(transaction.timestamp)
            val subtext = if (accountName != null) "$dateText · $accountName" else dateText
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = FinTextMuted
            )
        }

        Text(
            text = "${if (isIncome) "+" else "−"}${Formatters.formatCurrency(transaction.amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) FinGreenBrand else FinRedExpense
        )
    }
}

// Helpers for emoji and colors
fun getEmojiForCategory(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("food") || lower.contains("dining") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("restaurant") -> "🍴"
        lower.contains("coffee") || lower.contains("cafe") || lower.contains("tea") -> "☕"
        lower.contains("shop") || lower.contains("grocery") || lower.contains("store") || lower.contains("market") -> "🛍"
        lower.contains("transport") || lower.contains("uber") || lower.contains("ola") || lower.contains("cab") || lower.contains("fuel") || lower.contains("travel") -> "🚗"
        lower.contains("bill") || lower.contains("electric") || lower.contains("utilit") || lower.contains("recharge") -> "⚡"
        lower.contains("entertainment") || lower.contains("movie") || lower.contains("game") -> "▣"
        lower.contains("salary") || lower.contains("income") -> "💰"
        lower.contains("health") || lower.contains("med") || lower.contains("doctor") -> "💊"
        else -> "•••"
    }
}

fun getBgForCategory(name: String): Color {
    val lower = name.lowercase()
    return when {
        lower.contains("food") || lower.contains("dining") || lower.contains("lunch") -> FinRedBg
        lower.contains("coffee") || lower.contains("cafe") -> FinGreenBgAlt
        lower.contains("shop") || lower.contains("grocery") -> FinPurpleBg
        lower.contains("transport") || lower.contains("uber") || lower.contains("ola") -> FinBlueBg
        lower.contains("bill") || lower.contains("electric") -> FinOrangeBg
        lower.contains("salary") || lower.contains("income") -> FinGreenBgAlt
        else -> FinGrayBg
    }
}

fun formatTransactionDate(rawTimestamp: String): String {
    return try {
        // Try parsing ISO or numeric timestamp
        if (rawTimestamp.all { it.isDigit() }) {
            val millis = rawTimestamp.toLong()
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
        } else {
            // Simplified fallback
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = parser.parse(rawTimestamp)
            if (date != null) {
                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
            } else {
                rawTimestamp
            }
        }
    } catch (e: Exception) {
        "Recent"
    }
}
