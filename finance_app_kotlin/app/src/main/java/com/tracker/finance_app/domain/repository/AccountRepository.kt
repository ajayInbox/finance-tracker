package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val lastMutationTime: Long get() = 0L
    val accountUpdates: kotlinx.coroutines.flow.SharedFlow<Unit> get() = kotlinx.coroutines.flow.MutableSharedFlow()
    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun fetchAccounts(forceRefresh: Boolean = false): Result<List<Account>>
    suspend fun createAccount(request: AccountCreateUpdateRequest): Result<Account>
    suspend fun updateAccount(id: String, request: AccountCreateUpdateRequest): Result<Account>
    suspend fun deleteAccount(id: String): Result<Unit>
    suspend fun getNetWorthSummary(forceRefresh: Boolean = false): Result<NetWorthSummary>
    suspend fun initializeDefaults(): Result<Account>
    fun invalidateCache()
    fun clearCache()
}
