package com.tracker.finance_app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object TransactionStore {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("txns", Context.MODE_PRIVATE)
    }

    fun save(json: String) {
        prefs.edit { putString("latest", json) }
    }

    fun getAll(): String? = prefs.getString("latest", null)
}
