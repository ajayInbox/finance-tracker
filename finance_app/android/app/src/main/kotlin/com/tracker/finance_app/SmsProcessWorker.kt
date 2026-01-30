package com.tracker.finance_app

import android.content.Context
import android.net.http.HttpException
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.IOException

class SmsProcessWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val sender = inputData.getString(KEY_SENDER).orEmpty()
        val timestamp = inputData.getLong(KEY_DATE, 0L)

        Log.d(TAG, "Processing SMS")

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

            // ✅ Emit event ONLY when backend created a new draft
            if (status == "CREATED") {
                ParsedTxnEventEmitter.emit(
                    uniqueIdentifier
                )
            }

            // DUPLICATE is a successful no-op
            Result.success()

        } catch (e: IOException) {
            Log.w(TAG, "Network issue, retrying", e)
            Result.retry()

        }  catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
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
