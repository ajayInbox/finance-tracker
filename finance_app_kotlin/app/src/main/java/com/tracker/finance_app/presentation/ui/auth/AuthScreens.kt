package com.tracker.finance_app.presentation.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tracker.finance_app.presentation.components.GradientButton

@Composable
fun AnimatedRadialGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 3)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF020617)),
                center = center,
                radius = size.width * radiusRatio
            )
        )
        // Add subtle glow patches
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF13EC5B).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.2f),
                radius = size.width * 0.6f * radiusRatio
            )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun GoogleSsoButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(24.dp)) {
                // Simplified "G" Logo
                val sweepAngle = 270f
                val startAngle = -45f
                drawArc(
                    color = Color(0xFF4285F4),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
                // Crossbar
                drawLine(
                    color = Color(0xFF4285F4),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", color = Color.Black)
        }
    }
}

@Composable
fun FocusGlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF13EC5B) else Color.White.copy(alpha = 0.3f),
        label = "borderColor"
    )
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PasswordStrengthMeter(password: String) {
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    val score = listOf(hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    
    val (strengthText, strengthColor) = when {
        password.isEmpty() -> "None" to Color.Gray
        score <= 2 -> "Weak" to Color.Red
        score <= 3 -> "Fair" to Color(0xFFF59E0B)
        score == 4 -> "Good" to Color(0xFF3B82F6)
        else -> "Strong" to Color(0xFF10B981)
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Password Strength", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            Text(strengthText, color = strengthColor, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                val segmentColor = if (i <= score) strengthColor else Color.White.copy(alpha = 0.1f)
                Box(modifier = Modifier.weight(1f).height(4.dp).background(segmentColor, RoundedCornerShape(2.dp)))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Checklist
        val checks = listOf(
            "8+ Chars" to hasMinLength,
            "Upper" to hasUpper,
            "Lower" to hasLower,
            "Digit" to hasDigit,
            "Special" to hasSpecial
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            checks.forEach { (label, met) ->
                Text(
                    text = label,
                    color = if (met) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isAuthenticated) onSignInSuccess()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedRadialGradientBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            GlassCard {
                FocusGlowTextField(
                    value = uiState.emailInput,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))
                FocusGlowTextField(
                    value = uiState.passwordInput,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Password",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                GradientButton(text = "Sign In", onClick = { viewModel.signIn() }, enabled = !uiState.isLoading)
                Spacer(modifier = Modifier.height(16.dp))
                GoogleSsoButton(onClick = { /* TODO */ })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Sign Up", color = Color.White)
            }
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
    if (uiState.isAuthenticated) onSignUpSuccess()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedRadialGradientBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Join Finance Tracker",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            GlassCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FocusGlowTextField(value = uiState.firstNameInput, onValueChange = { viewModel.onFirstNameChanged(it) }, label = "First Name")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FocusGlowTextField(value = uiState.lastNameInput, onValueChange = { viewModel.onLastNameChanged(it) }, label = "Last Name")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FocusGlowTextField(
                    value = uiState.emailInput,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(12.dp))
                FocusGlowTextField(
                    value = uiState.passwordInput,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Password",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                PasswordStrengthMeter(password = uiState.passwordInput)
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                GradientButton(text = "Sign Up", onClick = { viewModel.signUp() }, enabled = !uiState.isLoading)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onNavigateToSignIn) {
                Text("Already have an account? Sign In", color = Color.White)
            }
        }
    }
}
