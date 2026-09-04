package com.tracker.finance_app.domain.repository

import com.tracker.finance_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesFlow(): Flow<List<Category>>
    suspend fun fetchCategories(): Result<List<Category>>
    suspend fun createCategory(
        name: String,
        groupName: String = "General",
        type: TransactionType = TransactionType.EXPENSE,
        parentId: String? = null,
        iconKey: String? = null,
        colorCode: String? = null
    ): Result<Category>
    suspend fun updateCategory(
        id: String,
        name: String,
        parentId: String? = null,
        iconKey: String? = null,
        colorCode: String? = null
    ): Result<Category>
    suspend fun deleteCategory(id: String): Result<Unit>
}
