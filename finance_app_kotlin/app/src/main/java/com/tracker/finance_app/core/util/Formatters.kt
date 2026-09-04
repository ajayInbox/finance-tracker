package com.tracker.finance_app.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {

    private val amountRegex = Regex("""^\d{1,13}(\.\d{1,4})?$""")

    fun parseAmountOrNull(text: String): Double? {
        val normalized = text.trim().replace(",", "")
        if (!amountRegex.matches(normalized)) return null
        val value = normalized.toDoubleOrNull() ?: return null
        return if (value.isFinite()) value else null
    }

    fun formatCurrency(amount: Double, currencyCode: String = "INR"): String {
        val locale = if (currencyCode.uppercase() == "INR") Locale("en", "IN") else Locale.US
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currencyCode.uppercase())
        return format.format(amount)
    }

    fun formatDate(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }
}
