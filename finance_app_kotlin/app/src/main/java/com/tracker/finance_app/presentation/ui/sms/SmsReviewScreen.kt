package com.tracker.finance_app.presentation.ui.sms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.finance_app.core.util.hasSmsPermissions
import com.tracker.finance_app.core.util.rememberSmsPermissionRequest
import com.tracker.finance_app.data.sync.SyncUiStatus
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.presentation.components.ShimmerList
import com.tracker.finance_app.presentation.components.SplitAmountText
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(hasSmsPermissions(context)) }
    var showRangeSheet by remember { mutableStateOf(false) }
    var selectedDraftIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Per-draft pickers: the draft whose account/category sheet is open
    var accountPickerDraft by remember { mutableStateOf<TransactionDraft?>(null) }
    var categoryPickerDraft by remember { mutableStateOf<TransactionDraft?>(null) }

    val requestPermissions = rememberSmsPermissionRequest(
        onPermissionsGranted = { hasPermissions = true },
        onPermissionsDenied = { hasPermissions = false }
    )

    // Match Flutter behavior: ask for SMS permission when the screen opens
    LaunchedEffect(Unit) { requestPermissions() }

    val isSyncing = syncStatus is SyncUiStatus.Syncing

    // Transient messages/errors surfaced as snackbars
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = uiState.message ?: uiState.error
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showRangeSheet) {
        SyncRangeBottomSheet(
            hasPreviousSync = lastSyncTimestamp > 0,
            lastSyncTimestamp = lastSyncTimestamp,
            onDismiss = { showRangeSheet = false },
            onRangeSelected = { rangeMillis ->
                showRangeSheet = false
                viewModel.startSync(rangeMillis)
            }
        )
    }

    accountPickerDraft?.let { draft ->
        AccountPickerSheet(
            accounts = accounts,
            onDismiss = { accountPickerDraft = null },
            onSelect = { account ->
                viewModel.assignAccount(draft, account)
                accountPickerDraft = null
            }
        )
    }

    categoryPickerDraft?.let { draft ->
        CategoryPickerSheet(
            categories = categories.filter { it.type == draft.type },
            onDismiss = { categoryPickerDraft = null },
            onSelect = { category ->
                viewModel.assignCategory(draft, category)
                categoryPickerDraft = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Drafts") },
                actions = {
                    IconButton(onClick = { viewModel.loadDrafts(true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(visible = selectedDraftIds.isNotEmpty()) {
                BottomAppBar(
                    actions = {
                        TextButton(onClick = {
                            if (selectedDraftIds.size == uiState.drafts.size) {
                                selectedDraftIds = emptySet()
                            } else {
                                selectedDraftIds = uiState.drafts.map { it.id }.toSet()
                            }
                        }) {
                            Text(if (selectedDraftIds.size == uiState.drafts.size) "Deselect All" else "Select All")
                        }
                    },
                    floatingActionButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FloatingActionButton(
                                onClick = {
                                    viewModel.deleteDrafts(uiState.drafts.filter { it.id in selectedDraftIds })
                                    selectedDraftIds = emptySet()
                                },
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Selected")
                            }
                            FloatingActionButton(
                                onClick = {
                                    val selected = uiState.drafts.filter { it.id in selectedDraftIds }
                                    viewModel.confirmDrafts(selected)
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
                uiState.error != null && uiState.drafts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.error ?: "An error occurred", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDrafts() }) { Text("Retry") }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SyncHeroCard(
                                isSyncing = isSyncing,
                                syncStatus = syncStatus,
                                draftCount = uiState.drafts.size,
                                hasPermissions = hasPermissions,
                                onRequestPermissions = requestPermissions,
                                onRunSync = {
                                    if (hasPermissions) showRangeSheet = true else requestPermissions()
                                }
                            )
                        }

                        item {
                            AutoSyncCard(
                                enabled = uiState.autoSyncEnabled,
                                onToggle = { viewModel.toggleAutoSync(it) }
                            )
                        }

                        if (uiState.drafts.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Inbox,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No pending drafts", style = MaterialTheme.typography.titleMedium)
                                    if (uiState.approvedCount > 0) {
                                        Text(
                                            text = "${uiState.approvedCount} transaction(s) approved",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "PENDING REVIEW · ${uiState.drafts.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(uiState.drafts, key = { it.id.ifBlank { it.rawSms } }) { draft ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.deleteDrafts(listOf(draft))
                                            selectedDraftIds -= draft.id
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
                                                contentDescription = "Delete draft",
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                ) {
                                    val isSelected = draft.id in selectedDraftIds
                                    DraftCard(
                                        draft = draft,
                                        isSelected = isSelected,
                                        isConfirming = uiState.isConfirming,
                                        onSelect = {
                                            if (isSelected) selectedDraftIds -= draft.id else selectedDraftIds += draft.id
                                        },
                                        onPickAccount = { accountPickerDraft = draft },
                                        onPickCategory = { categoryPickerDraft = draft },
                                        onConfirm = { viewModel.confirmDrafts(listOf(draft)) }
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

@Composable
private fun SyncHeroCard(
    isSyncing: Boolean,
    syncStatus: SyncUiStatus,
    draftCount: Int,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onRunSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(primary.copy(alpha = 0.9f), primary.copy(alpha = 0.7f))))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when {
                                isSyncing -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                syncStatus is SyncUiStatus.Error -> Icon(
                                    Icons.Default.ErrorOutline, contentDescription = null, tint = Color.White
                                )
                                else -> Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                !hasPermissions -> "Permissions required"
                                isSyncing -> "Sync in progress..."
                                syncStatus is SyncUiStatus.Error -> "Sync failed"
                                else -> "Periodic inbox sync"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = when {
                                !hasPermissions -> "Grant SMS access to scan your inbox"
                                isSyncing -> "Scanning your SMS inbox"
                                syncStatus is SyncUiStatus.Error -> (syncStatus as SyncUiStatus.Error).message
                                draftCount > 0 -> "$draftCount drafts waiting for review"
                                else -> "Scan your bank SMS for new transactions"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Source: Inbox scan", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                            labelColor = Color.White
                        ),
                        border = null
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Review first", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF34D399).copy(alpha = 0.25f),
                            labelColor = Color.White
                        ),
                        border = null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRunSync,
                    enabled = !isSyncing && hasPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSyncing) "Syncing..." else "Run sync now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncRangeBottomSheet(
    hasPreviousSync: Boolean,
    lastSyncTimestamp: Long,
    onDismiss: () -> Unit,
    onRangeSelected: (Long) -> Unit
) {
    val now = System.currentTimeMillis()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Choose sync range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (hasPreviousSync) {
                ListItem(
                    headlineContent = { Text("Continue from last sync", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(timeAgo(lastSyncTimestamp)) },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                    trailingContent = { Text("FASTEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onRangeSelected(lastSyncTimestamp) }
                )
            }
            ListItem(
                headlineContent = { Text("Last 7 days", fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingContent = { Text("QUICK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onRangeSelected(now - TimeUnit.DAYS.toMillis(7)) }
            )
            ListItem(
                headlineContent = { Text("Last 30 days", fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingContent = { Text("RECOMMENDED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { onRangeSelected(now - TimeUnit.DAYS.toMillis(30)) }
            )
            ListItem(
                headlineContent = { Text("Last 90 days", fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingContent = { Text("DEEPER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onRangeSelected(now - TimeUnit.DAYS.toMillis(90)) }
            )
            ListItem(
                headlineContent = { Text("Cancel") },
                leadingContent = { Icon(Icons.Default.Close, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}

@Composable
private fun AutoSyncCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Auto sync every 6 hours") },
            supportingContent = { Text(if (enabled) "Background inbox scan is on" else "Background inbox scan is off") },
            leadingContent = { Icon(Icons.Default.Autorenew, contentDescription = null) },
            trailingContent = {
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        )
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        else -> "$days day${if (days > 1) "s" else ""} ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DraftCard(
    draft: TransactionDraft,
    isSelected: Boolean,
    isConfirming: Boolean,
    onSelect: () -> Unit,
    onPickAccount: () -> Unit,
    onPickCategory: () -> Unit,
    onConfirm: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                        draft.occurredAt?.let {
                            Text(
                                text = formatDateLabel(it),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                SplitAmountText(amount = draft.amount)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account / category assignment pills (like the Flutter draft card)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onPickAccount,
                    label = {
                        Text(
                            draft.accountName ?: "Choose account",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (draft.accountId != null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                AssistChip(
                    onClick = onPickCategory,
                    label = {
                        Text(
                            draft.categoryName ?: "Choose category",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (draft.categoryId != null)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
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
                val ready = draft.isReadyForConfirm()
                Button(onClick = onConfirm, enabled = !isConfirming && !isSelected) {
                    if (isConfirming) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(if (ready) "Confirm ${draft.type.name.lowercase()}" else "Confirm")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerSheet(
    accounts: List<com.tracker.finance_app.domain.model.Account>,
    onDismiss: () -> Unit,
    onSelect: (com.tracker.finance_app.domain.model.Account) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Choose account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (accounts.isEmpty()) {
                Text(
                    "No accounts yet — add one first",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            accounts.forEach { account ->
                ListItem(
                    headlineContent = { Text(account.name) },
                    supportingContent = { Text(account.type.label) },
                    leadingContent = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    modifier = Modifier.clickable { onSelect(account) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    categories: List<com.tracker.finance_app.domain.model.Category>,
    onDismiss: () -> Unit,
    onSelect: (com.tracker.finance_app.domain.model.Category) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Choose category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (categories.isEmpty()) {
                Text(
                    "No categories for this transaction type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            categories.forEach { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    supportingContent = { Text(category.groupName) },
                    leadingContent = { Icon(Icons.Default.Label, contentDescription = null) },
                    modifier = Modifier.clickable { onSelect(category) }
                )
            }
        }
    }
}

private fun formatDateLabel(iso: String): String = try {
    val instant = java.time.Instant.parse(iso)
    val local = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
    "${local.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${local.dayOfMonth}, ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    iso
}
