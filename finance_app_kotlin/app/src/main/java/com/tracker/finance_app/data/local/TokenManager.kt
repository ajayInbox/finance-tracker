package com.tracker.finance_app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "auth_tokens_encrypted",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _accessTokenFlow = MutableStateFlow<String?>(
        sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    )
    val accessTokenFlow: Flow<String?> = _accessTokenFlow.asStateFlow()

    private val _refreshTokenFlow = MutableStateFlow<String?>(
        sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    )
    val refreshTokenFlow: Flow<String?> = _refreshTokenFlow.asStateFlow()

    val cachedAccessToken: String?
        get() = _accessTokenFlow.value

    val cachedRefreshToken: String?
        get() = _refreshTokenFlow.value

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        _accessTokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
