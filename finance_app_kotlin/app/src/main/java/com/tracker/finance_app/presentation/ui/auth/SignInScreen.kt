package com.tracker.finance_app.presentation.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinFlowHeroArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radial Glow
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEDF9F1),
                            Color(0xFFF7FBF8),
                            Color(0x00F7FBF8)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Floating Sparkle Dots
        Canvas(modifier = Modifier.size(240.dp, 200.dp)) {
            val sparkleColor = Color(0xFFB4E4C2)
            drawCircle(color = sparkleColor, radius = 4.dp.toPx(), center = Offset(size.width * 0.26f, size.height * 0.28f))
            drawCircle(color = sparkleColor, radius = 3.dp.toPx(), center = Offset(size.width * 0.74f, size.height * 0.35f))
            drawCircle(color = sparkleColor, radius = 2.5f.dp.toPx(), center = Offset(size.width * 0.80f, size.height * 0.78f))
        }

        // Wallet Illustration Container
        Box(
            modifier = Modifier.size(width = 170.dp, height = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Slanted Credit Card
            Box(
                modifier = Modifier
                    .offset(x = (-4).dp, y = (-18).dp)
                    .rotate(-10f)
                    .size(width = 114.dp, height = 68.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp), spotColor = Color(0x20000000))
                    .border(
                        width = 6.dp,
                        color = Color(0xFF2B2D31),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .background(
                        Color(0xFFF5F5F1),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF73C78F))
                )
            }

            // Wallet Body (Front Green Flap)
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 10.dp)
                    .size(width = 145.dp, height = 94.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = FinFlowGreenPrimary.copy(alpha = 0.35f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF24B85D), Color(0xFF089343)),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Specular highlight stripe
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 16.dp)
                        .size(width = 46.dp, height = 14.dp)
                        .background(
                            Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(10.dp)
                        )
                )

                // Wallet Clasp Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 18.dp, end = 12.dp)
                        .size(18.dp)
                        .background(Color(0xFFD9EDDF), CircleShape)
                        .border(3.dp, Color(0xFF8ED1A5), CircleShape)
                )
            }
        }
    }
}

@Composable
fun FinFlowBottomArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Hill A
            val hillAPath = Path().apply {
                moveTo(-width * 0.15f, height)
                cubicTo(
                    width * 0.05f, height * 0.40f,
                    width * 0.45f, height * 0.45f,
                    width * 0.70f, height
                )
                close()
            }
            drawPath(path = hillAPath, color = Color(0xFFEDF9F1))

            // Hill B
            val hillBPath = Path().apply {
                moveTo(width * 0.35f, height)
                cubicTo(
                    width * 0.60f, height * 0.35f,
                    width * 0.90f, height * 0.30f,
                    width * 1.15f, height
                )
                close()
            }
            drawPath(path = hillBPath, color = Color(0xFFDFF4E7))

            // Growth Plant
            val plantBaseX = width * 0.14f
            val plantBaseY = height * 0.88f

            drawLine(
                color = Color(0xFF2F9E59),
                start = Offset(plantBaseX, plantBaseY),
                end = Offset(plantBaseX, plantBaseY - 50.dp.toPx()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            val leafColor = Color(0xFF43A85F)
            drawOval(
                color = leafColor,
                topLeft = Offset(plantBaseX - 16.dp.toPx(), plantBaseY - 26.dp.toPx()),
                size = Size(16.dp.toPx(), 9.dp.toPx())
            )
            drawOval(
                color = leafColor,
                topLeft = Offset(plantBaseX + 2.dp.toPx(), plantBaseY - 32.dp.toPx()),
                size = Size(16.dp.toPx(), 9.dp.toPx())
            )
            drawOval(
                color = leafColor,
                topLeft = Offset(plantBaseX - 14.dp.toPx(), plantBaseY - 44.dp.toPx()),
                size = Size(14.dp.toPx(), 8.dp.toPx())
            )
            drawOval(
                color = leafColor,
                topLeft = Offset(plantBaseX + 2.dp.toPx(), plantBaseY - 48.dp.toPx()),
                size = Size(14.dp.toPx(), 8.dp.toPx())
            )

            // Growth Chart
            val chartX = width * 0.70f
            val chartBaseY = height * 0.88f
            val barWidth = 14.dp.toPx()
            val barSpacing = 7.dp.toPx()
            val barColor = Color(0xFF51B97A)
            val heights = listOf(20.dp.toPx(), 34.dp.toPx(), 48.dp.toPx(), 62.dp.toPx())

            heights.forEachIndexed { index, barHeight ->
                val x = chartX + index * (barWidth + barSpacing)
                val y = chartBaseY - barHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }

            // Trend line
            val chartLinePath = Path().apply {
                moveTo(chartX, chartBaseY - 22.dp.toPx())
                cubicTo(
                    chartX + 20.dp.toPx(), chartBaseY - 32.dp.toPx(),
                    chartX + 45.dp.toPx(), chartBaseY - 48.dp.toPx(),
                    chartX + 70.dp.toPx(), chartBaseY - 70.dp.toPx()
                )
            }
            drawPath(
                path = chartLinePath,
                color = Color(0xFF68BF84),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Home Indicator Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .size(width = 122.dp, height = 5.dp)
                .background(Color(0xFF111827), CircleShape)
        )
    }
}

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onSignInSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinFlowBackgroundPage)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(containerColor = FinFlowCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, end = 28.dp, top = 32.dp, bottom = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Brand Header
                    FinFlowBrandHeader()

                    // 2. Hero Art
                    FinFlowHeroArt(modifier = Modifier.padding(top = 10.dp))

                    // 3. Heading
                    Text(
                        text = "Welcome back!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.2).sp,
                        color = FinFlowTextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sign in to continue to your FinFlow account",
                        fontSize = 16.sp,
                        color = FinFlowTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 4. Form Inputs
                    FinFlowInputField(
                        value = uiState.emailInput,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = "Email or Phone number",
                        leadingIcon = { FinFlowEmailIcon() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FinFlowInputField(
                        value = uiState.passwordInput,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        placeholder = "Password",
                        leadingIcon = { FinFlowLockIcon() },
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.signIn()
                            }
                        )
                    )

                    // Forgot Password Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = FinFlowGreenPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { viewModel.sendPasswordReset() }
                        )
                    }

                    // Error Message Display
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.error?.let { err ->
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = err,
                                    color = Color(0xFFDC2626),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 5. Sign In Primary Button
                    FinFlowPrimaryButton(
                        text = "Sign In",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.signIn()
                        },
                        isLoading = uiState.isLoading
                    )

                    // 6. Divider
                    FinFlowDivider(modifier = Modifier.padding(vertical = 22.dp))

                    // 7. Google SSO Button
                    FinFlowGoogleButton(
                        text = "Continue with Google",
                        onClick = { /* Google Auth */ }
                    )

                    // 8. Sign Up Navigation Link
                    Row(
                        modifier = Modifier
                            .padding(top = 22.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don’t have an account? ",
                            color = Color(0xFF5F6878),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Sign Up",
                            color = FinFlowGreenBrandText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }

                    // 9. Bottom Art Illustration
                    FinFlowBottomArt(modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
