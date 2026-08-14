package com.tracker.finance_app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.tracker.finance_app.core.util.Formatters

@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var animatedValue by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(targetValue) {
        animatedValue = targetValue.toFloat()
    }
    
    val currentValue by animateFloatAsState(
        targetValue = animatedValue,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "AnimatedCounter"
    )
    
    Text(
        text = Formatters.formatCurrency(currentValue.toDouble()),
        modifier = modifier,
        style = style,
        color = color
    )
}
