package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun fetchAccounts(): Result<List<Account>>
    suspend fun createAccount(request: AccountCreateUpdateRequest): Result<Account>
    suspend fun updateAccount(id: String, request: AccountCreateUpdateRequest): Result<Account>
    suspend fun deleteAccount(id: String): Result<Unit>
    suspend fun getNetWorthSummary(): Result<NetWorthSummary>
    suspend fun initializeDefaults(): Result<Account>
}
