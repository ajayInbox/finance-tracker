package com.tracker.finance_app.presentation.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FinFlow Brand Colors
val FinFlowGreenPrimary = Color(0xFF079447)
val FinFlowGreenPrimaryDark = Color(0xFF087D3D)
val FinFlowGreenGradientTop = Color(0xFF12A453)
val FinFlowGreenGradientBottom = Color(0xFF078E45)
val FinFlowGreenBrandText = Color(0xFF168849)
val FinFlowTextDark = Color(0xFF111827)
val FinFlowTextMuted = Color(0xFF697386)
val FinFlowBorderColor = Color(0xFFE1E6EB)
val FinFlowBorderFocused = Color(0xFF83CA9D)
val FinFlowBackgroundPage = Color(0xFFEEF2F7)
val FinFlowCardBackground = Color(0xFFFFFFFF)

@Composable
fun FinFlowBrandHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Green circular emblem with tilted F
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = FinFlowGreenPrimary.copy(alpha = 0.25f))
                .clip(CircleShape)
                .background(FinFlowGreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "F",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.graphicsLayer {
                    rotationZ = -5f
                }
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "FinFlow",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.1).sp,
            color = FinFlowGreenBrandText
        )
    }
}

@Composable
fun FinFlowUserIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp, 24.dp)) {
        val strokeW = 2.2.dp.toPx()
        val centerX = size.width / 2f

        // Head circle
        drawCircle(
            color = FinFlowGreenPrimary,
            radius = 5.5.dp.toPx(),
            center = Offset(centerX, 6.5.dp.toPx()),
            style = Stroke(width = strokeW)
        )

        // Body arc / shoulders
        val shoulderWidth = 20.dp.toPx()
        val shoulderHeight = 11.dp.toPx()
        val shoulderLeft = centerX - shoulderWidth / 2f
        val shoulderTop = size.height - shoulderHeight

        val bodyPath = Path().apply {
            moveTo(shoulderLeft, size.height)
            cubicTo(
                shoulderLeft, shoulderTop,
                shoulderLeft + shoulderWidth, shoulderTop,
                shoulderLeft + shoulderWidth, size.height
            )
        }
        drawPath(
            path = bodyPath,
            color = FinFlowGreenPrimary,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FinFlowEmailIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp, 18.dp)) {
        val strokeW = 2.4.dp.toPx()
        val corner = 4.dp.toPx()

        // Envelope Box Outline
        drawRoundRect(
            color = FinFlowGreenPrimary,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = strokeW)
        )
        // Envelope Flap Lines
        val midX = size.width / 2f
        val midY = size.height * 0.62f
        drawLine(
            color = FinFlowGreenPrimary,
            start = Offset(0f, 0f),
            end = Offset(midX, midY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = FinFlowGreenPrimary,
            start = Offset(size.width, 0f),
            end = Offset(midX, midY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun FinFlowLockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp, 22.dp)) {
        val strokeW = 2.4.dp.toPx()
        val bodyTop = size.height * 0.38f
        val bodyHeight = size.height - bodyTop

        // Lock body
        drawRoundRect(
            color = FinFlowGreenPrimary,
            topLeft = Offset(0f, bodyTop),
            size = Size(size.width, bodyHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = strokeW)
        )
        // Lock shackle (loop)
        val shackleW = size.width * 0.58f
        val shackleLeft = (size.width - shackleW) / 2f
        val shacklePath = Path().apply {
            moveTo(shackleLeft, bodyTop)
            lineTo(shackleLeft, size.height * 0.2f)
            cubicTo(
                shackleLeft, 0f,
                shackleLeft + shackleW, 0f,
                shackleLeft + shackleW, size.height * 0.2f
            )
            lineTo(shackleLeft + shackleW, bodyTop)
        }
        drawPath(
            path = shacklePath,
            color = FinFlowGreenPrimary,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        // Keyhole
        drawCircle(
            color = FinFlowGreenPrimary,
            radius = 1.8.dp.toPx(),
            center = Offset(size.width / 2f, bodyTop + bodyHeight * 0.42f)
        )
    }
}

@Composable
fun FinFlowInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val currentVisualTransformation = when {
        isPassword && !isPasswordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FinFlowCardBackground)
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                color = if (isFocused) FinFlowBorderFocused else FinFlowBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusRequester.requestFocus() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF8791A3),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = FinFlowTextDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    ),
                    singleLine = true,
                    visualTransformation = currentVisualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions
                )
            }

            if (isPassword) {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF6B7381),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FinFlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(15.dp),
                spotColor = FinFlowGreenPrimary.copy(alpha = 0.28f)
            ),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFFA5D6B4)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FinFlowGreenGradientTop, FinFlowGreenGradientBottom)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }
        }
    }
}

@Composable
fun FinFlowGoogleButton(
    text: String = "Continue with Google",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp), spotColor = Color(0x12000000)),
        shape = RoundedCornerShape(15.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FinFlowBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google 'G' 4-color emblem
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeW = 3.8.dp.toPx()
                val radius = size.minDimension / 2f - strokeW / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Blue arc
                drawArc(
                    color = Color(0xFF4285F4),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                // Green arc
                drawArc(
                    color = Color(0xFF34A853),
                    startAngle = 45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                // Yellow arc
                drawArc(
                    color = Color(0xFFFBBC05),
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                // Red arc
                drawArc(
                    color = Color(0xFFEA4335),
                    startAngle = 225f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                // Crossbar
                drawLine(
                    color = Color(0xFF4285F4),
                    start = Offset(center.x, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                color = Color(0xFF273245),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FinFlowDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDFE4E9), thickness = 1.dp)
        Text(
            text = "or",
            color = Color(0xFF5F6878),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDFE4E9), thickness = 1.dp)
    }
}

@Composable
fun FinFlowTermsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.Top
    ) {
        // Custom Green Checkbox
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(6.dp), spotColor = FinFlowGreenPrimary.copy(alpha = 0.2f))
                .background(
                    if (checked) Color(0xFF0F9A4B) else Color.White,
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = if (checked) 0.dp else 1.5.dp,
                    color = if (checked) Color.Transparent else FinFlowBorderColor,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Canvas(modifier = Modifier.size(13.dp, 10.dp)) {
                    val path = Path().apply {
                        moveTo(1.dp.toPx(), 5.dp.toPx())
                        lineTo(4.5.dp.toPx(), 9.dp.toPx())
                        lineTo(12.dp.toPx(), 1.dp.toPx())
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        val annotatedText = buildAnnotatedString {
            append("I agree to the ")
            pushStringAnnotation(tag = "TERMS", annotation = "terms")
            withStyle(style = SpanStyle(color = Color(0xFF078D44), fontWeight = FontWeight.SemiBold)) {
                append("Terms of Service")
            }
            pop()
            append(" and ")
            pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
            withStyle(style = SpanStyle(color = Color(0xFF078D44), fontWeight = FontWeight.SemiBold)) {
                append("Privacy Policy")
            }
            pop()
        }

        Text(
            text = annotatedText,
            color = Color(0xFF687387),
            fontSize = 14.5.sp,
            lineHeight = 21.sp
        )
    }
}
