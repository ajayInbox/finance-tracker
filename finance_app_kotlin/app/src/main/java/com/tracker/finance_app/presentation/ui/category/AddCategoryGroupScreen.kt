package com.tracker.finance_app.presentation.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.presentation.components.ColorPicker
import com.tracker.finance_app.presentation.components.IconPickerGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryGroupScreen(
    onNavigateBack: () -> Unit,
    onSaveGroup: (name: String, icon: String, colorHex: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<ImageVector>(Icons.Default.Category) }
    var selectedColor by remember { mutableStateOf(Color(0xFF10B981)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Category Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Live Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = selectedIcon, contentDescription = null, tint = selectedColor)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = groupName.ifBlank { "Group Name Preview" },
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Select Color", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Select Icon", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            IconPickerGrid(
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val colorHex = String.format("#%06X", (0xFFFFFF and selectedColor.value.toInt()))
                    onSaveGroup(groupName, selectedIcon.name, colorHex)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = groupName.isNotBlank()
            ) {
                Text("Save Category Group")
            }
        }
    }
}
