package com.tracker.finance_app.presentation.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var statementDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("15") }
    var creditLimit by remember { mutableStateOf("") }

    val accountTypes = AccountType.values().take(8) // Bank, Savings, Checking, Cash, Wallet, Credit Card, Loan, Investment

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.createAccount(onSuccess = onNavigateBack) }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = uiState.newAccountBalance,
                onValueChange = { viewModel.onBalanceChanged(it) },
                label = { Text("Balance") },
                prefix = { Text("₹") },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Account Type", style = MaterialTheme.typography.titleMedium)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(accountTypes) { type ->
                    val isSelected = uiState.newAccountType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onTypeChanged(type)
                            if (type == AccountType.CREDIT_CARD || type == AccountType.LOAN) {
                                viewModel.onCategoryChanged(AccountCategory.LIABILITY)
                            } else {
                                viewModel.onCategoryChanged(AccountCategory.ASSET)
                            }
                        },
                        label = { Text(type.label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = uiState.newAccountName,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.newAccountInstitution,
                onValueChange = { viewModel.onInstitutionChanged(it) },
                label = { Text("Institution (e.g. HDFC, SBI)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            AnimatedVisibility(visible = uiState.newAccountType == AccountType.CREDIT_CARD) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        label = { Text("Credit Limit") },
                        prefix = { Text("₹") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = statementDay,
                            onValueChange = { statementDay = it },
                            label = { Text("Statement Day (1-31)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dueDay,
                            onValueChange = { dueDay = it },
                            label = { Text("Due Day (1-31)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.createAccount(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Account")
                }
            }
        }
    }
}
