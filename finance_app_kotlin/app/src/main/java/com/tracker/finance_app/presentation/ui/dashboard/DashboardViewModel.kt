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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()),
    val userName: String? = null,
    val dashboardMode: DashboardMode = DashboardMode.EXPENSE_ONLY,
    val accounts: List<Account> = emptyList(),
    val netWorthSummary: NetWorthSummary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val breakdowns: List<CategoryBreakdown> = emptyList(),
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

            // Step 1: Fetch profile first to determine dashboard mode
            val profileRes = authRepository.getUserProfile()
            val profile = profileRes.getOrNull()
            val mode = profile?.dashboardMode ?: DashboardMode.EXPENSE_ONLY

            val (startDate, endDate) = getMonthDateRange(_uiState.value.selectedMonth)

            // Step 2: Parallel fetch for transactions & expense report analysis (/api/v1/transactions/analysis)
            val txnsDeferred = async { transactionRepository.fetchTransactions() }
            val expenseReportDeferred = async { transactionRepository.fetchExpenseReport(startDate, endDate, "EXPENSE") }

            // Step 3: Conditional calls (only for EXPENSE_AND_ACCOUNT mode)
            val accountsDeferred = if (mode == DashboardMode.EXPENSE_AND_ACCOUNT) {
                async { accountRepository.fetchAccounts() }
            } else null

            val netWorthDeferred = if (mode == DashboardMode.EXPENSE_AND_ACCOUNT) {
                async { accountRepository.getNetWorthSummary() }
            } else null

            val txnsRes = txnsDeferred.await()
            val expenseReportRes = expenseReportDeferred.await()
            val accountsRes = accountsDeferred?.await()
            val netWorthRes = netWorthDeferred?.await()
            
            val displayName = profile?.name

            val txnsList = txnsRes.getOrDefault(emptyList())
            val expenseReport = expenseReportRes.getOrNull()

            val calculatedExpense = expenseReport?.total ?: txnsList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val calculatedIncome = txnsList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val calculatedNetSavings = calculatedIncome - calculatedExpense
            val breakdownsList = expenseReport?.byCategory ?: emptyList()

            val computedBudget = if (calculatedIncome > 0) {
                (calculatedIncome * 0.7).coerceAtLeast(10000.0)
            } else {
                30000.0
            }

            val firstError = listOfNotNull(
                profileRes.exceptionOrNull(),
                accountsRes?.exceptionOrNull(),
                netWorthRes?.exceptionOrNull(),
                txnsRes.exceptionOrNull(),
                expenseReportRes.exceptionOrNull()
            ).firstOrNull()?.message

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    isRefreshing = false,
                    userName = displayName,
                    dashboardMode = mode,
                    accounts = accountsRes?.getOrDefault(emptyList()) ?: emptyList(),
                    netWorthSummary = netWorthRes?.getOrNull(),
                    recentTransactions = txnsList.take(6),
                    totalExpense = calculatedExpense,
                    totalIncome = calculatedIncome,
                    netSavings = calculatedNetSavings,
                    breakdowns = breakdownsList,
                    monthlyBudget = computedBudget,
                    error = firstError
                )
            }
        }
    }

    private fun getMonthDateRange(monthName: String): Pair<String, String> {
        val now = LocalDate.now()
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthIndex = monthNames.indexOfFirst { it.equals(monthName, ignoreCase = true) }
            .takeIf { it >= 0 } ?: (now.monthValue - 1)

        val targetMonth = monthIndex + 1
        val year = now.year
        val yearMonth = YearMonth.of(year, targetMonth)
        val start = yearMonth.atDay(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return Pair(start, end)
    }
}
