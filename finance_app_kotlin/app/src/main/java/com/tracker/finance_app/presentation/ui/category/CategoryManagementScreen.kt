package com.tracker.finance_app.presentation.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    modifier: Modifier = Modifier,
    onNavigateToAddGroup: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isFabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Categories") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddGroup,
                expanded = isFabExpanded,
                icon = { Icon(Icons.Default.Add, "Add Group") },
                text = { Text("New Group") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val groupedCategories = uiState.categories.groupBy { it.groupName }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedCategories.forEach { (groupName, categories) ->
                        item(key = "group_$groupName") {
                            CategoryGroupCard(
                                groupName = groupName,
                                categories = categories,
                                onDelete = { viewModel.deleteCategory(it) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // FAB padding
                }
            }
        }
    }
}

@Composable
fun CategoryGroupCard(
    groupName: String,
    categories: List<Category>,
    onDelete: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "ExpandIcon")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotationState)
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    categories.forEach { cat ->
                        CategoryItem(category = cat, onDelete = { onDelete(cat.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit
) {
    val hexColor = category.colorHex ?: "#10B981"
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Color(0xFF10B981)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical color marker bar: 4x32dp
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 32.dp)
                .background(color = color, shape = MaterialTheme.shapes.small)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = category.type.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (!category.isSystem) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
