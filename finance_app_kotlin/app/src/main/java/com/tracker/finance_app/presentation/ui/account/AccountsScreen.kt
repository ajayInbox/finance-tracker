package com.tracker.finance_app.presentation.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.tracker.finance_app.presentation.components.FinPullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType
import com.tracker.finance_app.presentation.components.ScreenHeader
import com.tracker.finance_app.presentation.components.ShimmerList

// FinFlow Design System Colors
private val FinTextDark = Color(0xFF18253A)
private val FinTextMuted = Color(0xFF657188)
private val FinTextSub = Color(0xFF8D98AA)
private val FinBorder = Color(0xFFDCE1E8)
private val FinBorderLight = Color(0xFFEDF1F5)
private val FinGreen = Color(0xFF087B3D)
private val FinGreenDark = Color(0xFF08783B)
private val FinGreenSoft = Color(0xFFEDF9F1)
private val FinGreenAccent = Color(0xFF10B981)
private val FinRed = Color(0xFFF04B4B)
private val FinRedDark = Color(0xFFD93434)
private val FinRedSoft = Color(0xFFFFF3F3)
private val FinBlueSoft = Color(0xFFEFF6FF)
private val FinBlue = Color(0xFF2563EB)
private val FinOrangeSoft = Color(0xFFFFF7ED)
private val FinOrange = Color(0xFFEA580C)
private val FinPurpleSoft = Color(0xFFF5F3FF)
private val FinPurple = Color(0xFF7C3AED)
private val FinAppBg = Color(0xFFFBFCFD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    onNavigateToAddAccount: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load data whenever entering the screen
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Listen for error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    // Listen for toast/success messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onToastShown()
        }
    }

    // Delete Confirmation Dialog
    uiState.accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteAccount() },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(FinRedSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = FinRedDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete ${account.name}?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FinTextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${account.name}\"? Your past transaction history linked to this account will remain in your records.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FinTextMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteAccount() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinRedDark),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelDeleteAccount() },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, FinBorder),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Cancel", color = FinTextDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = FinAppBg,
        bottomBar = {
            // Floating Bottom Bar with + Add Account button
            // Single route: always navigates to AddAccount page
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                FinAppBg.copy(alpha = 0.95f),
                                FinAppBg
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        viewModel.prepareAddAccount()
                        onNavigateToAddAccount()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinGreen
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        FinPullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadData(isRefresh = true) },
            lazyListState = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Header: Title on Left, Notification Bell on Right
                ScreenHeader(
                    title = "Accounts",
                    onNotificationClick = onNotificationClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (uiState.isLoading) {
                    ShimmerList(modifier = Modifier.fillMaxSize())
                } else {
                    val assetAccounts = remember(uiState.accounts) {
                        uiState.accounts.filter { it.category == AccountCategory.ASSET }
                    }
                    val liabilityAccounts = remember(uiState.accounts) {
                        uiState.accounts.filter { it.category == AccountCategory.LIABILITY }
                    }
                    val totalAssets = remember(assetAccounts, uiState.netWorthSummary) {
                        uiState.netWorthSummary?.totalAssets ?: assetAccounts.sumOf { it.balance }
                    }
                    val totalLiabilities = remember(liabilityAccounts, uiState.netWorthSummary) {
                        uiState.netWorthSummary?.totalLiabilities ?: liabilityAccounts.sumOf { it.balance }
                    }
                    val netWorth = remember(totalAssets, totalLiabilities, uiState.netWorthSummary) {
                        uiState.netWorthSummary?.netWorth ?: (totalAssets - totalLiabilities)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // 1. Net Worth Hero Banner
                        item {
                            NetWorthHeroCard(
                                netWorth = netWorth,
                                totalAssets = totalAssets,
                                totalLiabilities = totalLiabilities
                            )
                        }

                        // 2. Assets Section
                        item {
                            SectionHeader(
                                title = "Assets",
                                count = assetAccounts.size,
                                subtotalText = Formatters.formatCurrency(totalAssets, "INR"),
                                isLiability = false
                            )
                        }

                        if (assetAccounts.isEmpty()) {
                            item {
                                EmptyAccountsPlaceholder(message = "No asset accounts found. Tap \"+ Add Account\" below.")
                            }
                        } else {
                            items(assetAccounts, key = { it.id }) { account ->
                                AccountCardItem(
                                    account = account,
                                    onEdit = {
                                        viewModel.prepareEditAccount(account)
                                        onNavigateToAddAccount()
                                    },
                                    onDelete = { viewModel.requestDeleteAccount(account) }
                                )
                            }
                        }

                        // 3. Liabilities Section
                        item {
                            SectionHeader(
                                title = "Liabilities",
                                count = liabilityAccounts.size,
                                subtotalText = "-${Formatters.formatCurrency(totalLiabilities, "INR")}",
                                isLiability = true
                            )
                        }

                        if (liabilityAccounts.isEmpty()) {
                            item {
                                EmptyAccountsPlaceholder(message = "No liabilities or credit cards found.")
                            }
                        } else {
                            items(liabilityAccounts, key = { it.id }) { account ->
                                AccountCardItem(
                                    account = account,
                                    isLiability = true,
                                    onEdit = {
                                        viewModel.prepareEditAccount(account)
                                        onNavigateToAddAccount()
                                    },
                                    onDelete = { viewModel.requestDeleteAccount(account) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Net Worth Hero Card with deep slate gradient and dual split pills
 */
@Composable
private fun NetWorthHeroCard(
    netWorth: Double,
    totalAssets: Double,
    totalLiabilities: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF18253A), Color(0xFF0D1522))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Live Dot + Total Net Worth Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(FinGreenAccent, CircleShape)
                    )
                    Text(
                        text = "TOTAL NET WORTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Net Worth Balance
                Text(
                    text = Formatters.formatCurrency(netWorth, "INR"),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Split Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Assets Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x1F10B981), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "↗",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                                Text(
                                    text = "Assets",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF34D399)
                                )
                            }
                            Text(
                                text = "+${Formatters.formatCurrency(totalAssets, "INR")}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        }
                    }

                    // Liabilities Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x1FF43F5E), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "↘",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFB7185)
                                )
                                Text(
                                    text = "Liabilities",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFB7185)
                                )
                            }
                            Text(
                                text = "-${Formatters.formatCurrency(totalLiabilities, "INR")}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section Header with Title, Count badge, and Subtotal
 */
@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    subtotalText: String,
    isLiability: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = FinTextDark
            )
            Box(
                modifier = Modifier
                    .background(
                        if (isLiability) FinRedSoft else FinGreenSoft,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLiability) FinRedDark else FinGreen
                )
            }
        }

        Text(
            text = subtotalText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinTextMuted
        )
    }
}

