package com.tracker.finance_app

object TransactionFilter {
    private val keywords = listOf(
        "debited", "credited", "spent",
        "upi", "txn", "transaction", "rs."
    )

    fun isTransaction(text: String): Boolean {
        val msg = text.lowercase()
        return keywords.any { msg.contains(it) }
    }
}
