package com.tracker.finance_app

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.IOException

class SmsProcessWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Check if we have tried too many times (e.g., 3 attempts max)
        // This prevents infinite loops on free-tier sleep cycles.
        if (runAttemptCount > 2) {
            Log.e(TAG, "Max retries (3) reached for this SMS. Giving up to save battery.")
            return Result.failure() 
        }

        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val sender = inputData.getString(KEY_SENDER).orEmpty()
        val timestamp = inputData.getLong(KEY_DATE, 0L)

        Log.d(TAG, "Processing SMS (Attempt ${runAttemptCount + 1})")

        return try {
            val response = ApiClient.parseTransaction(
                body = body,
                sender = sender,
                date = timestamp
            )

            val json = org.json.JSONObject(response)
            val status = json.optString("status")
            val uniqueIdentifier = json.optString("uniqueIdentifier")

            Log.d(TAG, "Parse status=${status}")

            if (status == "CREATED") {
                ParsedTxnEventEmitter.emit(uniqueIdentifier)
            }

            Result.success()

        } catch (e: IOException) {
            // This catches "Connection Refused" (ECONNREFUSED) or timeouts
            Log.w(TAG, "Network issue on attempt $runAttemptCount, scheduling retry", e)
            
            // WorkManager will use the BackoffPolicy defined when the work was enqueued
            Result.retry()

        } catch (e: Exception) {
            // For logic errors (JSON parsing, null pointers), don't retry.
            Log.e(TAG, "Unexpected non-recoverable error", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SmsProcessWorker"
        const val KEY_BODY = "body"
        const val KEY_SENDER = "sender"
        const val KEY_DATE = "date"
    }
}