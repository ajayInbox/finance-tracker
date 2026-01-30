package com.tracker.finance_app

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    const val TAG = "API_DEBUG"

    // ⚠️ Use real server IP, NOT localhost
    const val BASE_URL = "http://10.0.2.2:8080"
    const val PARSE_ENDPOINT = "/api/v1/transactions/parse"

    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun parseTransaction(body: String, sender: String, date: Long): String {

        Log.e(TAG, "parseTransaction called")

        val json = JSONObject().apply {
            put("sender", sender)
            put("body", body)
            put("uniqueIdentifier", "")
            put("timestamp", date )
        }

        val requestBody = json
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(BASE_URL + PARSE_ENDPOINT)
            .post(requestBody)
            .build()

        Log.e(TAG, "Sending request → $json")

        client.newCall(request).execute().use { response ->

            Log.e(TAG, "HTTP status = ${response.code}")

            if (!response.isSuccessful) {
                throw Exception("API failed: ${response.code}")
            }

            val responseData = response.body?.string().orEmpty()

            Log.d(TAG, "Response = $responseData")

            return responseData
        }
    }
}
