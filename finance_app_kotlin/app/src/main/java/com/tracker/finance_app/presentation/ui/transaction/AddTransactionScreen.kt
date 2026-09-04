package com.tracker.finance_app.presentation.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel,
    accounts: List<Account>,
    categories: List<Category>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    val parsedAmount = Formatters.parseAmountOrNull(amount)
    val isAmountValid = parsedAmount != null && parsedAmount > 0.0
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }

    val activeColor = when (type) {
        TransactionType.INCOME -> Color(0xFF10B981) // Emerald
        TransactionType.EXPENSE -> Color(0xFFF44336) // Red
        TransactionType.TRANSFER -> Color(0xFF2196F3) // Blue
    }

    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            LazyColumn {
                items(categories) { category ->
                    ListItem(
                        headlineContent = { Text(category.name) },
                        modifier = Modifier.clickable {
                            selectedCategory = category
                            showCategoryPicker = false
                        }
                    )
                }
            }
        }
    }

    if (showAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            LazyColumn {
                items(accounts) { account ->
                    ListItem(
                        headlineContent = { Text(account.name) },
                        supportingContent = { Text("Balance: ₹${account.balance}") },
                        modifier = Modifier.clickable {
                            selectedAccount = account
                            showAccountPicker = false
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val account = selectedAccount
                        if (account != null && isAmountValid) {
                            viewModel.addTransaction(
                                accountId = account.id,
                                amount = parsedAmount ?: return@IconButton,
                                type = type,
                                description = description,
                                categoryId = selectedCategory?.id,
                                onSuccess = onNavigateBack
                            )
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Segmented Button for Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER).forEach { t ->
                    val isSelected = type == t
                    val animatedColor by animateColorAsState(targetValue = if (isSelected) activeColor else Color.Transparent)
                    val textColor by animateColorAsState(targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(animatedColor)
                            .clickable { type = t },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t.name.lowercase().replaceFirstChar { it.uppercase() }, color = textColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Big Amount Input
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Amount", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    prefix = { Text("₹", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = activeColor) },
                    textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = activeColor),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    isError = amount.isNotBlank() && !isAmountValid,
                    supportingText = {
                        if (amount.isNotBlank() && !isAmountValid) {
                            Text("Enter a valid amount greater than 0")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Selectors
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ListItem(
                    headlineContent = { Text(selectedAccount?.name ?: "Select Account") },
                    leadingContent = {
                        Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { showAccountPicker = true }
                )

                ListItem(
                    headlineContent = { Text(selectedCategory?.name ?: "Select Category") },
                    leadingContent = {
                        Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { showCategoryPicker = true }
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val account = selectedAccount
                    if (account != null && isAmountValid) {
                        viewModel.addTransaction(
                            accountId = account.id,
                            amount = parsedAmount ?: return@Button,
                            type = type,
                            description = description,
                            categoryId = selectedCategory?.id,
                            onSuccess = onNavigateBack
                        )
                    }
                },
                enabled = selectedAccount != null && isAmountValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor)
            ) {
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
