package com.tracker.finance_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    val amount: Double,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val type: TransactionType,
    val description: String,
    val merchantName: String? = null,
    val timestamp: String,
    val rawSmsText: String? = null,
    val isAutoParsed: Boolean = false
)

@Serializable
data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val transactionCount: Int
)

@Serializable
data class CategoryBreakdown(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Double
)

@Serializable
data class ExpenseReport(
    val period: String,
    val totalExpense: Double,
    val breakdowns: List<CategoryBreakdown>
)

@Serializable
data class TransactionDraft(
    val amount: Double,
    val merchant: String?,
    val type: TransactionType,
    val rawSms: String,
    val timestampMillis: Long,
    val suggestedCategory: String? = null
)

@Serializable
data class AverageDailyExpense(
    val average: Double,
    val period: String = "",
    val currency: String = "INR"
)

@Serializable  
data class ExpenseReportRequest(
    val start: String,
    val end: String,
    val type: String
)

@Serializable
data class SyncTimestamp(
    val timestamp: Long
)
