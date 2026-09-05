package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.remote.SmsMessagePayload
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService,
    private val accountRepositoryProvider: javax.inject.Provider<com.tracker.finance_app.domain.repository.AccountRepository>
) : TransactionRepository {

    // Secondary constructor for testing
    constructor(apiService: FinanceApiService) : this(apiService, javax.inject.Provider {
        object : com.tracker.finance_app.domain.repository.AccountRepository {
            override fun getAccountsFlow() = kotlinx.coroutines.flow.emptyFlow<List<Account>>()
            override suspend fun fetchAccounts(forceRefresh: Boolean) = Result.success(emptyList<Account>())
            override suspend fun createAccount(request: AccountCreateUpdateRequest) = Result.failure<Account>(NotImplementedError())
            override suspend fun updateAccount(id: String, request: AccountCreateUpdateRequest) = Result.failure<Account>(NotImplementedError())
            override suspend fun deleteAccount(id: String) = Result.success(Unit)
            override suspend fun getNetWorthSummary(forceRefresh: Boolean) = Result.failure<NetWorthSummary>(NotImplementedError())
            override suspend fun initializeDefaults() = Result.failure<Account>(NotImplementedError())
            override fun invalidateCache() {}
            override fun clearCache() {}
        }
    })

    private val _transactionsState = MutableStateFlow<List<Transaction>>(emptyList())
    private var isCacheValid = false
    private var _lastMutationTime: Long = 0L
    override val lastMutationTime: Long get() = _lastMutationTime

    private val _transactionUpdates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    override val transactionUpdates: kotlinx.coroutines.flow.SharedFlow<Unit> = _transactionUpdates.asSharedFlow()

    private fun notifyMutation() {
        _lastMutationTime = System.currentTimeMillis()
        _transactionUpdates.tryEmit(Unit)
        try { accountRepositoryProvider.get()?.invalidateCache() } catch (_: Exception) {}
    }

    override fun getTransactionsFlow(): Flow<List<Transaction>> = _transactionsState.asStateFlow()

    override suspend fun fetchTransactions(forceRefresh: Boolean): Result<List<Transaction>> {
        if (!forceRefresh && isCacheValid && _transactionsState.value.isNotEmpty()) {
            return Result.success(_transactionsState.value)
        }
        return runCatching {
            val items = apiService.getTransactions().map { it.normalized() }
            _transactionsState.value = items
            isCacheValid = true
            items
        }
    }

    override suspend fun addTransaction(
        accountId: String?,
        amount: Double,
        type: TransactionType,
        categoryId: String?,
        categoryName: String?,
        transactionName: String?,
        notes: String?,
        occurredAt: java.time.LocalDateTime
    ): Result<Transaction> {
        return runCatching {
            val txName = transactionName?.trim()?.ifBlank { null }
                ?: notes?.take(50)?.ifBlank { null }
                ?: (categoryName?.ifBlank { null } ?: "New Transaction")
            val request = com.tracker.finance_app.data.remote.CreateTransactionRequest(
                transactionName = txName,
                amount = amount,
                type = type.name,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = occurredAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                notes = notes?.ifBlank { null },
                currency = "INR"
            )
            val item = apiService.createTransaction(request).normalized()
            _transactionsState.value = listOf(item) + _transactionsState.value
            isCacheValid = true
            notifyMutation()
            item
        }
    }
    
    override suspend fun updateTransaction(transactionId: String, transaction: Transaction): Result<Transaction> {
        return runCatching {
            val item = apiService.updateTransaction(transactionId, transaction)
            _transactionsState.value = _transactionsState.value.map { if (it.id == transactionId) item else it }
            isCacheValid = true
            notifyMutation()
            item
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteTransaction(id)
            _transactionsState.value = _transactionsState.value.filterNot { it.id == id }
            isCacheValid = true
            notifyMutation()
        }
    }

    override fun invalidateCache() {
        isCacheValid = false
        _lastMutationTime = System.currentTimeMillis()
        _transactionUpdates.tryEmit(Unit)
    }

    override fun clearCache() {
        _transactionsState.value = emptyList()
        isCacheValid = false
        _lastMutationTime = 0L
    }
    
    override suspend fun fetchExpenseReport(start: String, end: String, type: String): Result<MonthlyExpenseResponse> {
        return runCatching {
            apiService.getExpenseReport(ExpenseReportRequest(start, end, type))
        }
    }
    
    override suspend fun exportSmsMessages(messages: List<SmsMessagePayload>): Result<Unit> {
        return runCatching {
            apiService.exportSmsMessages(messages)
        }
    }
}
