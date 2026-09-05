package com.tracker.finance_app.presentation.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType

// FinFlow Reference Colors
private val FinFlowText = Color(0xFF18253A)
private val FinFlowMuted = Color(0xFF657188)
private val FinFlowBorder = Color(0xFFDCE1E8)
private val FinFlowBorderFocused = Color(0xFFAEB7C4)
private val FinFlowRed = Color(0xFFF04B4B)
private val FinFlowRedDark = Color(0xFFD93434)
private val FinFlowGreen = Color(0xFF087B3D)
private val FinFlowGreenDark = Color(0xFF08783B)
private val FinFlowGreenSoft = Color(0xFFEDF9F1)
private val FinFlowRedSoft = Color(0xFFFFF3F3)
private val FinFlowAppBg = Color(0xFFFBFCFD)
private val FinFlowPageBg = Color(0xFFEEF2F5)
private val FinFlowFieldIcon = Color(0xFF1D2A3D)
private val FinFlowPlaceholder = Color(0xFF707B8D)

@Composable
fun AddAccountScreen(
    viewModel: AccountsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = uiState.editingAccountId != null
    val handleBack = {
        viewModel.closeBottomSheet()
        onNavigateBack()
    }

    var statementDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("15") }

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

    // Outer Page Background Container
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinFlowPageBg)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        // App Frame Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .fillMaxHeight()
                .background(FinFlowAppBg)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp)
        ) {
            // Header: back button + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (-8).dp)
                        .size(38.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = handleBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color(0xFF526075),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isEditMode) "Edit Account" else "Add Account",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = FinFlowText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Category Toggle: Asset vs Liability (Matching FinFlow Segmented Toggle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, FinFlowBorder, RoundedCornerShape(16.dp))
                        .padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Asset Button
                        val isAsset = uiState.newAccountCategory == AccountCategory.ASSET
                        val assetBg = if (isAsset) {
                            Brush.horizontalGradient(listOf(Color(0xFFEDF9F1), Color(0xFFE1F6E9)))
                        } else {
                            SolidColor(Color.Transparent)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(assetBg)
                                .clickable { viewModel.onCategoryChanged(AccountCategory.ASSET) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isAsset) FinFlowGreen else Color(0xFFEEF1F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↗",
                                        color = if (isAsset) Color.White else Color(0xFF5B6575),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Asset",
                                    color = if (isAsset) FinFlowGreen else Color(0xFF5D687B),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Liability Button
                        val isLiability = uiState.newAccountCategory == AccountCategory.LIABILITY
                        val liabilityBg = if (isLiability) {
                            Brush.horizontalGradient(listOf(Color(0xFFFFF3F3), Color(0xFFFDE8E8)))
                        } else {
                            SolidColor(Color.Transparent)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(liabilityBg)
                                .clickable { viewModel.onCategoryChanged(AccountCategory.LIABILITY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isLiability) FinFlowRed else Color(0xFFEEF1F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↘",
                                        color = if (isLiability) Color.White else Color(0xFF5B6575),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Liability",
                                    color = if (isLiability) FinFlowRedDark else Color(0xFF5D687B),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Balance Field Group (Required)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Opening Balance ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText
                        )
                        Text(
                            text = "*",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowRedDark
                        )
                    }

                    var isBalanceFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isBalanceFocused) FinFlowBorderFocused else FinFlowBorder,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FinFlowFieldIcon
                                )
                            }
                            BasicTextField(
                                value = uiState.newAccountBalance,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                        viewModel.onBalanceChanged(input)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isBalanceFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (uiState.newAccountBalance.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FinFlowPlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Account Type Chips Group
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Account Type ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText
                        )
                        Text(
                            text = "*",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowRedDark
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentTypes.forEach { type ->
                            val isSelected = uiState.newAccountType == type
                            val icon = when (type) {
                                AccountType.BANK, AccountType.SAVINGS, AccountType.CHECKING -> "🏦"
                                AccountType.CASH, AccountType.WALLET -> "💵"
                                AccountType.CREDIT_CARD -> "💳"
                                AccountType.INVESTMENT -> "📈"
                                AccountType.LOAN -> "🏛️"
                                else -> "▣"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(if (isSelected) FinFlowGreenSoft else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) FinFlowGreen else FinFlowBorder,
                                        RoundedCornerShape(13.dp)
                                    )
                                    .clickable { viewModel.onTypeChanged(type) }
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = icon, fontSize = 15.sp)
                                    Text(
                                        text = type.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FinFlowGreenDark else FinFlowText
                                    )
                                }
                            }
                        }
                    }
                }

                // Account Name Field Group (Required)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Account Name ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText
                        )
                        Text(
                            text = "*",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowRedDark
                        )
                    }

                    var isNameFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isNameFocused) FinFlowBorderFocused else FinFlowBorder,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✎",
                                    fontSize = 19.sp,
                                    color = FinFlowFieldIcon
                                )
                            }
                            BasicTextField(
                                value = uiState.newAccountName,
                                onValueChange = { viewModel.onNameChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isNameFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (uiState.newAccountName.isEmpty()) {
                                        Text(
                                            text = "e.g., HDFC Salary, Travel Card",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FinFlowPlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Institution Field Group (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Institution / Provider ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText
                        )
                        Text(
                            text = "(optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = FinFlowMuted
                        )
                    }

                    var isInstFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isInstFocused) FinFlowBorderFocused else FinFlowBorder,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🏛️",
                                    fontSize = 18.sp
                                )
                            }
                            BasicTextField(
                                value = uiState.newAccountInstitution,
                                onValueChange = { viewModel.onInstitutionChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isInstFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (uiState.newAccountInstitution.isEmpty()) {
                                        Text(
                                            text = "e.g., HDFC Bank, SBI, ICICI",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FinFlowPlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Account Number Field Group (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Account Number ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowText
                        )
                        Text(
                            text = "(last 4 digits optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = FinFlowMuted
                        )
                    }

                    var isAccNumFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isAccNumFocused) FinFlowBorderFocused else FinFlowBorder,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinFlowFieldIcon
                                )
                            }
                            BasicTextField(
                                value = uiState.newAccountNumber,
                                onValueChange = { viewModel.onAccountNumberChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isAccNumFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (uiState.newAccountNumber.isEmpty()) {
                                        Text(
                                            text = "e.g., 8492",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FinFlowPlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Credit Card Specific Fields
                AnimatedVisibility(visible = uiState.newAccountType == AccountType.CREDIT_CARD || uiState.newAccountType == AccountType.LOAN) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        // Credit Limit Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(
                                    text = "Credit Limit / Sanctioned ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FinFlowText
                                )
                                Text(
                                    text = "(optional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowMuted
                                )
                            }

                            var isLimitFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isLimitFocused) FinFlowBorderFocused else FinFlowBorder,
                                        shape = RoundedCornerShape(15.dp)
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.width(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "₹",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FinFlowFieldIcon
                                        )
                                    }
                                    BasicTextField(
                                        value = uiState.newAccountCreditLimit,
                                        onValueChange = { viewModel.onCreditLimitChanged(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp)
                                            .onFocusChanged { isLimitFocused = it.isFocused },
                                        textStyle = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FinFlowText
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        cursorBrush = SolidColor(FinFlowGreen),
                                        decorationBox = { innerTextField ->
                                            if (uiState.newAccountCreditLimit.isEmpty()) {
                                                Text(
                                                    text = "e.g., 200000",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = FinFlowPlaceholder
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }

                        // Statement Day & Due Day Row
                        if (uiState.newAccountType == AccountType.CREDIT_CARD) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Statement Day
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Statement Day",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FinFlowText
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .border(1.dp, FinFlowBorder, RoundedCornerShape(14.dp))
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = statementDay,
                                            onValueChange = { statementDay = it },
                                            textStyle = TextStyle(fontSize = 15.sp, color = FinFlowText),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            cursorBrush = SolidColor(FinFlowGreen),
                                            decorationBox = { inner ->
                                                if (statementDay.isEmpty()) Text("1", color = FinFlowPlaceholder, fontSize = 14.sp)
                                                inner()
                                            }
                                        )
                                    }
                                }

                                // Due Day
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Due Day",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FinFlowText
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .border(1.dp, FinFlowBorder, RoundedCornerShape(14.dp))
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = dueDay,
                                            onValueChange = { dueDay = it },
                                            textStyle = TextStyle(fontSize = 15.sp, color = FinFlowText),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            cursorBrush = SolidColor(FinFlowGreen),
                                            decorationBox = { inner ->
                                                if (dueDay.isEmpty()) Text("15", color = FinFlowPlaceholder, fontSize = 14.sp)
                                                inner()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Save Account Button
                val canSave = uiState.newAccountName.isNotBlank()
                Button(
                    onClick = {
                        if (!canSave) return@Button
                        viewModel.saveAccount(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(top = 4.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0x21087C3F)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFA6C9B2)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (canSave) {
                                        listOf(Color(0xFF08783B), Color(0xFF087C3F))
                                    } else {
                                        listOf(Color(0xFF78BE92), Color(0xFF5A9F74))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "Save Changes" else "Save Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Error Message Display
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        fontSize = 13.sp,
                        color = FinFlowRedDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
