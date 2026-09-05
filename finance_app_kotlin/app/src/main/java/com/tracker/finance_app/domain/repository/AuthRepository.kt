package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val hasValidToken: Boolean
    suspend fun signIn(email: String, password: String): Result<AuthTokens>
    suspend fun signUp(email: String, password: String, name: String?, lastName: String?): Result<AuthTokens>
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    fun getAuthTokenFlow(): Flow<String?>
}
