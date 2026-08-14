package com.tracker.finance_app.core.network

import com.tracker.finance_app.data.local.TokenManager
import com.tracker.finance_app.domain.model.AuthTokens
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val json = Json { ignoreUnknownKeys = true }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Skip auth for login/register/refresh endpoints
        if (isAuthEndpoint(path)) {
            return chain.proceed(originalRequest)
        }

        // Add token to request
        val token = tokenManager.cachedAccessToken
        val authenticatedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // If 401, try to refresh token and retry once
        if (response.code == 401) {
            response.close()
            val newToken = tryRefreshToken(chain)
            if (newToken != null) {
                val retryRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
            // If refresh failed, clear tokens (force re-login)
            tokenManager.clearTokens()
        }

        return response
    }

    private fun tryRefreshToken(chain: Interceptor.Chain): String? {
        val refreshToken = tokenManager.cachedRefreshToken ?: return null
        val refreshBody = """{"refreshToken":"$refreshToken"}"""
            .toRequestBody("application/json".toMediaType())

        val refreshRequest = Request.Builder()
            .url(
                chain.request().url.newBuilder()
                    .encodedPath(ApiConstants.REFRESH)
                    .build()
            )
            .post(refreshBody)
            .build()

        return try {
            val refreshResponse = chain.proceed(refreshRequest)
            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string()
                refreshResponse.close()
                if (body != null) {
                    val tokens = json.decodeFromString<AuthTokens>(body)
                    tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                    tokens.accessToken
                } else null
            } else {
                refreshResponse.close()
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isAuthEndpoint(path: String): Boolean {
        return path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh")
    }
}
