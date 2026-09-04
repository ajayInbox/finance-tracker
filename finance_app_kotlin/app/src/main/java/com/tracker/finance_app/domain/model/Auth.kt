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
enum class UserType { REGULAR, PRO, PREMIUM }

@Serializable
enum class DashboardMode { EXPENSE_ONLY, EXPENSE_AND_ACCOUNT }

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String? = null,
    val preferredCurrency: String = "INR",
    val userType: UserType = UserType.REGULAR,
    val dashboardMode: DashboardMode = DashboardMode.EXPENSE_ONLY
)
