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
    private val tokenManager: TokenManager,
    private val accountRepositoryProvider: javax.inject.Provider<com.tracker.finance_app.domain.repository.AccountRepository>,
    private val transactionRepositoryProvider: javax.inject.Provider<com.tracker.finance_app.domain.repository.TransactionRepository>,
    private val categoryRepositoryProvider: javax.inject.Provider<com.tracker.finance_app.domain.repository.CategoryRepository>
) : AuthRepository {

    constructor(
        apiService: FinanceApiService,
        tokenManager: TokenManager
    ) : this(
        apiService,
        tokenManager,
        javax.inject.Provider { null as com.tracker.finance_app.domain.repository.AccountRepository? as com.tracker.finance_app.domain.repository.AccountRepository },
        javax.inject.Provider { null as com.tracker.finance_app.domain.repository.TransactionRepository? as com.tracker.finance_app.domain.repository.TransactionRepository },
        javax.inject.Provider { null as com.tracker.finance_app.domain.repository.CategoryRepository? as com.tracker.finance_app.domain.repository.CategoryRepository }
    )

    override val hasValidToken: Boolean
        get() = !tokenManager.cachedAccessToken.isNullOrBlank()

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
        val refreshToken = tokenManager.cachedRefreshToken
        tokenManager.clearTokens()
        try { accountRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
        try { transactionRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
        try { categoryRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
        if (refreshToken != null) {
            try {
                apiService.logout(refreshToken)
            } catch (_: Exception) { }
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return runCatching { apiService.sendPasswordReset(email) }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            apiService.deleteUserAccount()
            tokenManager.clearTokens()
            try { accountRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
            try { transactionRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
            try { categoryRepositoryProvider.get()?.clearCache() } catch (_: Exception) { }
        }
    }

    override fun getAuthTokenFlow(): Flow<String?> = tokenManager.accessTokenFlow
}
