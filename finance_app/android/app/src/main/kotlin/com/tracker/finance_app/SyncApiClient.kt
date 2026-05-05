package com.tracker.finance_app

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client dedicated to the sync workflow endpoints.
 * Separate from [ApiClient] which handles single-message parsing.
 *
 * 2-step sync protocol:
 *   1. getLatestTimestamp  → watermark
 *   2. batchUpload         → upload SMS, backend handles scan lifecycle internally
 *
 * Includes automatic token refresh: on a 401 response, reads the refresh token
 * from FlutterSharedPreferences, calls /auth/refresh, saves the new tokens back,
 * and retries the original request once.
 */
object SyncApiClient {
    private const val TAG = "SYNC_API"

    private val BASE_URL = BuildConfig.BASE_URL

    // Paths for the 2-step sync protocol
    private const val LATEST_TIMESTAMP_PATH = "api/sync/latest-timestamp"
    private const val BATCH_UPLOAD_PATH = "api/sync/batch-upload"
    private const val REFRESH_PATH = "auth/refresh"

    private const val FLUTTER_PREFS = "FlutterSharedPreferences"
    private const val ACCESS_TOKEN_KEY = "flutter.access_token"
    private const val REFRESH_TOKEN_KEY = "flutter.refresh_token"

    // Generous timeout — the Render free-tier may cold-start
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun url(path: String): String =
        if (BASE_URL.endsWith("/")) "$BASE_URL$path" else "$BASE_URL/$path"

    private fun accessToken(context: Context): String? {
        return context
            .getSharedPreferences(FLUTTER_PREFS, Context.MODE_PRIVATE)
            ?.getString(ACCESS_TOKEN_KEY, null)
    }

    private fun refreshToken(context: Context): String? {
        return context
            .getSharedPreferences(FLUTTER_PREFS, Context.MODE_PRIVATE)
            ?.getString(REFRESH_TOKEN_KEY, null)
    }

    /**
     * Saves new access and refresh tokens back to FlutterSharedPreferences
     * so both the Kotlin and Dart sides stay in sync.
     */
    private fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context
            .getSharedPreferences(FLUTTER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    /**
     * Attempts to refresh the access token using the stored refresh token.
     * On success, saves both new tokens to SharedPreferences.
     * @return the new access token
     * @throws IOException if refresh fails (no refresh token, network error, or 401)
     */
    private fun refreshAccessToken(context: Context): String {
        val currentRefreshToken = refreshToken(context)
            ?: throw IOException("No refresh token available — user must re-authenticate")

        val payload = JSONObject().apply {
            put("refreshToken", currentRefreshToken)
        }
        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url(REFRESH_PATH))
            .post(requestBody)
            .build()

        Log.d(TAG, "POST ${url(REFRESH_PATH)} — refreshing access token")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Token refresh failed (${response.code}) — user must re-authenticate")
            }
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            val newAccessToken = json.getString("accessToken")
            val newRefreshToken = json.getString("refreshToken")
            saveTokens(context, newAccessToken, newRefreshToken)
            Log.d(TAG, "Token refresh successful — new tokens saved")
            return newAccessToken
        }
    }

    /**
     * Executes a request builder with automatic 401 retry.
     * If the initial request returns 401, refreshes the access token and retries once.
     */
    private fun executeWithRefresh(
        context: Context,
        buildRequest: (token: String?) -> Request
    ): okhttp3.Response {
        val token = accessToken(context)
        val response = client.newCall(buildRequest(token)).execute()

        if (response.code != 401) return response

        // Got 401 — attempt token refresh and retry once
        response.close()
        Log.d(TAG, "Received 401 — attempting token refresh")

        val newToken = refreshAccessToken(context) // throws if refresh fails
        return client.newCall(buildRequest(newToken)).execute()
    }

    // ── Step 1: Handshake ──────────────────────────────────────────────
    /** Returns the epoch-millis timestamp of the last scanned SMS. */
    fun getLatestTimestamp(context: Context): Long {
        Log.d(TAG, "GET ${url(LATEST_TIMESTAMP_PATH)}")

        executeWithRefresh(context) { token ->
            Request.Builder()
                .url(url(LATEST_TIMESTAMP_PATH))
                .applyAuth(token)
                .get()
                .build()
        }.use { response ->
            if (!response.isSuccessful) throw IOException("Handshake failed: $response")
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
    fun batchUpload(context: Context, transactions: JSONArray, fromTimestamp: Long): Int {
        val payload = JSONObject().apply {
            put("smsList", transactions)
            put("fromTimestamp", fromTimestamp)
        }
        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        Log.d(TAG, "POST ${url(BATCH_UPLOAD_PATH)} — ${transactions.length()} txns, from=$fromTimestamp")

        executeWithRefresh(context) { token ->
            Request.Builder()
                .url(url(BATCH_UPLOAD_PATH))
                .applyAuth(token)
                .post(requestBody)
                .build()
        }.use { response ->
            if (!response.isSuccessful) throw IOException("Batch upload failed: $response")
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            return json.optInt("newCount", 0)
        }
    }

    private fun Request.Builder.applyAuth(token: String?): Request.Builder {
        token?.takeIf { it.isNotBlank() }?.let {
            addHeader("Authorization", "Bearer $it")
        }
        return this
    }
}

