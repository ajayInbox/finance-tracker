package com.tracker.finance_app.presentation.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: TransactionFilter,
    onApplyFilter: (TransactionFilter) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedType by remember { mutableStateOf(currentFilter.transactionType) }
    var selectedPeriod by remember { mutableStateOf(currentFilter.timePeriod) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Filter Transactions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Transaction Type
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val typeOptions = listOf(null to "All") + TransactionType.entries.map { it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                typeOptions.forEachIndexed { index, (type, label) ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        shape = SegmentedButtonDefaults.itemShape(index, typeOptions.size)
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Period
            Text(
                text = "Time Period",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TimePeriod.entries.forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        shape = SegmentedButtonDefaults.itemShape(index, TimePeriod.entries.size)
                    ) {
                        val label = when (period) {
                            TimePeriod.ALL -> "All"
                            TimePeriod.TODAY -> "Today"
                            TimePeriod.YESTERDAY -> "Yesterday"
                            TimePeriod.THIS_WEEK -> "Week"
                            TimePeriod.THIS_MONTH -> "Month"
                        }
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onClearFilter()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = {
                        onApplyFilter(TransactionFilter(selectedType, selectedPeriod))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