/**
 * FinFlow Account Card Item
 */
@Composable
private fun AccountCardItem(
    account: Account,
    isLiability: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val iconBgColor = when (account.type) {
        AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> FinBlueSoft
        AccountType.CASH, AccountType.WALLET -> FinGreenSoft
        AccountType.CREDIT_CARD -> FinOrangeSoft
        AccountType.INVESTMENT -> FinPurpleSoft
        AccountType.LOAN -> FinRedSoft
        else -> Color(0xFFEEF2F6)
    }

    val iconColor = when (account.type) {
        AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> FinBlue
        AccountType.CASH, AccountType.WALLET -> FinGreen
        AccountType.CREDIT_CARD -> FinOrange
        AccountType.INVESTMENT -> FinPurple
        AccountType.LOAN -> FinRedDark
        else -> FinTextMuted
    }

    val iconVector = when (account.type) {
        AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> Icons.Default.AccountBalance
        AccountType.CASH, AccountType.WALLET -> Icons.Default.Payments
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
        AccountType.LOAN -> Icons.Default.AccountBalance
        else -> Icons.Default.Wallet
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.linearGradient(listOf(FinBorder, FinBorderLight))),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon Badge + Account Name & Subtitle
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(iconBgColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = account.type.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinTextDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val maskedNum = account.accountNumber?.takeLast(4)?.let { " • •••• $it" } ?: ""
                        Text(
                            text = "${account.institution ?: account.type.label}$maskedNum",
                            fontSize = 12.sp,
                            color = FinTextMuted,
                            maxLines = 1
                        )
                    }
                }

                // Right: Balance + Type Tag + 3-dots Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isLiability) "-${Formatters.formatCurrency(account.balance, "INR")}" else Formatters.formatCurrency(account.balance, "INR"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            color = if (isLiability) FinRedDark else FinTextDark
                        )
                        Text(
                            text = account.type.label,
                            fontSize = 11.sp,
                            color = FinTextMuted
                        )
                    }

                    // 3-dots overflow button
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = FinTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            shape = RoundedCornerShape(14.dp),
                            containerColor = Color.White
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Account", fontSize = 13.sp, color = FinTextDark) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = FinTextDark)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Account", fontSize = 13.sp, color = FinRedDark) },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = FinRedDark)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Optional Credit Limit & Available Credit Bar for Credit Cards
            if (account.type == AccountType.CREDIT_CARD && account.creditLimit != null && account.creditLimit > 0.0) {
                val limit = account.creditLimit
                val balance = account.balance
                val available = (limit - balance).coerceAtLeast(0.0)
                val usedFraction = (balance / limit).toFloat().coerceIn(0f, 1f)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = FinBorderLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Limit: ${Formatters.formatCurrency(limit, "INR")}",
                        fontSize = 11.sp,
                        color = FinTextMuted
                    )
                    Text(
                        text = "Available: ${Formatters.formatCurrency(available, "INR")}",
                        fontSize = 11.sp,
                        color = FinTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { usedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FinOrange,
                    trackColor = FinBorderLight
                )
            }
        }
    }
}

