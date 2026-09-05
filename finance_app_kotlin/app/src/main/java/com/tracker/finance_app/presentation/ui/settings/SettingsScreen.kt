package com.tracker.finance_app.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tracker.finance_app.presentation.theme.ThemeMode
import com.tracker.finance_app.presentation.theme.ThemeViewModel
import com.valentinilk.shimmer.shimmer
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.graphics.Color
import com.tracker.finance_app.presentation.components.ScreenHeader
import java.util.stream.Collectors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSmsReview: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    // Delete account confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text("Delete Account?") },
            text = { Text("This will permanently delete your account and all data. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAccount { onSignOut() } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFBFCFD),
        snackbarHost = {
            uiState.message?.let {
                Snackbar { Text(it) }
            }
            uiState.error?.let {
                Snackbar(containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Consistent Screen Header
            ScreenHeader(
                title = "Settings",
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            // User profile card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.isLoading) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .shimmer(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Box(modifier = Modifier.width(120.dp).height(20.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.width(180.dp).height(16.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = uiState.userProfile?.let { profile ->
                                    val parts = profile.name?.trim()?.split("\\s+".toRegex())?.filter { it.isNotEmpty() }

                                    val nameInitials = when {
                                        parts.isNullOrEmpty() -> ""
                                        parts.size == 1 -> parts[0].take(1)
                                        else -> "${parts.first().first()}${parts.last().first()}"
                                    }

                                    nameInitials.uppercase().ifEmpty {
                                        profile.email.firstOrNull()?.uppercase()
                                    }
                                } ?: "?"
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val displayName = uiState.userProfile?.name?: "User"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    if (uiState.isProMember) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "PRO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                uiState.userProfile?.email?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Navigation items
            item {
                Text("Manage", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToCategories) {
                    ListItem(
                        headlineContent = { Text("Manage Categories") },
                        supportingContent = { Text("Add, edit or remove categories") },
                        leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToSmsReview) {
                    ListItem(
                        headlineContent = { Text("Review SMS Drafts") },
                        supportingContent = { Text("Approve or reject auto-parsed transactions") },
                        leadingContent = { Icon(Icons.Default.MarkEmailUnread, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }

            // Preferences
            item {
                Text("Preferences", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Automatic SMS Sync") },
                            supportingContent = { Text("Background sync every 6 hours") },
                            leadingContent = { Icon(Icons.Default.Sync, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = uiState.autoSyncEnabled,
                                    onCheckedChange = { viewModel.toggleAutoSync(it) }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = { Text("Biometric Login") },
                            supportingContent = { Text("Use Face ID or Fingerprint") },
                            leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                            trailingContent = {
                                Switch(checked = uiState.biometricEnabled, onCheckedChange = { viewModel.toggleBiometric(it) })
                            }
                        )
                    }
                }
            }

            // Appearance & Formatting
            item {
                Text("Appearance & Formatting", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                            ) {
                                Text("Light")
                            }
                            SegmentedButton(
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                            ) {
                                Text("Dark")
                            }
                            SegmentedButton(
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                            ) {
                                Text("System")
                            }
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    var currencyExpanded by remember { mutableStateOf(false) }
                    val currencies = listOf("INR", "USD", "EUR", "GBP")
                    
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            headlineContent = { Text("Currency") },
                            supportingContent = { Text("Display transactions in this currency") },
                            leadingContent = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.selectedCurrency,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                                }
                            }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            currencies.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        viewModel.setCurrency(selectionOption)
                                        currencyExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }
            }

            // Account actions
            item {
                Text("Account", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.sendPasswordReset() }) {
                    ListItem(
                        headlineContent = { Text("Reset Password") },
                        supportingContent = { Text("Send a password reset email") },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.signOut {
                            onSignOut()
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = { Text("Sign Out") },
                        supportingContent = { Text("Log out of your account") },
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.showDeleteConfirmation() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    ListItem(
                        headlineContent = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                        supportingContent = { Text("Permanently delete your account and data") },
                        leadingContent = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}
}
