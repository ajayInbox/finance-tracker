package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    selectedColor: Color?,
    onColorSelected: (Color) -> Unit
) {
    val presetColors = listOf(
        Color(0xFFEF4444), // Red
        Color(0xFFF97316), // Orange
        Color(0xFFF59E0B), // Yellow
        Color(0xFF10B981), // Green
        Color(0xFF3B82F6), // Blue
        Color(0xFF8B5CF6)  // Purple
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presetColors.forEach { color ->
            val isSelected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (isSelected) {
                            Modifier
                                .border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                .shadow(8.dp, CircleShape, ambientColor = color, spotColor = color)
                        } else Modifier
                    )
            )
        }
    }
}