/**
 * Placeholder when no accounts exist in a group
 */
@Composable
private fun EmptyAccountsPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FinBorder, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = FinTextMuted
        )
    }
}

/**
 * Bottom Sheet Content for Creating & Editing Accounts
 */
@Composable
private fun AccountBottomSheetContent(
    uiState: AccountsUiState,
    onCategoryChanged: (AccountCategory) -> Unit,
    onTypeChanged: (AccountType) -> Unit,
    onNameChanged: (String) -> Unit,
    onInstitutionChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onBalanceChanged: (String) -> Unit,
    onCreditLimitChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    val isEditMode = uiState.editingAccountId != null

    val assetTypes = listOf(
        AccountType.CHECKING,
        AccountType.SAVINGS,
        AccountType.BANK,
        AccountType.CASH,
        AccountType.WALLET,
        AccountType.INVESTMENT
    )

    val liabilityTypes = listOf(
        AccountType.CREDIT_CARD,
        AccountType.LOAN
    )

    val currentTypes = if (uiState.newAccountCategory == AccountCategory.ASSET) assetTypes else liabilityTypes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sheet Header: Title & Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "Edit Account" else "New Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FinTextDark
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF0F3F6), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = FinTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Category Switcher: Asset vs Liability
        Text(
            text = "ACCOUNT CATEGORY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FinTextDark,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FinBorder, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            // Asset Button
            val isAsset = uiState.newAccountCategory == AccountCategory.ASSET
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAsset) FinGreenSoft else Color.Transparent)
                    .clickable { onCategoryChanged(AccountCategory.ASSET) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("↗", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isAsset) FinGreen else FinTextMuted)
                    Text("Asset", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isAsset) FinGreen else FinTextMuted)
                }
            }

            // Liability Button
            val isLiability = uiState.newAccountCategory == AccountCategory.LIABILITY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLiability) FinRedSoft else Color.Transparent)
                    .clickable { onCategoryChanged(AccountCategory.LIABILITY) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("↘", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isLiability) FinRedDark else FinTextMuted)
                    Text("Liability", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isLiability) FinRedDark else FinTextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector Chips
        Text(
            text = "ACCOUNT TYPE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FinTextDark,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentTypes.forEach { type ->
                val isSelected = uiState.newAccountType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isSelected) FinGreen else FinBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .background(if (isSelected) FinGreenSoft else Color.White)
                        .clickable { onTypeChanged(type) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = type.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) FinGreenDark else FinTextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Name Field
        OutlinedTextField(
            value = uiState.newAccountName,
            onValueChange = onNameChanged,
            label = { Text("Account Name *") },
            placeholder = { Text("e.g. HDFC Salary, Travel Card") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = FinTextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Financial Institution Field
        OutlinedTextField(
            value = uiState.newAccountInstitution,
            onValueChange = onInstitutionChanged,
            label = { Text("Institution / Provider (optional)") },
            placeholder = { Text("e.g. HDFC Bank, SBI, ICICI") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = FinTextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Account Number / Last 4 Digits
        OutlinedTextField(
            value = uiState.newAccountNumber,
            onValueChange = onAccountNumberChanged,
            label = { Text("Account Number / Last 4 Digits (optional)") },
            placeholder = { Text("e.g. 8492") },
            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = FinTextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Balance Field
        OutlinedTextField(
            value = uiState.newAccountBalance,
            onValueChange = onBalanceChanged,
            label = { Text("Current Balance *") },
            placeholder = { Text("0.00") },
            leadingIcon = { Text("₹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FinTextDark) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Credit Limit Field (if Credit Card or Loan)
        if (uiState.newAccountType == AccountType.CREDIT_CARD || uiState.newAccountType == AccountType.LOAN) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.newAccountCreditLimit,
                onValueChange = onCreditLimitChanged,
                label = { Text("Credit Limit / Sanctioned (optional)") },
                placeholder = { Text("e.g. 200000") },
                leadingIcon = { Text("₹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FinTextDark) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Save Button
        Button(
            onClick = onSave,
            enabled = !uiState.isSaving && uiState.newAccountName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinGreen)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isEditMode) "Save Changes" else "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
