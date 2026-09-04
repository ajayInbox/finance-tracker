package com.tracker.finance_app.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.data.sync.SyncManager
import com.tracker.finance_app.data.sync.SyncPreferences
import com.tracker.finance_app.domain.model.UserProfile
import com.tracker.finance_app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val message: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val biometricEnabled: Boolean = false,
    val selectedCurrency: String = "INR",
    val isProMember: Boolean = true, // Mocked
    val autoSyncEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadAutoSyncSetting()
    }

    private fun loadAutoSyncSetting() {
        viewModelScope.launch {
            _uiState.update { it.copy(autoSyncEnabled = syncPreferences.isAutoSyncEnabled()) }
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) syncManager.enableAutoSync() else syncManager.disableAutoSync()
            syncPreferences.setAutoSyncEnabled(enabled)
            _uiState.update {
                it.copy(
                    autoSyncEnabled = enabled,
                    message = if (enabled) "Inbox will be scanned every 6 hours" else "Background sync turned off"
                )
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, userProfile = profile) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message) }
                }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.userProfile?.email ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, message = "Password reset email sent") }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message ?: "Failed to send reset email") }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteConfirmation = false) }
            authRepository.deleteAccount()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message) }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun setCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }
}
