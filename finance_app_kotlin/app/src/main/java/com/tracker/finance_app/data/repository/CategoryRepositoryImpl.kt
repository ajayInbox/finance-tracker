package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : CategoryRepository {

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())

    override fun getCategoriesFlow(): Flow<List<Category>> = _categoriesState.asStateFlow()

    override suspend fun fetchCategories(): Result<List<Category>> {
        return runCatching {
            val items = apiService.getCategories()
            _categoriesState.value = items
            items
        }
    }

    override suspend fun createCategory(
        name: String,
        groupName: String,
        type: TransactionType
    ): Result<Category> {
        return runCatching {
            val cat = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                groupName = groupName,
                type = type
            )
            val item = apiService.createCategory(cat)
            _categoriesState.value = _categoriesState.value + item
            item
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteCategory(id)
            _categoriesState.value = _categoriesState.value.filterNot { it.id == id }
        }
    }
}
