package com.tracker.finance_app.presentation.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AccountsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val netWorthSummary: NetWorthSummary? = null,
    val error: String? = null,

    // Form State
    val newAccountName: String = "",
    val newAccountType: AccountType = AccountType.BANK,
    val newAccountCategory: AccountCategory = AccountCategory.ASSET,
    val newAccountBalance: String = "",
    val newAccountInstitution: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            accountRepository.fetchAccounts()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, accounts = list) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = exc.message) }
                }

            accountRepository.getNetWorthSummary().onSuccess { summary ->
                _uiState.update { it.copy(netWorthSummary = summary) }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(newAccountName = name) }
    }

    fun onTypeChanged(type: AccountType) {
        _uiState.update { it.copy(newAccountType = type) }
    }

    fun onCategoryChanged(category: AccountCategory) {
        _uiState.update { it.copy(newAccountCategory = category) }
    }

    fun onBalanceChanged(balance: String) {
        _uiState.update { it.copy(newAccountBalance = balance) }
    }

    fun onInstitutionChanged(institution: String) {
        _uiState.update { it.copy(newAccountInstitution = institution) }
    }

    fun createAccount(onSuccess: () -> Unit) {
        val name = _uiState.value.newAccountName.trim()
        val balance = _uiState.value.newAccountBalance.toDoubleOrNull() ?: 0.0

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Account name required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val req = AccountCreateUpdateRequest(
                name = name,
                type = _uiState.value.newAccountType,
                category = _uiState.value.newAccountCategory,
                balance = balance,
                institution = _uiState.value.newAccountInstitution.ifBlank { null }
            )
            accountRepository.createAccount(req)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, newAccountName = "", newAccountBalance = "") }
                    loadData()
                    onSuccess()
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isSaving = false, error = exc.message) }
                }
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            accountRepository.deleteAccount(id)
            loadData()
        }
    }
}
