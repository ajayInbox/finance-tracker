package com.tracker.finance_app.presentation.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.core.util.Formatters
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
        val balanceText = _uiState.value.newAccountBalance.trim()
        val balance = when {
            balanceText.isEmpty() -> 0.0
            else -> Formatters.parseAmountOrNull(balanceText)
        }

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Account name required") }
            return
        }
        if (balance == null || balance < 0.0) {
            _uiState.update { it.copy(error = "Enter a valid opening balance") }
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

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun deleteAccount(id: String) {
        val target = _uiState.value.accounts.firstOrNull { it.id == id } ?: return
        _uiState.update { state ->
            state.copy(accounts = state.accounts.filterNot { it.id == id })
        }
        viewModelScope.launch {
            accountRepository.deleteAccount(id)
                .onSuccess {
                    loadData()
                }
                .onFailure { exc ->
                    _uiState.update { state ->
                        if (state.accounts.any { it.id == id }) {
                            state
                        } else {
                            state.copy(accounts = listOf(target) + state.accounts)
                        }
                    }
                    _uiState.update { it.copy(error = exc.message ?: "Couldn't delete account") }
                }
        }
    }
}
