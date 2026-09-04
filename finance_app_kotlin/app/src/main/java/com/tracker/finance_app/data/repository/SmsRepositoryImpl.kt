package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.BatchUpdateTransactionRequest
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.domain.repository.SmsRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : SmsRepository {

    override suspend fun fetchDrafts(): Result<List<TransactionDraft>> {
        return runCatching {
            apiService.getDrafts(version = 3)
        }
    }

    /**
     * Converts confirmed drafts to backend batch-update payloads.
     * The draft's occurredAt is an ISO instant (e.g. 2024-08-23T09:15:00Z);
     * the backend expects a local LocalDateTime, so it is normalized here.
     */
    override suspend fun confirmDrafts(requests: List<BatchUpdateTransactionRequest>): Result<Unit> {
        return runCatching {
            apiService.batchUpdateTransactions(requests)
        }
    }

    override suspend fun deleteDraftsBatch(ids: List<String>): Result<Unit> {
        return runCatching {
            apiService.deleteDraftsBatch(ids)
        }
    }

    companion object {
        fun normalizeOccurredAt(iso: String?): String {
            if (iso.isNullOrBlank()) return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return try {
                Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                iso.removeSuffix("Z").substringBefore('.')
            }
        }

        fun buildConfirmRequest(draft: TransactionDraft): BatchUpdateTransactionRequest {
            return BatchUpdateTransactionRequest(
                id = draft.id,
                transactionName = draft.transactionName ?: "SMS Transaction",
                amount = draft.amount,
                type = draft.type.name,
                categoryId = draft.categoryId!!,
                accountId = draft.accountId!!,
                occurredAt = normalizeOccurredAt(draft.occurredAt),
                merchant = draft.transactionName,
                notes = draft.notes ?: draft.rawSms.ifBlank { null },
                tags = draft.tags,
                currency = draft.currency.take(3).ifBlank { "INR" }
            )
        }
    }
}
