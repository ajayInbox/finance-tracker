package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : AccountRepository {

    private val _accountsState = MutableStateFlow<List<Account>>(emptyList())

    override fun getAccountsFlow(): Flow<List<Account>> = _accountsState.asStateFlow()

    override suspend fun fetchAccounts(): Result<List<Account>> {
        return runCatching {
            val items = apiService.getAccounts()
            _accountsState.value = items
            items
        }
    }

    override suspend fun createAccount(request: AccountCreateUpdateRequest): Result<Account> {
        return runCatching {
            val item = apiService.createAccount(request)
            _accountsState.value = _accountsState.value + item
            item
        }
    }

    override suspend fun updateAccount(id: String, request: AccountCreateUpdateRequest): Result<Account> {
        return runCatching {
            val item = apiService.updateAccount(id, request)
            _accountsState.value = _accountsState.value.map { if (it.id == id) item else it }
            item
        }
    }

    override suspend fun deleteAccount(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteAccount(id)
            _accountsState.value = _accountsState.value.filterNot { it.id == id }
        }
    }

    override suspend fun getNetWorthSummary(): Result<NetWorthSummary> {
        return runCatching {
            apiService.getNetWorthSummary()
        }
    }
    
    override suspend fun initializeDefaults(): Result<Account> {
        return runCatching {
            val item = apiService.initializeDefaults()
            fetchAccounts()
            item
        }
    }
}
