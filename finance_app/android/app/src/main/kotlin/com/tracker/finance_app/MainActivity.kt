package com.tracker.finance_app

import androidx.lifecycle.Observer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : FlutterActivity() {
    companion object {
        private const val AUTO_SYNC_WORK_NAME = "transaction_periodic_sync"
    }

    override fun configureFlutterEngine(engine: FlutterEngine) {
        super.configureFlutterEngine(engine)

        MethodChannel(
            engine.dartExecutor.binaryMessenger,
            "com.tracker.finance_app/sync"
        ).setMethodCallHandler { call, result ->
            val workManager = WorkManager.getInstance(applicationContext)

            when (call.method) {
                "startManualSync" -> {
                    val bootstrapStartTimestampMillis =
                        call.argument<Number>("bootstrapStartTimestampMillis")
                            ?.toLong()

                    val request = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
                        .addTag("manual_sync")
                        .setInputData(
                            workDataOf(
                                TransactionSyncWorker.KEY_BOOTSTRAP_START_TIMESTAMP to
                                    bootstrapStartTimestampMillis
                            )
                        )
                        .build()

                    workManager.enqueue(request)
                    observeSyncRequest(workManager, request.id)
                    result.success(null)
                }

                "enableAutoSync" -> {
                    val request = PeriodicWorkRequestBuilder<TransactionSyncWorker>(
                        6, TimeUnit.HOURS
                    )
                        .addTag("auto_sync")
                        .build()

                    workManager.enqueueUniquePeriodicWork(
                        AUTO_SYNC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        request
                    )
                    result.success(null)
                }

                "disableAutoSync" -> {
                    workManager.cancelUniqueWork(AUTO_SYNC_WORK_NAME)
                    result.success(null)
                }

                "isAutoSyncEnabled" -> {
                    val infos = workManager.getWorkInfosForUniqueWork(
                        AUTO_SYNC_WORK_NAME
                    ).get()

                    val enabled = infos.any { info ->
                        info.state == WorkInfo.State.ENQUEUED ||
                            info.state == WorkInfo.State.RUNNING
                    }
                    result.success(enabled)
                }

                else -> result.notImplemented()
            }
        }

        EventChannel(
            engine.dartExecutor.binaryMessenger,
            "sync_status_events"
        ).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                SyncStatusEmitter.setSink(events)
            }

            override fun onCancel(arguments: Any?) {
                SyncStatusEmitter.setSink(null)
            }
        })
    }

    private fun observeSyncRequest(workManager: WorkManager, requestId: UUID) {
        workManager.getWorkInfoByIdLiveData(requestId)
            .observe(this@MainActivity, Observer { workInfo ->
                workInfo ?: return@Observer

                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        SyncStatusEmitter.emit("SYNCING")
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val count = workInfo.outputData.getInt(
                            TransactionSyncWorker.KEY_COUNT, 0
                        )
                        val timestamp = workInfo.outputData.getLong(
                            TransactionSyncWorker.KEY_TIMESTAMP,
                            System.currentTimeMillis()
                        )
                        SyncStatusEmitter.emit("SUCCESS:$count:$timestamp")
                    }

                    WorkInfo.State.FAILED -> {
                        SyncStatusEmitter.emit("ERROR:Sync failed")
                    }

                    else -> {
                    }
                }
            })
    }
}
