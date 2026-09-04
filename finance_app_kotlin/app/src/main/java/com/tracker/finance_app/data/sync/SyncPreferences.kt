package com.tracker.finance_app.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")
    private val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync_enabled")

    val lastSyncTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_KEY] ?: 0L
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }

    val autoSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SYNC_KEY] ?: false
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SYNC_KEY] = enabled
        }
    }

    suspend fun isAutoSyncEnabled(): Boolean = autoSyncEnabled.first()
}
