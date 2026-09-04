package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.data.remote.SmsMessagePayload
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsFlow(): Flow<List<Transaction>>
    suspend fun fetchTransactions(): Result<List<Transaction>>
    suspend fun addTransaction(accountId: String, amount: Double, type: TransactionType, description: String, categoryId: String?): Result<Transaction>
    suspend fun updateTransaction(transactionId: String, transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun fetchExpenseReport(start: String, end: String, type: String = "EXPENSE"): Result<MonthlyExpenseResponse>
    suspend fun exportSmsMessages(messages: List<SmsMessagePayload>): Result<Unit>
}
