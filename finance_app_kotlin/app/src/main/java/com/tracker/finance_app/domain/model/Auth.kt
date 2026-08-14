package com.tracker.finance_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInMillis: Long? = null
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val preferredCurrency: String = "USD",
    val isOnboarded: Boolean = true
)
