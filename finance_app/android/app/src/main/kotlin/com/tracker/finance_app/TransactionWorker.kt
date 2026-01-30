package com.tracker.finance_app

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

object TransactionWorker {

    fun enqueue(context: Context, body: String, sender: String, date: Long) {
        Log.e("WORK_DEBUG", "Enqueue worker called")
        val data = workDataOf(
            "body" to body,
            "sender" to sender,
            "date" to date
        )

        val request = OneTimeWorkRequestBuilder<SmsProcessWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
