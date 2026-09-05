package com.tracker.finance_app.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Transaction(
    val id: String = "",
    val accountId: String? = null,
    val amount: Double = 0.0,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    @JsonNames("description", "transactionName")
    val description: String = "",
    val transactionName: String? = null,
    val merchantName: String? = null,
    @JsonNames("timestamp", "occurredAt")
    val timestamp: String = "",
    val occurredAt: String? = null,
    val notes: String? = null,
    val rawSmsText: String? = null,
    val isAutoParsed: Boolean = false
) {
    val effectiveDescription: String
        get() = description.ifBlank { transactionName ?: notes ?: "Transaction" }

    val effectiveTimestamp: String
        get() = timestamp.ifBlank { occurredAt ?: "" }

    fun normalized(): Transaction {
        val normDesc = effectiveDescription
        val normTime = effectiveTimestamp
        return copy(
            description = normDesc,
            timestamp = normTime
        )
    }
}

@Serializable
data class TransactionSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val transactionCount: Int = 0
)

@Serializable
data class CategoryBreakdown(
    val categoryId: String? = null,
    val categoryName: String,
    val total: Double = 0.0,
    val percentage: Double = 0.0,
    val transactionCount: Int = 0,
    val categoryColor: String? = null
) {
    val totalAmount: Double get() = total
}

@Serializable
data class MonthlyExpenseResponse(
    val startDate: String? = null,
    val endDate: String? = null,
    val currency: String = "INR",
    val total: Double = 0.0,
    val byCategory: List<CategoryBreakdown> = emptyList()
)

@Serializable
data class TransactionDraft(
    val id: String = "",
    val transactionName: String? = null,
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null,
    val accountName: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val occurredAt: String? = null,
    val postedAt: String? = null,
    val currency: String = "INR",
    val notes: String? = null,
    val status: String? = null,
    val tags: List<String> = emptyList(),
    val originalMessage: String? = null
) {
    val rawSms: String get() = originalMessage.orEmpty()
    val merchant: String? get() = transactionName

    fun isReadyForConfirm(): Boolean =
        accountId != null && categoryId != null && id.isNotBlank()

    fun withAccount(accountId: String?, accountName: String?): TransactionDraft =
        copy(accountId = accountId, accountName = accountName)

    fun withCategory(categoryId: String?, categoryName: String?): TransactionDraft =
        copy(categoryId = categoryId, categoryName = categoryName)
}

@Serializable  
data class ExpenseReportRequest(
    val start: String,
    val end: String,
    val type: String = "EXPENSE"
)

@Serializable
data class SyncTimestamp(
    val timestamp: Long? = null,
    val latestScannedTimestamp: Long? = null
) {
    val watermark: Long get() = latestScannedTimestamp ?: timestamp ?: 0L
}
