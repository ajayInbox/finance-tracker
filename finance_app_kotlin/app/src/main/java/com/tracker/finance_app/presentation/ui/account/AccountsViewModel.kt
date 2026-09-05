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
    val toastMessage: String? = null,

    // Form & Bottom Sheet State
    val isBottomSheetOpen: Boolean = false,
    val editingAccountId: String? = null, // null = create mode, non-null = edit mode
    val newAccountName: String = "",
    val newAccountType: AccountType = AccountType.CHECKING,
    val newAccountCategory: AccountCategory = AccountCategory.ASSET,
    val newAccountBalance: String = "",
    val newAccountInstitution: String = "",
    val newAccountNumber: String = "",
    val newAccountCreditLimit: String = "",
    val isSaving: Boolean = false,

    // Delete Confirmation State
    val accountToDelete: Account? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            accountRepository.getAccountsFlow().collect { accounts ->
                if (accounts.isNotEmpty()) {
                    _uiState.update { it.copy(accounts = accounts) }
                }
            }
        }
    }

    fun loadData(isRefresh: Boolean = false) {
        val hasExistingData = _uiState.value.accounts.isNotEmpty()
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else if (!hasExistingData) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            accountRepository.fetchAccounts(forceRefresh = isRefresh)
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, accounts = list) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = exc.message) }
                }

            accountRepository.getNetWorthSummary(forceRefresh = isRefresh).onSuccess { summary ->
                _uiState.update { it.copy(netWorthSummary = summary) }
            }
        }
    }

    fun openAddAccountSheet() {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = true,
                editingAccountId = null,
                newAccountName = "",
                newAccountInstitution = "",
                newAccountNumber = "",
                newAccountBalance = "",
                newAccountCreditLimit = "",
                newAccountCategory = AccountCategory.ASSET,
                newAccountType = AccountType.CHECKING,
                error = null
            )
        }
    }

    // Single-route form: reset for full-page AddAccountScreen (no bottom sheet)
    fun prepareAddAccount() {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = false,
                editingAccountId = null,
                newAccountName = "",
                newAccountInstitution = "",
                newAccountNumber = "",
                newAccountBalance = "",
                newAccountCreditLimit = "",
                newAccountCategory = AccountCategory.ASSET,
                newAccountType = AccountType.CHECKING,
                error = null
            )
        }
    }

    // Single-route form: populate for full-page AddAccountScreen edit mode (no bottom sheet)
    fun prepareEditAccount(account: Account) {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = false,
                editingAccountId = account.id,
                newAccountName = account.name,
                newAccountInstitution = account.institution.orEmpty(),
                newAccountNumber = account.accountNumber.orEmpty(),
                newAccountBalance = if (account.balance == 0.0) "" else account.balance.toString(),
                newAccountCreditLimit = account.creditLimit?.toString().orEmpty(),
                newAccountCategory = account.category,
                newAccountType = account.type,
                error = null
            )
        }
    }

    fun openEditAccountSheet(account: Account) {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = true,
                editingAccountId = account.id,
                newAccountName = account.name,
                newAccountInstitution = account.institution.orEmpty(),
                newAccountNumber = account.accountNumber.orEmpty(),
                newAccountBalance = if (account.balance == 0.0) "" else account.balance.toString(),
                newAccountCreditLimit = account.creditLimit?.toString().orEmpty(),
                newAccountCategory = account.category,
                newAccountType = account.type,
                error = null
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = false,
                editingAccountId = null,
                error = null
            )
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(newAccountName = name) }
    }

    fun onTypeChanged(type: AccountType) {
        _uiState.update { it.copy(newAccountType = type) }
    }

    fun onCategoryChanged(category: AccountCategory) {
        val defaultType = when (category) {
            AccountCategory.ASSET -> {
                if (_uiState.value.newAccountType == AccountType.CREDIT_CARD || _uiState.value.newAccountType == AccountType.LOAN) {
                    AccountType.CHECKING
                } else {
                    _uiState.value.newAccountType
                }
            }
            AccountCategory.LIABILITY -> {
                if (_uiState.value.newAccountType != AccountType.CREDIT_CARD && _uiState.value.newAccountType != AccountType.LOAN) {
                    AccountType.CREDIT_CARD
                } else {
                    _uiState.value.newAccountType
                }
            }
        }
        _uiState.update { it.copy(newAccountCategory = category, newAccountType = defaultType) }
    }

    fun onBalanceChanged(balance: String) {
        _uiState.update { it.copy(newAccountBalance = balance) }
    }

    fun onInstitutionChanged(institution: String) {
        _uiState.update { it.copy(newAccountInstitution = institution) }
    }

    fun onAccountNumberChanged(number: String) {
        _uiState.update { it.copy(newAccountNumber = number) }
    }

    fun onCreditLimitChanged(limit: String) {
        _uiState.update { it.copy(newAccountCreditLimit = limit) }
    }

    fun saveAccount(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val name = state.newAccountName.trim()
        val balanceText = state.newAccountBalance.trim()
        val balance = when {
            balanceText.isEmpty() -> 0.0
            else -> Formatters.parseAmountOrNull(balanceText)
        }

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Account name is required") }
            return
        }
        if (balance == null || balance < 0.0) {
            _uiState.update { it.copy(error = "Enter a valid balance") }
            return
        }

        val creditLimit = state.newAccountCreditLimit.trim().toDoubleOrNull()
        val req = AccountCreateUpdateRequest(
            name = name,
            lastFour = state.newAccountNumber.trim().ifBlank { null },
            institution = state.newAccountInstitution.trim().ifBlank { null },
            type = state.newAccountType,
            category = state.newAccountCategory,
            balance = balance,
            creditLimit = creditLimit
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val editingId = state.editingAccountId

            if (editingId != null) {
                accountRepository.updateAccount(editingId, req)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isBottomSheetOpen = false,
                                editingAccountId = null,
                                toastMessage = "Account updated successfully"
                            )
                        }
                        loadData()
                        onSuccess()
                    }
                    .onFailure { exc ->
                        _uiState.update { it.copy(isSaving = false, error = exc.message ?: "Failed to update account") }
                    }
            } else {
                accountRepository.createAccount(req)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isBottomSheetOpen = false,
                                editingAccountId = null,
                                newAccountName = "",
                                newAccountBalance = "",
                                toastMessage = "Account created successfully"
                            )
                        }
                        loadData()
                        onSuccess()
                    }
                    .onFailure { exc ->
                        _uiState.update { it.copy(isSaving = false, error = exc.message ?: "Failed to create account") }
                    }
            }
        }
    }

    fun createAccount(onSuccess: () -> Unit) {
        saveAccount(onSuccess)
    }

    fun requestDeleteAccount(account: Account) {
        _uiState.update { it.copy(accountToDelete = account) }
    }

    fun cancelDeleteAccount() {
        _uiState.update { it.copy(accountToDelete = null) }
    }

    fun confirmDeleteAccount() {
        val target = _uiState.value.accountToDelete ?: return
        _uiState.update { it.copy(accountToDelete = null) }
        deleteAccount(target.id)
    }

    fun deleteAccount(id: String) {
        val target = _uiState.value.accounts.firstOrNull { it.id == id } ?: return
        _uiState.update { state ->
            state.copy(accounts = state.accounts.filterNot { it.id == id })
        }
        viewModelScope.launch {
            accountRepository.deleteAccount(id)
                .onSuccess {
                    _uiState.update { it.copy(toastMessage = "Account deleted") }
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

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun onToastShown() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
