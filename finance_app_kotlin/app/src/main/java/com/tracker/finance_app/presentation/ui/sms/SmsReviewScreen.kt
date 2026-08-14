package com.tracker.finance_app.presentation.ui.sms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.presentation.components.ShimmerList
import com.tracker.finance_app.presentation.components.SplitAmountText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedDraftIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Drafts") },
                actions = {
                    IconButton(onClick = { viewModel.loadDrafts(true) }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync SMS")
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = selectedDraftIds.isNotEmpty()) {
                BottomAppBar(
                    actions = {
                        TextButton(onClick = { 
                            if (selectedDraftIds.size == uiState.drafts.size) {
                                selectedDraftIds = emptySet()
                            } else {
                                selectedDraftIds = uiState.drafts.map { it.rawSms }.toSet()
                            }
                        }) {
                            Text(if (selectedDraftIds.size == uiState.drafts.size) "Deselect All" else "Select All")
                        }
                    },
                    floatingActionButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FloatingActionButton(
                                onClick = { 
                                    uiState.drafts.filter { it.rawSms in selectedDraftIds }.forEach { viewModel.rejectDraft(it) }
                                    selectedDraftIds = emptySet()
                                },
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                            }
                            FloatingActionButton(
                                onClick = { 
                                    uiState.drafts.filter { it.rawSms in selectedDraftIds }.forEach { viewModel.approveDraft(it, "default") }
                                    selectedDraftIds = emptySet()
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm Selected")
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadDrafts(true) },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    ShimmerList(modifier = Modifier.padding(16.dp))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.error ?: "An error occurred", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDrafts() }) { Text("Retry") }
                    }
                }
                uiState.drafts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No pending drafts",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.approvedCount > 0) {
                            Text(
                                text = "${uiState.approvedCount} transaction(s) approved",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Sync Active", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Text("${uiState.drafts.size} drafts waiting for review", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                        
                        items(uiState.drafts, key = { it.rawSms }) { draft ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.rejectDraft(draft)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )
                            
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val color = MaterialTheme.colorScheme.errorContainer
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, shape = CardDefaults.shape)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Reject",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                val isSelected = selectedDraftIds.contains(draft.rawSms)
                                DraftCard(
                                    draft = draft,
                                    isSelected = isSelected,
                                    onSelect = { 
                                        if (isSelected) selectedDraftIds -= draft.rawSms else selectedDraftIds += draft.rawSms 
                                    },
                                    onApprove = { viewModel.approveDraft(draft, "default") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftCard(
    draft: TransactionDraft,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = draft.merchant ?: "Unknown Merchant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        draft.suggestedCategory?.let { cat ->
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                SplitAmountText(
                    amount = draft.amount
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Raw SMS in quote bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "\"${draft.rawSms}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onApprove) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}
