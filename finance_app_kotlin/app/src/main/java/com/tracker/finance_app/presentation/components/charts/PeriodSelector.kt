package com.tracker.finance_app.presentation.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf("1W", "1M", "3M", "6M", "1Y")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        periods.forEach { period ->
            val selected = selectedPeriod == period
            FilterChip(
                selected = selected,
                onClick = { onPeriodSelected(period) },
                label = { Text(period) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White,
                    containerColor = Color.Transparent
                ),
                elevation = if (selected) FilterChipDefaults.filterChipElevation(elevation = 4.dp) else null,
                border = null
            )
        }
    }
}
