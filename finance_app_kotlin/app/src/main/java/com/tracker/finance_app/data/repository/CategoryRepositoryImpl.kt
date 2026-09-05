package com.tracker.finance_app.data.repository

import com.tracker.finance_app.data.remote.CreateCategoryRequest
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.remote.UpdateCategoryRequest
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val apiService: FinanceApiService
) : CategoryRepository {

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    private var isCacheValid = false

    override fun getCategoriesFlow(): Flow<List<Category>> = _categoriesState.asStateFlow()

    override suspend fun fetchCategories(forceRefresh: Boolean): Result<List<Category>> {
        if (!forceRefresh && isCacheValid && _categoriesState.value.isNotEmpty()) {
            return Result.success(_categoriesState.value)
        }
        return runCatching {
            val items = apiService.getCategories()
            _categoriesState.value = items
            isCacheValid = true
            items
        }
    }

    override suspend fun createCategory(
        name: String,
        groupName: String,
        type: TransactionType,
        parentId: String?,
        iconKey: String?,
        colorCode: String?
    ): Result<Category> {
        return runCatching {
            val request = CreateCategoryRequest(
                name = name,
                type = type.name,
                parentId = parentId,
                iconKey = iconKey ?: "default-folder",
                colorCode = colorCode ?: "#087B3D"
            )
            val item = apiService.createCategory(request)
            fetchCategories(forceRefresh = true)
            item
        }
    }

    override suspend fun updateCategory(
        id: String,
        name: String,
        parentId: String?,
        iconKey: String?,
        colorCode: String?
    ): Result<Category> {
        return runCatching {
            val request = UpdateCategoryRequest(
                name = name,
                parentId = parentId,
                iconKey = iconKey ?: "default-folder",
                colorCode = colorCode ?: "#087B3D"
            )
            val item = apiService.updateCategory(id, request)
            fetchCategories(forceRefresh = true)
            item
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return runCatching {
            apiService.deleteCategory(id)
            fetchCategories(forceRefresh = true)
        }
    }

    override fun invalidateCache() {
        isCacheValid = false
    }

    override fun clearCache() {
        _categoriesState.value = emptyList()
        isCacheValid = false
    }
}
