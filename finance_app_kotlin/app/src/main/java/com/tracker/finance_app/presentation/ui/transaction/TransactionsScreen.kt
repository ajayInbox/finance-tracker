package com.tracker.finance_app.presentation.ui.transaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import com.tracker.finance_app.presentation.components.FinPullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.presentation.components.ScreenHeader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isScrollingDown = remember {
        derivedStateOf { 
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50
        }
    }
    val fabScale by animateFloatAsState(
        targetValue = if (isScrollingDown.value) 0f else 1f,
        label = "fabScale"
    )
    var showFilterSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load transactions whenever entering the screen
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    LaunchedEffect(uiState.lastDeletedTransaction) {
        uiState.lastDeletedTransaction?.let { deleted ->
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete(deleted)
            }
            viewModel.onDeleteMessageShown()
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = uiState.filter,
            onApplyFilter = {
                viewModel.applyFilter(it)
                showFilterSheet = false
            },
            onClearFilter = {
                viewModel.clearFilters()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFBFCFD)
    ) { innerPadding ->
        FinPullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadTransactions(forceRefresh = true) },
            lazyListState = listState,
            modifier = modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(14.dp))

                // Consistent Screen Header
                ScreenHeader(
                    title = "Transactions",
                    onNotificationClick = onNotificationClick,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.filter.transactionType == null,
                        onClick = { viewModel.applyFilter(uiState.filter.copy(transactionType = null)) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF13EC5B))
                    )
                    FilterChip(
                        selected = uiState.filter.transactionType == TransactionType.INCOME,
                        onClick = { viewModel.applyFilter(uiState.filter.copy(transactionType = TransactionType.INCOME)) },
                        label = { Text("Income") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF13EC5B))
                    )
                    FilterChip(
                        selected = uiState.filter.transactionType == TransactionType.EXPENSE,
                        onClick = { viewModel.applyFilter(uiState.filter.copy(transactionType = TransactionType.EXPENSE)) },
                        label = { Text("Expense") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF13EC5B))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = uiState.isFilterActive,
                        onClick = { showFilterSheet = true },
                        label = { Icon(Icons.Default.Tune, contentDescription = "Filter") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF13EC5B))
                    )
                }

                // Main List
                if (uiState.isLoading && uiState.transactions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.groupedTransactions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.groupedTransactions.forEach { (dateLabel, transactions) ->
                            stickyHeader {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            items(transactions, key = { it.id }) { item ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.deleteTransaction(item.id)
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                    positionalThreshold = { it * 0.75f }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                            else -> Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                                .background(color, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.onError,
                                                    modifier = Modifier.padding(end = 16.dp)
                                                )
                                            }
                                        }
                                    },
                                    enableDismissFromStartToEnd = false
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        val isIncome = item.type == TransactionType.INCOME
                                        ListItem(
                                            leadingContent = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = item.categoryName?.take(1) ?: "?",
                                                        color = if (isIncome) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            },
                                            headlineContent = { Text(item.description, fontWeight = FontWeight.SemiBold) },
                                            supportingContent = {
                                                val timeText = try {
                                                    LocalDateTime.parse(item.timestamp).format(DateTimeFormatter.ofPattern("hh:mm a"))
                                                } catch (e: Exception) {
                                                    ""
                                                }
                                                Text("${item.categoryName ?: "Uncategorized"} • $timeText")
                                            },
                                            trailingContent = {
                                                Text(
                                                    text = "${if (isIncome) "+" else "-"}₹${String.format("%.2f", item.amount)}",
                                                    color = if (isIncome) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
