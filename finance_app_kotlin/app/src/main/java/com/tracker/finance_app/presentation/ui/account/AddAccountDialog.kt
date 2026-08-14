package com.tracker.finance_app.presentation.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    viewModel: AccountsViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.newAccountName,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.newAccountInstitution,
                    onValueChange = { viewModel.onInstitutionChanged(it) },
                    label = { Text("Bank / Institution") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.newAccountBalance,
                    onValueChange = { viewModel.onBalanceChanged(it) },
                    label = { Text("Initial Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Account Category", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.newAccountCategory == AccountCategory.ASSET,
                        onClick = { viewModel.onCategoryChanged(AccountCategory.ASSET) },
                        label = { Text("Asset") }
                    )
                    FilterChip(
                        selected = uiState.newAccountCategory == AccountCategory.LIABILITY,
                        onClick = { viewModel.onCategoryChanged(AccountCategory.LIABILITY) },
                        label = { Text("Liability") }
                    )
                }

                Text("Account Type", style = MaterialTheme.typography.labelLarge)
                ScrollableTabRow(selectedTabIndex = uiState.newAccountType.ordinal) {
                    AccountType.values().forEach { type ->
                        Tab(
                            selected = uiState.newAccountType == type,
                            onClick = { viewModel.onTypeChanged(type) },
                            text = { Text(type.name) }
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.createAccount(onSuccess = onDismiss) },
                enabled = !uiState.isSaving && uiState.newAccountName.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
