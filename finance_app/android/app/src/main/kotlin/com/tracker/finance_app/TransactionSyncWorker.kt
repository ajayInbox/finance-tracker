package com.tracker.finance_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class TransactionSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TxnSyncWorker"
        const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID = 9001
        private const val DEFAULT_INITIAL_SYNC_DAYS = 30L

        const val KEY_STATUS = "status"
        const val KEY_COUNT = "count"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_BOOTSTRAP_START_TIMESTAMP = "bootstrap_start_timestamp"
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > 2) {
            Log.e(TAG, "Max retries reached. Giving up.")
            SyncStatusEmitter.emit("ERROR:Max retries exceeded")
            return Result.failure(workDataOf(KEY_STATUS to "FAILED"))
        }

        Log.d(TAG, "Starting sync (attempt ${runAttemptCount + 1})")
        SyncStatusEmitter.emit("SYNCING")

        return try {
            setForeground(createForegroundInfo())

            val latestTimestamp = SyncApiClient.getLatestTimestamp()
            val scanStartTimestamp = resolveScanStartTimestamp(latestTimestamp)
            Log.d(
                TAG,
                "Latest scanned timestamp: $latestTimestamp, effective start: $scanStartTimestamp"
            )

            val scanId = SyncApiClient.startScan()
            Log.d(TAG, "Scan started with id: $scanId")

            val transactions = querySmsInbox(scanStartTimestamp)
            Log.d(TAG, "Found ${transactions.length()} transaction SMS")

            if (transactions.length() > 0) {
                SyncApiClient.batchUpload(scanId, transactions)
            }

            val count = SyncApiClient.endScan(scanId)

            val now = System.currentTimeMillis()
            SyncStatusEmitter.emit("SUCCESS:$count:$now")

            Result.success(
                workDataOf(
                    KEY_STATUS to "SUCCESS",
                    KEY_COUNT to count,
                    KEY_TIMESTAMP to now
                )
            )
        } catch (e: IOException) {
            Log.w(TAG, "Network error on attempt $runAttemptCount", e)
            SyncStatusEmitter.emit("ERROR:${e.localizedMessage ?: "Network error"}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unrecoverable sync error", e)
            SyncStatusEmitter.emit("ERROR:${e.localizedMessage ?: "Unknown error"}")
            Result.failure(workDataOf(KEY_STATUS to "FAILED"))
        }
    }

    private fun resolveScanStartTimestamp(latestTimestamp: Long): Long {
        if (latestTimestamp > 0) {
            return latestTimestamp
        }

        val manualBootstrap = inputData.getLong(KEY_BOOTSTRAP_START_TIMESTAMP, 0L)
        if (manualBootstrap > 0) {
            return manualBootstrap
        }

        return System.currentTimeMillis() -
            TimeUnit.DAYS.toMillis(DEFAULT_INITIAL_SYNC_DAYS)
    }

    private fun querySmsInbox(sinceTimestamp: Long): JSONArray {
        val results = JSONArray()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("body", "address", "date")
        val selection = if (sinceTimestamp > 0) "date > ?" else null
        val selectionArgs = if (sinceTimestamp > 0) arrayOf(sinceTimestamp.toString()) else null
        val sortOrder = "date DESC"

        val cursor: Cursor? = applicationContext.contentResolver.query(
            uri, projection, selection, selectionArgs, sortOrder
        )

        cursor?.use {
            val bodyIdx = it.getColumnIndexOrThrow("body")
            val senderIdx = it.getColumnIndexOrThrow("address")
            val dateIdx = it.getColumnIndexOrThrow("date")

            while (it.moveToNext()) {
                val body = it.getString(bodyIdx) ?: continue
                val sender = it.getString(senderIdx) ?: ""
                val date = it.getLong(dateIdx)

                if (TransactionFilter.isTransaction(body)) {
                    val amount = extractAmount(body)
                    val hash = generateHash(body, date, amount)

                    val txn = JSONObject().apply {
                        put("body", body)
                        put("sender", sender)
                        put("timestamp", date)
                        put("uniqueIdentifier", hash)
                    }
                    results.put(txn)
                }
            }
        }
        return results
    }

    private fun extractAmount(body: String): Double {
        val regex = Regex("""(?:rs\.?|inr|₹)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val match = regex.find(body)
        return match?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull() ?: 0.0
    }

    private fun generateHash(body: String, date: Long, amount: Double): String {
        val input = "$body|$date|$amount"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
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
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
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
}
