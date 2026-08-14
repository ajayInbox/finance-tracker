package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerGrid(
    selectedIcon: ImageVector?,
    onIconSelected: (ImageVector) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val icons = listOf(
        Icons.Default.Home, Icons.Default.ShoppingCart, Icons.Default.Restaurant,
        Icons.Default.Commute, Icons.Default.LocalHospital, Icons.Default.School,
        Icons.Default.FitnessCenter, Icons.Default.Pets, Icons.Default.Flight,
        Icons.Default.DirectionsCar, Icons.Default.ElectricBolt, Icons.Default.WaterDrop,
        Icons.Default.PhoneAndroid, Icons.Default.Wifi, Icons.Default.Checkroom,
        Icons.Default.Movie, Icons.Default.MusicNote, Icons.Default.VideogameAsset,
        Icons.Default.CardGiftcard, Icons.Default.Favorite
    )
    
    val filteredIcons = icons.filter { 
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Icons") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredIcons) { icon ->
                IconButton(
                    onClick = { onIconSelected(icon) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                        tint = if (selectedIcon == icon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
