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
 */
object SyncApiClient {
    private const val TAG = "SYNC_API"

    private val BASE_URL = BuildConfig.BASE_URL

    // Paths for the 4-step sync protocol
    private const val LATEST_TIMESTAMP_PATH = "api/sync/latest-timestamp"
    private const val START_SCAN_PATH = "api/sync/start"
    private const val BATCH_UPLOAD_PATH = "api/sync/batch-upload"
    private const val END_SCAN_PATH = "api/sync/end"

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

    // ── Step 2: Announce ───────────────────────────────────────────────
    /** Tells backend a scan is starting. Returns scanId. */
    fun startScan(): String {
        val requestBody = "{}".toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url(START_SCAN_PATH)).post(requestBody).build()
        Log.d(TAG, "POST ${url(START_SCAN_PATH)}")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Start scan failed: $response")
            val body = response.body?.string() ?: "{}"
            return JSONObject(body).optString("scanId", "")
        }
    }

    // ── Step 3: Batch Upload ───────────────────────────────────────────
    fun batchUpload(scanId: String, transactions: JSONArray) {
        val payload = JSONObject().apply {
            put("scanId", scanId)
            put("smsList", transactions)
        }
        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url(BATCH_UPLOAD_PATH)).post(requestBody).build()
        Log.d(TAG, "POST ${url(BATCH_UPLOAD_PATH)} — ${transactions.length()} txns")
        Log.d(TAG, "Request body: ${payload.toString()}")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Batch upload failed: $response")
        }
    }

    // ── Step 4: Finalize ───────────────────────────────────────────────
    fun endScan(scanId: String): Int {
        val endScanUrl = url(END_SCAN_PATH) + "?scanId=$scanId"
        val request = Request.Builder().url(endScanUrl).post("".toRequestBody(null)).build()
        Log.d(TAG, "POST ${url(END_SCAN_PATH)} — scanId=$scanId")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("End scan failed: $response")
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            return json.optInt("transactionsCreated", 0)
        }
    }
}
