package com.tracker.finance_app

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "API_DEBUG"

    // Use the BuildConfig value we set up earlier
    private val BASE_URL = BuildConfig.BASE_URL 
    private const val PARSE_PATH = "api/v1/transactions/parse"

    // 1-minute timeout is perfect for waking up sleeping free-tier instances
    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES) 
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        // Helps OkHttp recover from minor socket drops during the "wake up"
        .retryOnConnectionFailure(true) 
        .build()    

    fun parseTransaction(body: String, sender: String, date: Long): String {
        Log.d(TAG, "parseTransaction called")

        val json = JSONObject().apply {
            put("sender", sender)
            put("body", body)
            put("uniqueIdentifier", "") // Placeholder as per your schema
            put("timestamp", date)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        // Ensure there is exactly one slash between base and path
        val fullUrl = if (BASE_URL.endsWith("/")) "$BASE_URL$PARSE_PATH" else "$BASE_URL/$PARSE_PATH"

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        Log.d(TAG, "Sending request to: $fullUrl")

        // .execute() is synchronous, which is exactly what CoroutineWorker 
        // expects inside its background thread.
        client.newCall(request).execute().use { response ->
            Log.d(TAG, "HTTP status = ${response.code}")

            if (!response.isSuccessful) {
                // Throwing here triggers the 'catch' block in SmsProcessWorker, 
                // which then triggers a 'Result.retry()'
                throw java.io.IOException("Unexpected code $response")
            }

            return response.body?.string() ?: ""
        }
    }
}