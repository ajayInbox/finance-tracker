package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SplitAmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    currency: String = "₹",
    integerFontSize: TextUnit = 36.sp,
    decimalFontSize: TextUnit = 20.sp
) {
    val formatted = String.format(Locale.getDefault(), "%.2f", amount)
    val parts = formatted.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) ".${parts[1]}" else ".00"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = currency,
            fontSize = integerFontSize * 0.7f,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = integerPart,
            fontSize = integerFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = decimalPart,
            fontSize = decimalFontSize,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
