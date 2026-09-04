package com.tracker.finance_app.presentation.ui.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.data.repository.SmsRepositoryImpl
import com.tracker.finance_app.data.sync.SyncManager
import com.tracker.finance_app.data.sync.SyncPreferences
import com.tracker.finance_app.data.sync.SyncStatusBus
import com.tracker.finance_app.data.sync.SyncUiStatus
import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.domain.repository.AccountRepository
import com.tracker.finance_app.domain.repository.CategoryRepository
import com.tracker.finance_app.domain.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsReviewUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val drafts: List<TransactionDraft> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    val approvedCount: Int = 0,
    val autoSyncEnabled: Boolean = false,
    val isConfirming: Boolean = false
)

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val syncManager: SyncManager,
    private val syncPreferences: SyncPreferences,
    private val syncStatusBus: SyncStatusBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsReviewUiState())
    val uiState: StateFlow<SmsReviewUiState> = _uiState.asStateFlow()

    val syncStatus: StateFlow<SyncUiStatus> = syncStatusBus.status

    val lastSyncTimestamp: StateFlow<Long> = syncPreferences.lastSyncTimestamp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadDrafts()
        loadPickers()
        observeSyncStatus()
        restoreAutoSyncSetting()
    }

    private fun loadPickers() {
        viewModelScope.launch {
            accountRepository.fetchAccounts()
                .onSuccess { _accounts.value = it }
            categoryRepository.fetchCategories()
                .onSuccess { _categories.value = it }
        }
    }

    fun assignAccount(draft: TransactionDraft, account: Account) {
        _uiState.update { state ->
            state.copy(
                drafts = state.drafts.map {
                    if (it.id == draft.id) it.withAccount(account.id, account.name) else it
                }
            )
        }
    }

    fun assignCategory(draft: TransactionDraft, category: Category) {
        _uiState.update { state ->
            state.copy(
                drafts = state.drafts.map {
                    if (it.id == draft.id) it.withCategory(category.id, category.name) else it
                }
            )
        }
    }

    /** Batch-confirms the given drafts via PUT /api/v1/transactions/batch. */
    fun confirmDrafts(toConfirm: List<TransactionDraft>) {
        val ready = toConfirm.filter { it.isReadyForConfirm() }
        if (ready.isEmpty()) {
            _uiState.update { it.copy(message = "Select an account and category for each draft first") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConfirming = true, error = null, message = null) }
            smsRepository.confirmDrafts(ready.map { SmsRepositoryImpl.buildConfirmRequest(it) })
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isConfirming = false,
                            drafts = state.drafts.filter { d -> d !in ready },
                            approvedCount = state.approvedCount + ready.size,
                            message = "${ready.size} transaction(s) added to ledger"
                        )
                    }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isConfirming = false, error = exc.message ?: "Failed to confirm drafts") }
                }
        }
    }

    /** Batch-deletes drafts server-side; optimistic removal with rollback on failure. */
    fun deleteDrafts(toDelete: List<TransactionDraft>) {
        val ids = toDelete.mapNotNull { d -> d.id.takeIf { it.isNotBlank() } }
        if (ids.isEmpty()) return

        // Drafts without an id exist only locally — drop them immediately.
        val localOnly = toDelete.filter { it.id.isBlank() }
        _uiState.update { state -> state.copy(drafts = state.drafts - localOnly.toSet()) }

        viewModelScope.launch {
            smsRepository.deleteDraftsBatch(ids)
                .onSuccess {
                    _uiState.update { state -> state.copy(drafts = state.drafts - toDelete.toSet()) }
                }
                .onFailure { exc ->
                    // Roll back
                    _uiState.update { state ->
                        state.copy(drafts = (state.drafts + localOnly).distinctBy { it.id }, error = exc.message)
                    }
                }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncStatus.collect { status ->
                when (status) {
                    is SyncUiStatus.Success -> {
                        loadDrafts(isRefresh = true)
                        loadPickers()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun restoreAutoSyncSetting() {
        viewModelScope.launch {
            val enabled = syncPreferences.isAutoSyncEnabled()
            // Re-schedule in case the process was killed while a periodic job existed
            if (enabled) syncManager.enableAutoSync()
            _uiState.update { it.copy(autoSyncEnabled = enabled) }
        }
    }

    /** @param bootstrapStartMillis epoch-millis scan-window start, or 0 to continue from watermark. */
    fun startSync(bootstrapStartMillis: Long = 0L) {
        syncStatusBus.emit(SyncUiStatus.Syncing)
        syncManager.triggerManualSync(bootstrapStartMillis)
    }

    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) syncManager.enableAutoSync() else syncManager.disableAutoSync()
            syncPreferences.setAutoSyncEnabled(enabled)
            _uiState.update { it.copy(autoSyncEnabled = enabled) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun loadDrafts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            smsRepository.fetchDrafts()
                .onSuccess { drafts ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, drafts = drafts) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = exc.message) }
                }
        }
    }
}
