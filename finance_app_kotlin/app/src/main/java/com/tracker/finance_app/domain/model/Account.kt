package com.tracker.finance_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType(val label: String) {
    BANK("Bank Account"),
    SAVINGS("Savings"),
    CHECKING("Checking"),
    CASH("Cash"),
    WALLET("Digital Wallet"),
    CREDIT_CARD("Credit Card"),
    LOAN("Loan"),
    INVESTMENT("Investment"),
    UNKNOWN("Other")
}

@Serializable
enum class AccountCategory {
    ASSET,
    LIABILITY
}

@Serializable
data class Account(
    val id: String,
    val name: String,
    val accountNumber: String? = null,
    val institution: String? = null,
    val type: AccountType,
    val category: AccountCategory,
    val balance: Double,
    val currency: String = "INR",
    val creditLimit: Double? = null,
    val availableCredit: Double? = null,
    val isActive: Boolean = true
)

@Serializable
data class AccountCreateUpdateRequest(
    val name: String,
    val accountNumber: String? = null,
    val institution: String? = null,
    val type: AccountType,
    val category: AccountCategory,
    val balance: Double,
    val currency: String = "INR",
    val creditLimit: Double? = null
)

@Serializable
data class NetWorthSummary(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val currency: String = "INR"
)
