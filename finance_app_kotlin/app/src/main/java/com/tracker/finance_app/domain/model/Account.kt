package com.tracker.finance_app.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Account(
    val id: String,
    @JsonNames("name", "accountName")
    val name: String,
    @JsonNames("accountNumber", "lastFour")
    val accountNumber: String? = null,
    val institution: String? = null,
    @JsonNames("type", "accountType")
    val type: AccountType,
    val category: AccountCategory,
    @JsonNames("balance", "currentBalance")
    val balance: Double = 0.0,
    val currency: String = "INR",
    val creditLimit: Double? = null,
    val availableCredit: Double? = null,
    @JsonNames("isActive", "active")
    val isActive: Boolean = true
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AccountCreateUpdateRequest(
    @JsonNames("name", "accountName")
    val name: String,
    val lastFour: String? = null,
    val institution: String? = null,
    @JsonNames("type", "accountType")
    val type: AccountType,
    val category: AccountCategory,
    val balance: Double,
    val currency: String = "INR",
    val creditLimit: Double? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NetWorthSummary(
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netWorth: Double = 0.0,
    val currency: String = "INR"
)

