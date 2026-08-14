package com.tracker.finance_app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tracker.finance_app.core.util.MessageParser
import com.tracker.finance_app.data.local.SmsReader
import com.tracker.finance_app.data.remote.SmsMessagePayload
import com.tracker.finance_app.domain.repository.TransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun smsReader(): SmsReader
        fun transactionRepository(): TransactionRepository
        fun syncPreferences(): SyncPreferences
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SyncWorkerEntryPoint::class.java
            )
            
            val smsReader = entryPoint.smsReader()
            val repository = entryPoint.transactionRepository()
            val syncPreferences = entryPoint.syncPreferences()

            val lastSync = syncPreferences.lastSyncTimestamp.first()
            val currentSync = System.currentTimeMillis()
            
            val messages = smsReader.readMessagesSince(lastSync)
            if (messages.isNotEmpty()) {
                val payloads = messages.mapNotNull { msg ->
                    val parsed = MessageParser.parse(msg.body)
                    if (parsed.isValid) {
                        SmsMessagePayload(
                            sender = msg.address,
                            body = msg.body,
                            timestamp = msg.date
                        )
                    } else null
                }
                
                if (payloads.isNotEmpty()) {
                    val result = repository.exportSmsMessages(payloads)
                    if (result.isSuccess) {
                        syncPreferences.setLastSyncTimestamp(currentSync)
                    } else {
                        return Result.retry()
                    }
                } else {
                    syncPreferences.setLastSyncTimestamp(currentSync)
                }
            } else {
                syncPreferences.setLastSyncTimestamp(currentSync)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
