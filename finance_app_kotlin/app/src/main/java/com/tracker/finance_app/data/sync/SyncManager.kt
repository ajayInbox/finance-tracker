package com.tracker.finance_app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Manual sync. Pass a bootstrap epoch-millis to force the scan window start
     * (e.g. "last 30 days"); pass 0 to continue from the backend watermark.
     */
    fun triggerManualSync(bootstrapStartTimestampMillis: Long = 0L) {
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(MANUAL_SYNC_TAG)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    SyncWorker.KEY_BOOTSTRAP_START_TIMESTAMP to bootstrapStartTimestampMillis
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            MANUAL_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun enableAutoSync() {
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            AUTO_SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .addTag(AUTO_SYNC_TAG)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            AUTO_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun disableAutoSync() {
        workManager.cancelUniqueWork(AUTO_SYNC_WORK_NAME)
    }

    suspend fun isAutoSyncEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            workManager.getWorkInfosForUniqueWork(AUTO_SYNC_WORK_NAME).get().any { info ->
                info.state == WorkInfo.State.ENQUEUED || info.state == WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val AUTO_SYNC_INTERVAL_HOURS = 6L
        private const val AUTO_SYNC_WORK_NAME = "transaction_periodic_sync"
        private const val MANUAL_SYNC_WORK_NAME = "manual_sms_sync"
        private const val MANUAL_SYNC_TAG = "manual_sync"
        private const val AUTO_SYNC_TAG = "auto_sync"
    }
}
