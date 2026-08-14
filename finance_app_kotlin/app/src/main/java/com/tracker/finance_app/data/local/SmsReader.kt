package com.tracker.finance_app.data.local

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class SmsMessage(
    val address: String,
    val body: String,
    val date: Long
)

class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readMessagesSince(timestamp: Long): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(timestamp.toString())
        val sortOrder = "${Telephony.Sms.DATE} DESC"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex) ?: ""
                val body = cursor.getString(bodyIndex) ?: ""
                val date = cursor.getLong(dateIndex)
                messages.add(SmsMessage(address, body, date))
            }
        }
        return messages
    }
}
