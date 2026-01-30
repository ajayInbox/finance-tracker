package com.tracker.finance_app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.tracker.finance_app.TransactionFilter
import com.tracker.finance_app.TransactionWorker

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("SMS_DEBUG", "onReceive triggered")
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.e("SMS_DEBUG", "Not SMS_RECEIVED_ACTION")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Log.e("SMS_DEBUG", "Messages count = ${messages.size}")

        for (sms in messages) {
            Log.e(
                "SMS_DEBUG",
                "From=${sms.originatingAddress}, Body=${sms.messageBody}"
            )
            val body = sms.messageBody
            val sender = sms.originatingAddress ?: ""
            val date = sms.timestampMillis

            if (TransactionFilter.isTransaction(body)) {
                Log.e("SMS_DEBUG", "Transaction detected → enqueue worker")
                TransactionWorker.enqueue(context, body, sender, date)
            }
        }
    }
}