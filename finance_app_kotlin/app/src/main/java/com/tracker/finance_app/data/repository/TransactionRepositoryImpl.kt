package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.remote.SmsMessagePayload
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : TransactionRepository {

    private val _transactionsState = MutableStateFlow<List<Transaction>>(emptyList())

    override fun getTransactionsFlow(): Flow<List<Transaction>> = _transactionsState.asStateFlow()

    override suspend fun fetchTransactions(): Result<List<Transaction>> {
        return runCatching {
            val items = apiService.getTransactions()
            _transactionsState.value = items
            items
        }
    }

    override suspend fun addTransaction(
        accountId: String,
        amount: Double,
        type: TransactionType,
        description: String,
        categoryId: String?
    ): Result<Transaction> {
        return runCatching {
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                amount = amount,
                type = type,
                description = description,
                categoryId = categoryId,
                timestamp = System.currentTimeMillis().toString()
            )
            val item = apiService.createTransaction(tx)
            _transactionsState.value = listOf(item) + _transactionsState.value
            item
        }
    }
    
    override suspend fun updateTransaction(transactionId: String, transaction: Transaction): Result<Transaction> {
        return runCatching {
            val item = apiService.updateTransaction(transactionId, transaction)
            _transactionsState.value = _transactionsState.value.map { if (it.id == transactionId) item else it }
            item
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteTransaction(id)
            _transactionsState.value = _transactionsState.value.filterNot { it.id == id }
        }
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
