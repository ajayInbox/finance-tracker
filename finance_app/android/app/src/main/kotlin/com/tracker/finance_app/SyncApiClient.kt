package com.tracker.finance_app

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * HTTP client dedicated to the sync workflow endpoints.
 * Separate from [ApiClient] which handles single-message parsing.
 *
 * 2-step sync protocol:
 *   1. getLatestTimestamp  → watermark
 *   2. batchUpload         → upload SMS, backend handles scan lifecycle internally
 */
object SyncApiClient {
    private const val TAG = "SYNC_API"

    private val BASE_URL = BuildConfig.BASE_URL

    // Paths for the 2-step sync protocol
    private const val LATEST_TIMESTAMP_PATH = "api/sync/latest-timestamp"
    private const val BATCH_UPLOAD_PATH = "api/sync/batch-upload"

    private const val USER_ID = "960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"

    // Generous timeout — the Render free-tier may cold-start
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-User-Id", USER_ID)
                .build()
            chain.proceed(request)
        }
        .build()

    private fun url(path: String): String =
        if (BASE_URL.endsWith("/")) "$BASE_URL$path" else "$BASE_URL/$path"

    // ── Step 1: Handshake ──────────────────────────────────────────────
    /** Returns the epoch-millis timestamp of the last scanned SMS. */
    fun getLatestTimestamp(): Long {
        val request = Request.Builder().url(url(LATEST_TIMESTAMP_PATH)).get().build()
        Log.d(TAG, "GET ${url(LATEST_TIMESTAMP_PATH)}")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Handshake failed: $response")
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            return json.optLong("latestScannedTimestamp", 0L)
        }
    }

    // ── Step 2: Batch Upload ───────────────────────────────────────────
    /**
     * Uploads candidate SMS messages to the backend.
     * The backend handles scan lifecycle internally (start → process → complete).
     * Returns the number of draft transactions created.
     */
    fun batchUpload(transactions: JSONArray, fromTimestamp: Long): Int {
        val payload = JSONObject().apply {
            put("smsList", transactions)
            put("fromTimestamp", fromTimestamp)
        }
        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url(BATCH_UPLOAD_PATH)).post(requestBody).build()
        Log.d(TAG, "POST ${url(BATCH_UPLOAD_PATH)} — ${transactions.length()} txns, from=$fromTimestamp")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Batch upload failed: $response")
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            return json.optInt("newCount", 0)
        }
    }
}
