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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinFlowSignUpHeroArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(205.dp),
        contentAlignment = Alignment.Center
    ) {
        // Orbit Outer Ring Glow
        Box(
            modifier = Modifier
                .size(168.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEEF9F1),
                            Color(0xFFF8FCF9),
                            Color(0x00F8FCF9)
                        )
                    ),
                    shape = CircleShape
                )
                .border(width = 12.dp, color = Color(0x1A73C78F), shape = CircleShape)
        )

        // Inner White Disk
        Box(
            modifier = Modifier
                .size(118.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x1430503A))
                .background(Color.White.copy(alpha = 0.96f), CircleShape)
        )

        // Person Profile Bubble
        Box(
            modifier = Modifier
                .size(85.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = FinFlowGreenPrimary.copy(alpha = 0.3f))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF31AF62), Color(0xFF0C8F46))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Head
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Shoulders / Torso
                Box(
                    modifier = Modifier
                        .size(width = 43.dp, height = 24.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
                        )
                )
            }
        }

        // Floating Stars
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "✦",
                color = Color(0xFF45B873),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-95).dp, y = (-42).dp)
            )

            Text(
                text = "✧",
                color = Color(0xFF45B873),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 95.dp, y = (-26).dp)
            )
        }

        // Botanical Sprig (bottom-right)
        Canvas(
            modifier = Modifier
                .size(70.dp, 80.dp)
                .align(Alignment.Center)
                .offset(x = 88.dp, y = 38.dp)
                .rotate(35f)
        ) {
            val stemColor = Color(0xFF79BA8B)
            val leafColor = Color(0xFF92D2A5)

            // Stem
            drawLine(
                color = stemColor,
                start = Offset(30.dp.toPx(), size.height),
                end = Offset(30.dp.toPx(), 5.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Leaf 1
            drawOval(
                color = leafColor,
                topLeft = Offset(0f, 34.dp.toPx()),
                size = Size(26.dp.toPx(), 14.dp.toPx())
            )
            // Leaf 2
            drawOval(
                color = leafColor,
                topLeft = Offset(30.dp.toPx(), 18.dp.toPx()),
                size = Size(26.dp.toPx(), 14.dp.toPx())
            )
            // Leaf 3
            drawOval(
                color = leafColor,
                topLeft = Offset(4.dp.toPx(), 6.dp.toPx()),
                size = Size(24.dp.toPx(), 13.dp.toPx())
            )
        }
    }
}

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onSignUpSuccess()
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
                        .padding(start = 28.dp, end = 28.dp, top = 32.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Brand Header
                    FinFlowBrandHeader()

                    // 2. Hero Art
                    FinFlowSignUpHeroArt(modifier = Modifier.padding(top = 8.dp))

                    // 3. Heading
                    Text(
                        text = "Create your account",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.2).sp,
                        color = FinFlowTextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start your journey to financial clarity",
                        fontSize = 16.sp,
                        color = FinFlowTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // 4. Form Inputs
                    // Full Name Input
                    FinFlowInputField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            validationError = null
                            val parts = it.trim().split(" ", limit = 2)
                            viewModel.onFirstNameChanged(parts.getOrElse(0) { "" })
                            viewModel.onLastNameChanged(parts.getOrElse(1) { "" })
                        },
                        placeholder = "Full Name",
                        leadingIcon = { FinFlowUserIcon() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Input
                    FinFlowInputField(
                        value = uiState.emailInput,
                        onValueChange = {
                            viewModel.onEmailChanged(it)
                            validationError = null
                        },
                        placeholder = "Email",
                        leadingIcon = { FinFlowEmailIcon() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Create Password Input
                    FinFlowInputField(
                        value = uiState.passwordInput,
                        onValueChange = {
                            viewModel.onPasswordChanged(it)
                            validationError = null
                        },
                        placeholder = "Create Password",
                        leadingIcon = { FinFlowLockIcon() },
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirm Password Input
                    FinFlowInputField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            validationError = null
                        },
                        placeholder = "Confirm Password",
                        leadingIcon = { FinFlowLockIcon() },
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (confirmPassword != uiState.passwordInput) {
                                    validationError = "Passwords do not match"
                                } else if (!termsAccepted) {
                                    validationError = "Please accept the Terms of Service"
                                } else {
                                    viewModel.signUp()
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Terms and Conditions Checkbox Row
                    FinFlowTermsRow(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it }
                    )

                    // Error Message Display
                    val activeError = validationError ?: uiState.error
                    AnimatedVisibility(
                        visible = activeError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        activeError?.let { err ->
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6. Sign Up Primary Button
                    FinFlowPrimaryButton(
                        text = "Sign Up",
                        onClick = {
                            focusManager.clearFocus()
                            if (uiState.passwordInput != confirmPassword) {
                                validationError = "Passwords do not match"
                            } else if (!termsAccepted) {
                                validationError = "Please accept the Terms of Service"
                            } else {
                                viewModel.signUp()
                            }
                        },
                        isLoading = uiState.isLoading
                    )

                    // 7. Divider
                    FinFlowDivider(modifier = Modifier.padding(vertical = 22.dp))

                    // 8. Social Sign Up Button
                    FinFlowGoogleButton(
                        text = "Sign up with Google",
                        onClick = { /* Google Sign Up */ }
                    )

                    // 9. Sign In Navigation Link
                    Row(
                        modifier = Modifier
                            .padding(top = 22.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = Color(0xFF5F6878),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Sign In",
                            color = FinFlowGreenBrandText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToSignIn() }
                        )
                    }

                    // 10. Home Indicator Pill
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .size(width = 122.dp, height = 5.dp)
                            .background(Color(0xFF111827), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
