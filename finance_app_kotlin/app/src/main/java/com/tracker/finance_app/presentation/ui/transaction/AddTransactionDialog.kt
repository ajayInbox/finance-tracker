package com.tracker.finance_app.presentation.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.core.util.Formatters
import com.tracker.finance_app.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: TransactionsViewModel,
    accounts: List<com.tracker.finance_app.domain.model.Account>,
    categories: List<com.tracker.finance_app.domain.model.Category>,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val parsedAmount = Formatters.parseAmountOrNull(amountText)
    val isAmountValid = parsedAmount != null && parsedAmount > 0.0
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountText.isNotBlank() && !isAmountValid,
                    supportingText = {
                        if (amountText.isNotBlank() && !isAmountValid) {
                            Text("Enter a valid amount greater than 0")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Transaction Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("Expense") }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("Income") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = parsedAmount
                    if (description.isNotBlank() && amount != null && amount > 0) {
                        viewModel.addTransaction(
                            accountId = selectedAccountId,
                            amount = amount,
                            type = selectedType,
                            description = description,
                            categoryId = selectedCategoryId,
                            onSuccess = onDismiss
                        )
                    }
                },
                enabled = description.isNotBlank() && isAmountValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
