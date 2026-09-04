package com.tracker.finance_app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = createPrefs(context)

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

    private fun createPrefs(context: Context): SharedPreferences {
        return try {
            openEncrypted(context)
        } catch (first: Exception) {
            Log.e(TAG, "Encrypted token storage unavailable, attempting recovery", first)
            try {
                resetKeyStoreAndPrefs(context)
                openEncrypted(context)
            } catch (second: Exception) {
                Log.e(TAG, "Recovery failed, falling back to plain prefs", second)
                context.getSharedPreferences(PREFS_NAME_FALLBACK, Context.MODE_PRIVATE)
            }
        }
    }

    private fun openEncrypted(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME_ENCRYPTED,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun resetKeyStoreAndPrefs(context: Context) {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry(KEY_STORE_ALIAS)
        }
        File(context.filesDir.parentFile, "shared_prefs/$PREFS_NAME_ENCRYPTED.xml").delete()
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME_ENCRYPTED = "auth_tokens_encrypted"
        private const val PREFS_NAME_FALLBACK = "auth_tokens"
        private const val KEY_STORE_ALIAS = "_androidx_security_crypto_encrypted_prefs_"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
