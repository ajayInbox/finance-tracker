package com.tracker.finance_app

data class ParsedTxnResponse(
    val status: String,
    val uniqueIdentifier: String,
    val parsedTransaction: ParsedTransaction? = null,
)
