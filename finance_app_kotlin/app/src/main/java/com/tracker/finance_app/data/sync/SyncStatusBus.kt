package com.tracker.finance_app.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncUiStatus {
    data object Idle : SyncUiStatus()
    data object Syncing : SyncUiStatus()
    data class Success(val count: Int, val timestamp: Long) : SyncUiStatus()
    data class Error(val message: String) : SyncUiStatus()
}

@Singleton
class SyncStatusBus @Inject constructor() {
    private val _status = MutableStateFlow<SyncUiStatus>(SyncUiStatus.Idle)
    val status: StateFlow<SyncUiStatus> = _status.asStateFlow()

    fun emit(status: SyncUiStatus) {
        _status.value = status
    }
}
