package com.tracker.finance_app.presentation.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// FinFlow Reference Colors
private val FinFlowText = Color(0xFF18253A)
private val FinFlowMuted = Color(0xFF657188)
private val FinFlowBorder = Color(0xFFDCE1E8)
private val FinFlowBorderFocused = Color(0xFFAEB7C4)
private val FinFlowRed = Color(0xFFF04B4B)
private val FinFlowRedDark = Color(0xFFD93434)
private val FinFlowGreen = Color(0xFF087B3D)
private val FinFlowGreenDark = Color(0xFF08783B)
private val FinFlowAppBg = Color(0xFFFBFCFD)
private val FinFlowPageBg = Color(0xFFEEF2F5)
private val FinFlowFieldIcon = Color(0xFF1D2A3D)
private val FinFlowPlaceholder = Color(0xFF707B8D)
private val FinFlowChevron = Color(0xFF526075)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel,
    accounts: List<Account>,
    categories: List<Category>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    val parsedAmount = Formatters.parseAmountOrNull(amount)
    val isAmountValid = parsedAmount != null && parsedAmount > 0.0

    // Account is optional - null when not selected
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var transactionName by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var note by remember { mutableStateOf("") }

    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // Date Picker Dialog
    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    // Time Picker Dialog (24h)
    val timePickerDialog = remember(selectedTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
    }

    // Account Picker Bottom Sheet
    if (showAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAccountPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Select Account",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option 1: None / Clear selection -> sends null
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAccount = null
                                    showAccountPicker = false
                                },
                            color = if (selectedAccount == null) Color(0xFFEDF9F1) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = null,
                                    tint = if (selectedAccount == null) FinFlowGreen else FinFlowMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Select account (None)",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedAccount == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedAccount == null) FinFlowGreen else FinFlowText
                                    )
                                    Text(
                                        text = "Account will be set to null",
                                        fontSize = 11.sp,
                                        color = FinFlowMuted
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if (selectedAccount == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = FinFlowGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(accounts) { account ->
                        val isSelected = selectedAccount?.id == account.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAccount = account
                                    showAccountPicker = false
                                },
                            color = if (isSelected) Color(0xFFEDF9F1) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = if (isSelected) FinFlowGreen else FinFlowFieldIcon,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = account.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FinFlowGreen else FinFlowText
                                    )
                                    Text(
                                        text = "Balance: ₹${account.balance}",
                                        fontSize = 11.sp,
                                        color = FinFlowMuted
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = FinFlowGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        val subcategories = remember(categories, type) {
            val matchingGroups = categories.filter { it.type == type }.ifEmpty { categories }
            val nestedSubs = matchingGroups.flatMap { parent ->
                parent.children.map { child ->
                    child.copy(
                        type = parent.type,
                        groupName = parent.name,
                        iconName = child.iconName ?: parent.iconName,
                        colorHex = child.colorHex ?: parent.colorHex
                    )
                }
            }
            val directSubs = matchingGroups.filter { it.parentId != null }
            val allSubs = (nestedSubs + directSubs).distinctBy { it.id }

            if (allSubs.isNotEmpty()) {
                allSubs
            } else {
                // Fallback: If no subcategories exist yet, show parent groups
                matchingGroups
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Category",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinFlowText
                    )
                    Text(
                        text = "${subcategories.size} subcategories",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinFlowMuted
                    )
                }

                if (subcategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subcategories available",
                            fontSize = 14.sp,
                            color = FinFlowMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(subcategories, key = { it.id.ifBlank { it.name } }) { category ->
                            val isSelected = selectedCategory?.id == category.id
                            val catColor = remember(category.colorHex) {
                                parseCategoryColor(category.colorHex, FinFlowGreen)
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCategory = category
                                        showCategoryPicker = false
                                    },
                                color = if (isSelected) Color(0xFFEDF9F1) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(catColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category.iconName ?: "🏷️",
                                            fontSize = 17.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = category.name,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) FinFlowGreen else FinFlowText
                                        )
                                        if (category.groupName.isNotBlank()) {
                                            Text(
                                                text = category.groupName,
                                                fontSize = 11.sp,
                                                color = FinFlowMuted
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = FinFlowGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

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
            // Header: back-btn + title
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
                            onClick = onNavigateBack
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
                    text = "Add Transaction",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = FinFlowText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form container: gap = 18px
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Type Toggle
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
                        // Expense Button
                        val isExpense = type == TransactionType.EXPENSE
                        val expenseBg = if (isExpense) {
                            Brush.horizontalGradient(listOf(Color(0xFFFFF3F3), Color(0xFFFDE8E8)))
                        } else {
                            SolidColor(Color.Transparent)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(expenseBg)
                                .clickable {
                                    type = TransactionType.EXPENSE
                                    if (selectedCategory?.type == TransactionType.INCOME) {
                                        selectedCategory = null
                                    }
                                },
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
                                        .background(if (isExpense) FinFlowRed else Color(0xFFEEF1F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↘",
                                        color = if (isExpense) Color.White else Color(0xFF5B6575),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Expense",
                                    color = if (isExpense) FinFlowRedDark else Color(0xFF5D687B),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Income Button
                        val isIncome = type == TransactionType.INCOME
                        val incomeBg = if (isIncome) {
                            Brush.horizontalGradient(listOf(Color(0xFFEDF9F1), Color(0xFFE1F6E9)))
                        } else {
                            SolidColor(Color.Transparent)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(incomeBg)
                                .clickable {
                                    type = TransactionType.INCOME
                                    if (selectedCategory?.type == TransactionType.EXPENSE) {
                                        selectedCategory = null
                                    }
                                },
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
                                        .background(if (isIncome) FinFlowGreen else Color(0xFFEEF1F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↗",
                                        color = if (isIncome) Color.White else Color(0xFF5B6575),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Income",
                                    color = if (isIncome) FinFlowGreen else Color(0xFF5D687B),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Amount Field Group
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Amount ",
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

                    var isAmountFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isAmountFocused) FinFlowBorderFocused else FinFlowBorder,
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
                                value = amount,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                        amount = input
                                        statusMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isAmountFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (amount.isEmpty()) {
                                        Text(
                                            text = "Enter amount",
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

                // Transaction Name Field Group (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Transaction Name ",
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

                    var isTxNameFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isTxNameFocused) FinFlowBorderFocused else FinFlowBorder,
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
                                value = transactionName,
                                onValueChange = { input ->
                                    if (input.length <= 100) {
                                        transactionName = input
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                                    .onFocusChanged { isTxNameFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FinFlowText
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (transactionName.isEmpty()) {
                                        Text(
                                            text = "e.g., Grocery shopping, Salary",
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

                // Account Field Group (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Account ",
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(1.dp, FinFlowBorder, RoundedCornerShape(15.dp))
                            .clickable { showAccountPicker = true },
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
                                    text = "▣",
                                    fontSize = 20.sp,
                                    color = FinFlowFieldIcon
                                )
                            }
                            Text(
                                text = selectedAccount?.name ?: "Select account",
                                fontSize = 15.sp,
                                color = if (selectedAccount != null) FinFlowText else Color(0xFF596579),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedAccount != null) {
                                IconButton(
                                    onClick = { selectedAccount = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear account",
                                        tint = FinFlowMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = FinFlowChevron,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(22.dp)
                            )
                        }
                    }
                }

                // Category Field Group (Required)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Category ",
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(1.dp, FinFlowBorder, RoundedCornerShape(15.dp))
                            .clickable { showCategoryPicker = true },
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
                                if (selectedCategory != null && !selectedCategory?.iconName.isNullOrBlank()) {
                                    Text(
                                        text = selectedCategory?.iconName ?: "🏷️",
                                        fontSize = 20.sp
                                    )
                                } else {
                                    Text(
                                        text = "▦",
                                        fontSize = 20.sp,
                                        color = FinFlowFieldIcon
                                    )
                                }
                            }
                            if (selectedCategory != null) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedCategory!!.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FinFlowText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (selectedCategory!!.groupName.isNotBlank()) {
                                        Text(
                                            text = selectedCategory!!.groupName,
                                            fontSize = 11.sp,
                                            color = FinFlowMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Select category",
                                    fontSize = 15.sp,
                                    color = Color(0xFF596579),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = FinFlowChevron,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(22.dp)
                            )
                        }
                    }
                }

                // Date & Time Row (2 equal columns)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Date Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row {
                            Text(
                                text = "Date ",
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White)
                                .border(1.dp, FinFlowBorder, RoundedCornerShape(15.dp))
                                .clickable { datePickerDialog.show() },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.width(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "▣",
                                        fontSize = 19.sp,
                                        color = FinFlowFieldIcon
                                    )
                                }
                                Text(
                                    text = selectedDate.format(dateFormatter),
                                    fontSize = 14.sp,
                                    color = FinFlowText
                                )
                            }
                        }
                    }

                    // Time Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row {
                            Text(
                                text = "Time ",
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White)
                                .border(1.dp, FinFlowBorder, RoundedCornerShape(15.dp))
                                .clickable { timePickerDialog.show() },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.width(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "◷",
                                        fontSize = 19.sp,
                                        color = FinFlowFieldIcon
                                    )
                                }
                                Text(
                                    text = selectedTime.format(timeFormatter),
                                    fontSize = 14.sp,
                                    color = FinFlowText
                                )
                            }
                        }
                    }
                }

                // Note Field Group (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = "Note ",
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

                    var isNoteFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(116.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isNoteFocused) FinFlowBorderFocused else FinFlowBorder,
                                shape = RoundedCornerShape(15.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 45.dp, bottom = 26.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .padding(top = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "▤",
                                    fontSize = 19.sp,
                                    color = FinFlowFieldIcon
                                )
                            }
                            BasicTextField(
                                value = note,
                                onValueChange = {
                                    if (it.length <= 200) {
                                        note = it
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp)
                                    .onFocusChanged { isNoteFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = FinFlowText,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = SolidColor(FinFlowGreen),
                                decorationBox = { innerTextField ->
                                    if (note.isEmpty()) {
                                        Text(
                                            text = "Add a note...",
                                            fontSize = 14.sp,
                                            color = FinFlowPlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        // Character Counter at bottom right
                        Text(
                            text = "${note.length}/200",
                            fontSize = 12.sp,
                            color = Color(0xFF647187),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 10.dp)
                        )
                    }
                }

                // Save Transaction Button
                val canSave = isAmountValid && selectedCategory != null
                Button(
                    onClick = {
                        if (!canSave) {
                            statusMessage = "Please enter an amount and select a category."
                            return@Button
                        }
                        val amountVal = parsedAmount ?: return@Button
                        val cat = selectedCategory ?: return@Button
                        isSaving = true
                        statusMessage = null

                        // If user hasn't selected an account, send as null!
                        val finalTxName = transactionName.trim().ifBlank { "New Transaction" }
                        viewModel.addTransaction(
                            accountId = selectedAccount?.id, // null when no account selected
                            amount = amountVal,
                            type = type,
                            categoryId = cat.id,
                            categoryName = cat.name,
                            transactionName = finalTxName,
                            notes = note.trim().ifEmpty { null },
                            date = selectedDate,
                            time = selectedTime,
                            onSuccess = {
                                isSaving = false
                                onNavigateBack()
                            }
                        )
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
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save Transaction",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Message text below button
                val displayMessage = statusMessage ?: uiState.error
                if (!displayMessage.isNullOrBlank()) {
                    Text(
                        text = displayMessage,
                        fontSize = 12.sp,
                        color = if (displayMessage.contains("error", ignoreCase = true) || !canSave) {
                            FinFlowRedDark
                        } else {
                            FinFlowGreen
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun parseCategoryColor(hex: String?, fallback: Color = FinFlowGreen): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

