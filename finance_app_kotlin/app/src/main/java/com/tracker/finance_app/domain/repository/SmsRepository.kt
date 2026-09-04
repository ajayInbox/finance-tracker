package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.data.remote.BatchUpdateTransactionRequest
import com.tracker.finance_app.domain.model.TransactionDraft

interface SmsRepository {
    suspend fun fetchDrafts(): Result<List<TransactionDraft>>
    suspend fun confirmDrafts(requests: List<BatchUpdateTransactionRequest>): Result<Unit>
    suspend fun deleteDraftsBatch(ids: List<String>): Result<Unit>
}
