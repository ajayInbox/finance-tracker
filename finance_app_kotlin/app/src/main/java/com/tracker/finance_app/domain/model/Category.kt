package com.tracker.finance_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Serializable
data class Category(
    val id: String,
    val name: String,
    val groupName: String = "General",
    val type: TransactionType = TransactionType.EXPENSE,
    val iconName: String? = null,
    val colorHex: String? = null,
    val isSystem: Boolean = false
)

@Serializable
data class CategoryGroup(
    val name: String,
    val categories: List<Category>
)
