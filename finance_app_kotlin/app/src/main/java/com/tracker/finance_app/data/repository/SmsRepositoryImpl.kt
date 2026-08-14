package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.domain.repository.SmsRepository
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : SmsRepository {

    override suspend fun fetchDrafts(): Result<List<TransactionDraft>> {
        return runCatching {
            apiService.getDrafts(version = 3)
        }
    }

    override suspend fun deleteDraftsBatch(ids: List<String>): Result<Unit> {
        return runCatching {
            apiService.deleteDraftsBatch(ids)
        }
    }
}
