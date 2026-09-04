package com.tracker.finance_app.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tracker.finance_app.data.local.SmsReader
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.remote.SmsMessagePayload
import com.tracker.finance_app.data.remote.SyncBatchUploadRequest
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun smsReader(): SmsReader
        fun financeApiService(): FinanceApiService
        fun syncPreferences(): SyncPreferences
        fun syncStatusBus(): SyncStatusBus
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > 2) {
            Log.e(TAG, "Max retries reached. Giving up.")
            entryPoint().syncStatusBus().emit(SyncUiStatus.Error("Max retries exceeded"))
            return Result.failure(workDataOf(KEY_STATUS to "FAILED"))
        }

        Log.d(TAG, "Starting sync (attempt ${runAttemptCount + 1})")
        val bus = entryPoint().syncStatusBus()
        bus.emit(SyncUiStatus.Syncing)

        return try {
            setForeground(createForegroundInfo())

            val ep = entryPoint()
            val api = ep.financeApiService()
            val smsReader = ep.smsReader()
            val prefs = ep.syncPreferences()

            // Step 1: watermark handshake with the backend
            val watermark = try {
                api.getSyncLatestTimestamp().watermark
            } catch (e: Exception) {
                Log.w(TAG, "Watermark fetch failed, using local last-sync", e)
                prefs.lastSyncTimestamp.first()
            }
            val scanStart = resolveScanStartTimestamp(watermark)
            Log.d(TAG, "Latest scanned timestamp: $watermark, effective start: $scanStart")

            // Step 2: batch upload candidate SMS messages
            val messages = queryTransactionSms(smsReader, scanStart)
            Log.d(TAG, "Found ${messages.size} transaction SMS")

            val count = if (messages.isNotEmpty()) {
                api.syncBatchUpload(SyncBatchUploadRequest(smsList = messages, fromTimestamp = scanStart)).newCount
            } else 0

            val now = System.currentTimeMillis()
            prefs.setLastSyncTimestamp(now)
            bus.emit(SyncUiStatus.Success(count, now))

            Result.success(
                workDataOf(
                    KEY_STATUS to "SUCCESS",
                    KEY_COUNT to count,
                    KEY_TIMESTAMP to now
                )
            )
        } catch (e: IOException) {
            Log.w(TAG, "Network error on attempt $runAttemptCount", e)
            bus.emit(SyncUiStatus.Error(e.localizedMessage ?: "Network error"))
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unrecoverable sync error", e)
            bus.emit(SyncUiStatus.Error(e.localizedMessage ?: "Unknown error"))
            Result.failure(workDataOf(KEY_STATUS to "FAILED"))
        }
    }

    private fun entryPoint(): SyncWorkerEntryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        SyncWorkerEntryPoint::class.java
    )

    private fun resolveScanStartTimestamp(watermark: Long): Long {
        // If the user explicitly chose a range, honour it
        val manualBootstrap = inputData.getLong(KEY_BOOTSTRAP_START_TIMESTAMP, 0L)
        if (manualBootstrap > 0) return manualBootstrap

        // Otherwise, continue from the backend watermark
        if (watermark > 0) return watermark

        // Fallback for first-ever sync with no explicit range
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DEFAULT_INITIAL_SYNC_DAYS)
    }

    private fun queryTransactionSms(smsReader: SmsReader, sinceTimestamp: Long): List<SmsMessagePayload> {
        if (sinceTimestamp <= 0) return emptyList()
        return smsReader.readMessagesSince(sinceTimestamp)
            .filter { TransactionFilter.isTransaction(it.body) }
            .map { msg ->
                SmsMessagePayload(
                    body = msg.body,
                    sender = msg.address,
                    timestamp = msg.date,
                    uniqueIdentifier = generateHash(msg.body, msg.date)
                )
            }
    }

    private fun generateHash(body: String, date: Long): String {
        val input = "$body|$date"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Syncing transactions...")
            .setContentText("Scanning SMS inbox for new transactions")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transaction Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress while scanning SMS inbox"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID = 9001
        private const val DEFAULT_INITIAL_SYNC_DAYS = 30L

        const val KEY_STATUS = "status"
        const val KEY_COUNT = "count"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_BOOTSTRAP_START_TIMESTAMP = "bootstrap_start_timestamp"
    }
}

object TransactionFilter {
    private val keywords = listOf("debited", "credited", "spent", "upi")

    fun isTransaction(text: String): Boolean {
        val msg = text.lowercase()
        return keywords.any { msg.contains(it) }
    }
}
