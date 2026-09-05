package com.tracker.finance_app.presentation.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionFilter(
    val transactionType: TransactionType? = null,
    val timePeriod: TimePeriod = TimePeriod.ALL
)

enum class TimePeriod {
    ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH
}

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val summary: TransactionSummary? = null,
    val breakdowns: List<CategoryBreakdown> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val isFilterActive: Boolean = false,
    val error: String? = null,
    val lastDeletedTransaction: Transaction? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        viewModelScope.launch {
            transactionRepository.getTransactionsFlow().collect { transactions ->
                if (transactions.isNotEmpty()) {
                    _uiState.update { state ->
                        updateLists(state, transactions)
                    }
                }
            }
        }
    }

    fun loadTransactions(forceRefresh: Boolean = false) {
        val hasExistingData = _uiState.value.transactions.isNotEmpty()
        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else if (!hasExistingData) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            transactionRepository.fetchTransactions(forceRefresh = forceRefresh)
                .onSuccess { list ->
                    _uiState.update { state ->
                        updateLists(state, list).copy(isLoading = false, isRefreshing = false)
                    }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = exc.message) }
                }
        }
    }

    fun applyFilter(filter: TransactionFilter) {
        _uiState.update { state ->
            val isActive = filter.transactionType != null || filter.timePeriod != TimePeriod.ALL || state.searchQuery.isNotEmpty()
            val filtered = applyFilterToList(state.transactions, filter, state.searchQuery)
            state.copy(
                filter = filter,
                isFilterActive = isActive,
                filteredTransactions = filtered,
                groupedTransactions = groupTransactions(filtered)
            )
        }
    }

    fun clearFilters() {
        applyFilter(TransactionFilter())
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterToList(state.transactions, state.filter, query)
            state.copy(
                searchQuery = query,
                filteredTransactions = filtered,
                groupedTransactions = groupTransactions(filtered)
            )
        }
    }

    private fun applyFilterToList(transactions: List<Transaction>, filter: TransactionFilter, searchQuery: String): List<Transaction> {
        var result = transactions

        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            result = result.filter {
                it.description.lowercase().contains(query) ||
                (it.categoryName?.lowercase()?.contains(query) == true) ||
                (it.merchantName?.lowercase()?.contains(query) == true)
            }
        }

        filter.transactionType?.let { type ->
            result = result.filter { it.type == type }
        }

        if (filter.timePeriod != TimePeriod.ALL) {
            val now = java.time.LocalDate.now()
            result = result.filter {
                val txDate = parseLocalDate(it)
                if (txDate != java.time.LocalDate.MIN) {
                    when (filter.timePeriod) {
                        TimePeriod.TODAY -> txDate == now
                        TimePeriod.YESTERDAY -> txDate == now.minusDays(1)
                        TimePeriod.THIS_WEEK -> {
                            val startOfWeek = now.with(java.time.DayOfWeek.MONDAY)
                            val endOfWeek = now.with(java.time.DayOfWeek.SUNDAY)
                            !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                        }
                        TimePeriod.THIS_MONTH -> {
                            txDate.year == now.year && txDate.month == now.month
                        }
                        TimePeriod.ALL -> true
                    }
                } else {
                    true // If parsing fails, include it
                }
            }
        }

        return result
    }

    private fun parseLocalDate(tx: Transaction): java.time.LocalDate {
        val raw = tx.timestamp.ifBlank { tx.occurredAt.orEmpty() }.trim()
        if (raw.isBlank()) return java.time.LocalDate.MIN
        try {
            return java.time.LocalDateTime.parse(raw).toLocalDate()
        } catch (_: Exception) {}
        try {
            return java.time.OffsetDateTime.parse(raw).toLocalDate()
        } catch (_: Exception) {}
        try {
            return java.time.Instant.parse(raw).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        } catch (_: Exception) {}
        try {
            return java.time.LocalDate.parse(raw.take(10))
        } catch (_: Exception) {}
        return java.time.LocalDate.MIN
    }

    private fun groupTransactions(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val now = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")

        val groupedByDate = transactions.groupBy { parseLocalDate(it) }
        val sortedDates = groupedByDate.keys.sortedDescending()

        val result = linkedMapOf<String, List<Transaction>>()
        for (date in sortedDates) {
            val label = when (date) {
                java.time.LocalDate.MIN -> "Earlier"
                now -> "Today"
                now.minusDays(1) -> "Yesterday"
                else -> date.format(formatter)
            }
            val existing = result[label].orEmpty()
            result[label] = existing + (groupedByDate[date] ?: emptyList())
        }
        return result
    }

    fun addTransaction(
        accountId: String?,
        amount: Double,
        type: TransactionType,
        categoryId: String?,
        categoryName: String? = null,
        transactionName: String? = null,
        notes: String? = null,
        date: java.time.LocalDate = java.time.LocalDate.now(),
        time: java.time.LocalTime = java.time.LocalTime.now(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val occurredAt = java.time.LocalDateTime.of(date, time)
            transactionRepository.addTransaction(
                accountId = accountId,
                amount = amount,
                type = type,
                categoryId = categoryId,
                categoryName = categoryName,
                transactionName = transactionName,
                notes = notes,
                occurredAt = occurredAt
            ).onSuccess {
                loadTransactions()
                onSuccess()
            }.onFailure { exc ->
                _uiState.update { it.copy(error = exc.message) }
            }
        }
    }

    fun addTransaction(
        accountId: String?,
        amount: Double,
        type: TransactionType,
        description: String,
        categoryId: String?,
        onSuccess: () -> Unit
    ) {
        addTransaction(
            accountId = accountId,
            amount = amount,
            type = type,
            categoryId = categoryId,
            categoryName = null,
            transactionName = description.ifBlank { "New Transaction" },
            notes = description,
            onSuccess = onSuccess
        )
    }

    fun updateTransaction(transactionId: String, transaction: Transaction, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transactionId, transaction)
                .onSuccess {
                    loadTransactions()
                    onSuccess()
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(error = exc.message) }
                }
        }
    }

    fun deleteTransaction(id: String) {
        val target = _uiState.value.transactions.firstOrNull { it.id == id } ?: return
        _uiState.update { state ->
            updateLists(state, state.transactions.filterNot { it.id == id })
        }
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
                .onSuccess {
                    _uiState.update { it.copy(lastDeletedTransaction = target) }
                }
                .onFailure { exc ->
                    _uiState.update { state ->
                        val restored = if (state.transactions.any { it.id == id }) {
                            state.transactions
                        } else {
                            (listOf(target) + state.transactions).sortedByDescending { it.timestamp }
                        }
                        updateLists(state, restored).copy(error = exc.message ?: "Couldn't delete transaction")
                    }
                }
        }
    }

    fun undoDelete(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.addTransaction(
                accountId = transaction.accountId,
                amount = transaction.amount,
                type = transaction.type,
                categoryId = transaction.categoryId,
                categoryName = transaction.categoryName,
                transactionName = transaction.transactionName ?: "New Transaction",
                notes = transaction.effectiveDescription
            ).onSuccess {
                loadTransactions()
            }.onFailure { exc ->
                _uiState.update { it.copy(error = exc.message ?: "Couldn't restore transaction") }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun onDeleteMessageShown() {
        _uiState.update { it.copy(lastDeletedTransaction = null) }
    }

    private fun computeSummary(transactions: List<Transaction>): TransactionSummary {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return TransactionSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netSavings = totalIncome - totalExpense,
            transactionCount = transactions.size
        )
    }

    private fun updateLists(state: TransactionsUiState, transactions: List<Transaction>): TransactionsUiState {
        val filtered = applyFilterToList(transactions, state.filter, state.searchQuery)
        return state.copy(
            transactions = transactions,
            filteredTransactions = filtered,
            groupedTransactions = groupTransactions(filtered),
            summary = computeSummary(transactions)
        )
    }
}
