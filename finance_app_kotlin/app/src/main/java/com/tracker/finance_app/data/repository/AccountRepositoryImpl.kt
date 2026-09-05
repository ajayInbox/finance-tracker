package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : AccountRepository {

    private val _accountsState = MutableStateFlow<List<Account>>(emptyList())
    private var isCacheValid = false
    private var cachedNetWorthSummary: NetWorthSummary? = null
    private var _lastMutationTime: Long = 0L
    override val lastMutationTime: Long get() = _lastMutationTime

    private val _accountUpdates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    override val accountUpdates: kotlinx.coroutines.flow.SharedFlow<Unit> = _accountUpdates.asSharedFlow()

    private fun notifyMutation() {
        _lastMutationTime = System.currentTimeMillis()
        _accountUpdates.tryEmit(Unit)
    }

    override fun getAccountsFlow(): Flow<List<Account>> = _accountsState.asStateFlow()

    override suspend fun fetchAccounts(forceRefresh: Boolean): Result<List<Account>> {
        if (!forceRefresh && isCacheValid && _accountsState.value.isNotEmpty()) {
            return Result.success(_accountsState.value)
        }
        return runCatching {
            val items = apiService.getAccounts()
            _accountsState.value = items
            isCacheValid = true
            items
        }
    }

    override suspend fun createAccount(request: AccountCreateUpdateRequest): Result<Account> {
        return runCatching {
            val item = apiService.createAccount(request)
            _accountsState.value = _accountsState.value + item
            cachedNetWorthSummary = null
            isCacheValid = true
            notifyMutation()
            item
        }
    }

    override suspend fun updateAccount(id: String, request: AccountCreateUpdateRequest): Result<Account> {
        return runCatching {
            val item = apiService.updateAccount(id, request)
            _accountsState.value = _accountsState.value.map { if (it.id == id) item else it }
            cachedNetWorthSummary = null
            isCacheValid = true
            notifyMutation()
            item
        }
    }

    override suspend fun deleteAccount(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteAccount(id)
            _accountsState.value = _accountsState.value.filterNot { it.id == id }
            cachedNetWorthSummary = null
            isCacheValid = true
            notifyMutation()
        }
    }

    override suspend fun getNetWorthSummary(forceRefresh: Boolean): Result<NetWorthSummary> {
        if (!forceRefresh && isCacheValid && cachedNetWorthSummary != null) {
            return Result.success(cachedNetWorthSummary!!)
        }
        return runCatching {
            val summary = apiService.getNetWorthSummary()
            cachedNetWorthSummary = summary
            summary
        }
    }
    
    override suspend fun initializeDefaults(): Result<Account> {
        return runCatching {
            val item = apiService.initializeDefaults()
            fetchAccounts(forceRefresh = true)
            item
        }
    }

    override fun invalidateCache() {
        isCacheValid = false
        cachedNetWorthSummary = null
        notifyMutation()
    }

    override fun clearCache() {
        _accountsState.value = emptyList()
        cachedNetWorthSummary = null
        isCacheValid = false
        _lastMutationTime = 0L
    }
}
