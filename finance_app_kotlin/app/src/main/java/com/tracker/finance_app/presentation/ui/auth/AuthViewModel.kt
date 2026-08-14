package com.tracker.finance_app.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val firstNameInput: String = "",
    val lastNameInput: String = "",
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthTokenFlow().collect { token ->
                _uiState.update { it.copy(isAuthenticated = !token.isNullOrBlank()) }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(emailInput = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password, error = null) }
    }

    fun onFirstNameChanged(firstName: String) {
        _uiState.update { it.copy(firstNameInput = firstName, error = null) }
    }

    fun onLastNameChanged(lastName: String) {
        _uiState.update { it.copy(lastNameInput = lastName, error = null) }
    }

    fun signIn() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message ?: "Sign in failed") }
                }
        }
    }

    fun signUp() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in required fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUp(
                email = email,
                password = password,
                name = _uiState.value.firstNameInput.ifBlank { null },
                lastName = _uiState.value.lastNameInput.ifBlank { null }
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            }.onFailure { exc ->
                _uiState.update { it.copy(isLoading = false, error = exc.message ?: "Sign up failed") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email to reset password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = "Password reset email sent (if account exists)") }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message ?: "Failed to send reset email") }
                }
        }
    }
}
