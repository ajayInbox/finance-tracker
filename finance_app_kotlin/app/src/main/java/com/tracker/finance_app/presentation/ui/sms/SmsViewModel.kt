package com.tracker.finance_app.presentation.ui.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.model.TransactionDraft
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.domain.repository.SmsRepository
import com.tracker.finance_app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsReviewUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val drafts: List<TransactionDraft> = emptyList(),
    val error: String? = null,
    val approvedCount: Int = 0
)

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsReviewUiState())
    val uiState: StateFlow<SmsReviewUiState> = _uiState.asStateFlow()

    init {
        loadDrafts()
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

    fun approveDraft(draft: TransactionDraft, accountId: String) {
        viewModelScope.launch {
            transactionRepository.addTransaction(
                accountId = accountId,
                amount = draft.amount,
                type = draft.type,
                description = draft.merchant ?: "SMS Transaction",
                categoryId = null
            ).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        drafts = state.drafts - draft,
                        approvedCount = state.approvedCount + 1
                    )
                }
            }.onFailure { exc ->
                _uiState.update { it.copy(error = exc.message) }
            }
        }
    }

    fun rejectDraft(draft: TransactionDraft) {
        _uiState.update { state ->
            state.copy(drafts = state.drafts - draft)
        }
    }

    fun approveAll(accountId: String) {
        val currentDrafts = _uiState.value.drafts.toList()
        currentDrafts.forEach { draft ->
            approveDraft(draft, accountId)
        }
    }
}
