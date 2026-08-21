package com.tracker.finance_app.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import com.tracker.finance_app.domain.repository.AuthRepository
import com.tracker.finance_app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()),
    val userName: String? = null,
    val accounts: List<Account> = emptyList(),
    val netWorthSummary: NetWorthSummary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val summary: TransactionSummary? = null,
    val breakdowns: List<CategoryBreakdown> = emptyList(),
    val averageDailyExpense: AverageDailyExpense? = null,
    val monthlyBudget: Double = 30000.0,
    val incomeTrend: String = "",
    val expenseTrend: String = "",
    val savingsTrend: String = "",
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onMonthSelected(month: String) {
        _uiState.update { it.copy(selectedMonth = month) }
        loadDashboardData(isRefresh = true)
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadDashboardData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            val profileDeferred = async { authRepository.getUserProfile() }
            val accountsDeferred = async { accountRepository.fetchAccounts() }
            val netWorthDeferred = async { accountRepository.getNetWorthSummary() }
            val txnsDeferred = async { transactionRepository.fetchTransactions() }
            val summaryDeferred = async { transactionRepository.getSummary() }
            val breakdownDeferred = async { transactionRepository.getCategoryBreakdown() }
            val avgDailyDeferred = async { transactionRepository.fetchAverageDailyExpense() }

            val profileRes = profileDeferred.await()
            val accountsRes = accountsDeferred.await()
            val netWorthRes = netWorthDeferred.await()
            val txnsRes = txnsDeferred.await()
            val summaryRes = summaryDeferred.await()
            val breakdownRes = breakdownDeferred.await()
            val avgDailyRes = avgDailyDeferred.await()

            val displayName = profileRes.getOrNull()?.let { profile ->
                listOfNotNull(profile.firstName, profile.lastName)
                    .joinToString(" ")
                    .ifBlank { profile.email.substringBefore("@") }
            }

            val summaryData = summaryRes.getOrNull()
            val computedBudget = if (summaryData != null && summaryData.totalIncome > 0) {
                (summaryData.totalIncome * 0.7).coerceAtLeast(10000.0)
            } else {
                30000.0
            }

            val firstError = listOfNotNull(
                profileRes.exceptionOrNull(),
                accountsRes.exceptionOrNull(),
                netWorthRes.exceptionOrNull(),
                txnsRes.exceptionOrNull(),
                summaryRes.exceptionOrNull(),
                breakdownRes.exceptionOrNull(),
                avgDailyRes.exceptionOrNull()
            ).firstOrNull()?.message

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    isRefreshing = false,
                    userName = displayName,
                    accounts = accountsRes.getOrDefault(emptyList()),
                    netWorthSummary = netWorthRes.getOrNull(),
                    recentTransactions = txnsRes.getOrDefault(emptyList()).take(6),
                    summary = summaryData,
                    breakdowns = breakdownRes.getOrDefault(emptyList()),
                    averageDailyExpense = avgDailyRes.getOrNull(),
                    monthlyBudget = computedBudget,
                    error = firstError
                )
            }
        }
    }
}
