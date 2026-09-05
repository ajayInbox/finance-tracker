package com.tracker.finance_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    UNKNOWN
}

@Serializable
data class Category(
    val id: String = "",
    val name: String = "",
    val groupName: String = "General",
    val type: TransactionType = TransactionType.EXPENSE,
    @SerialName("iconKey") val iconName: String? = null,
    @SerialName("colorCode") val colorHex: String? = null,
    val isSystem: Boolean = false,
    val parentId: String? = null,
    val children: List<Category> = emptyList()
)

@Serializable
data class CategoryGroup(
    val name: String,
    val categories: List<Category>
)
