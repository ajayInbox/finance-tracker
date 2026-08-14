package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.local.TokenManager
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.remote.SignInRequest
import com.tracker.finance_app.data.remote.SignUpRequest
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<AuthTokens> {
        return runCatching {
            val response = apiService.signIn(SignInRequest(email, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            response
        }
    }

    override suspend fun signUp(email: String, password: String, name: String?, lastName: String?): Result<AuthTokens> {
        return runCatching {
            val response = apiService.signUp(SignUpRequest(email, password, name, lastName))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            response
        }
    }

    override suspend fun getUserProfile(): Result<UserProfile> {
        return runCatching { apiService.getUserProfile() }
    }

    override suspend fun signOut() {
        try {
            val refreshToken = tokenManager.cachedRefreshToken
            if (refreshToken != null) {
                apiService.logout(refreshToken)
            }
        } catch (_: Exception) { }
        tokenManager.clearTokens()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return runCatching { apiService.sendPasswordReset(email) }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            apiService.deleteUserAccount()
            tokenManager.clearTokens()
        }
    }

    override fun getAuthTokenFlow(): Flow<String?> = tokenManager.accessTokenFlow
}
