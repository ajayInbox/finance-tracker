package com.tracker.finance_app.presentation.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val newName: String = "",
    val newGroupName: String = "General",
    val newType: TransactionType = TransactionType.EXPENSE,
    val isCreating: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            categoryRepository.fetchCategories()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, categories = list) }
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isLoading = false, error = exc.message) }
                }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(newName = name) }
    }

    fun onGroupNameChanged(groupName: String) {
        _uiState.update { it.copy(newGroupName = groupName) }
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.update { it.copy(newType = type) }
    }

    fun createCategory() {
        val name = _uiState.value.newName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            categoryRepository.createCategory(name, _uiState.value.newGroupName, _uiState.value.newType)
                .onSuccess {
                    _uiState.update { it.copy(isCreating = false, newName = "") }
                    loadCategories()
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isCreating = false, error = exc.message) }
                }
        }
    }

    fun createCategoryGroup(name: String, icon: String, colorHex: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            categoryRepository.createCategory(name = trimmed, groupName = trimmed, type = TransactionType.EXPENSE)
                .onSuccess {
                    _uiState.update { it.copy(isCreating = false) }
                    loadCategories()
                }
                .onFailure { exc ->
                    _uiState.update { it.copy(isCreating = false, error = exc.message) }
                }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id)
            loadCategories()
        }
    }
}
