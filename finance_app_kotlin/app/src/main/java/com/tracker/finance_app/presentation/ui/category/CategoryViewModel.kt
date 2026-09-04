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

enum class CategoryLevel {
    PARENT_GROUP,
    SUBCATEGORY
}

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val isCreating: Boolean = false,
    val expandedGroupIds: Set<String> = emptySet(),

    // Bottom Sheet State
    val isBottomSheetOpen: Boolean = false,
    val sheetLevel: CategoryLevel = CategoryLevel.PARENT_GROUP,
    val editingCategoryId: String? = null,
    val selectedParentGroup: Category? = null,
    val inputName: String = "",
    val selectedCategoryType: TransactionType = TransactionType.EXPENSE,
    val selectedColorHex: String = "#087B3D",
    val selectedIconKey: String = "🍽️",

    // Delete Dialog State
    val groupToDelete: Category? = null,

    // Legacy fields for backward compatibility
    val newName: String = "",
    val newGroupName: String = "General",
    val newType: TransactionType = TransactionType.EXPENSE
) {
    val isEditing: Boolean get() = editingCategoryId != null
}

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

    fun toggleGroupExpanded(groupId: String) {
        _uiState.update { current ->
            val newExpanded = if (current.expandedGroupIds.contains(groupId)) {
                current.expandedGroupIds - groupId
            } else {
                current.expandedGroupIds + groupId
            }
            current.copy(expandedGroupIds = newExpanded)
        }
    }

    fun openAddGroupSheet() {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = true,
                sheetLevel = CategoryLevel.PARENT_GROUP,
                editingCategoryId = null,
                selectedParentGroup = null,
                inputName = "",
                selectedCategoryType = TransactionType.EXPENSE,
                selectedColorHex = "#087B3D",
                selectedIconKey = "🍽️",
                error = null
            )
        }
    }

    fun openAddSubcategorySheet(parent: Category) {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = true,
                sheetLevel = CategoryLevel.SUBCATEGORY,
                editingCategoryId = null,
                selectedParentGroup = parent,
                inputName = "",
                selectedCategoryType = parent.type,
                selectedColorHex = parent.colorHex ?: "#087B3D",
                selectedIconKey = parent.iconName ?: "🍽️",
                error = null
            )
        }
    }

    fun openEditGroupSheet(group: Category) {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = true,
                sheetLevel = CategoryLevel.PARENT_GROUP,
                editingCategoryId = group.id,
                selectedParentGroup = null,
                inputName = group.name,
                selectedCategoryType = group.type,
                selectedColorHex = group.colorHex ?: "#087B3D",
                selectedIconKey = group.iconName ?: "🍽️",
                error = null
            )
        }
    }

    fun openEditSubcategorySheet(subcategory: Category, parent: Category?) {
        _uiState.update { current ->
            val actualParent = parent ?: current.categories.firstOrNull { it.id == subcategory.parentId }
            current.copy(
                isBottomSheetOpen = true,
                sheetLevel = CategoryLevel.SUBCATEGORY,
                editingCategoryId = subcategory.id,
                selectedParentGroup = actualParent,
                inputName = subcategory.name,
                selectedCategoryType = subcategory.type,
                selectedColorHex = subcategory.colorHex ?: actualParent?.colorHex ?: "#087B3D",
                selectedIconKey = subcategory.iconName ?: "🍽️",
                error = null
            )
        }
    }

    fun setSheetLevel(level: CategoryLevel) {
        _uiState.update { current ->
            val defaultParent = if (level == CategoryLevel.SUBCATEGORY && current.selectedParentGroup == null) {
                current.categories.firstOrNull { it.parentId == null }
            } else {
                current.selectedParentGroup
            }
            current.copy(
                sheetLevel = level,
                selectedParentGroup = defaultParent,
                selectedCategoryType = defaultParent?.type ?: current.selectedCategoryType
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update {
            it.copy(
                isBottomSheetOpen = false,
                editingCategoryId = null,
                inputName = ""
            )
        }
    }

    fun onInputNameChanged(name: String) {
        _uiState.update { it.copy(inputName = name) }
    }

    fun onCategoryTypeChanged(type: TransactionType) {
        _uiState.update { it.copy(selectedCategoryType = type) }
    }

    fun onParentGroupSelected(parent: Category) {
        _uiState.update { it.copy(selectedParentGroup = parent, selectedCategoryType = parent.type) }
    }

    fun onColorSelected(hex: String) {
        _uiState.update { it.copy(selectedColorHex = hex) }
    }

    fun onIconSelected(icon: String) {
        _uiState.update { it.copy(selectedIconKey = icon) }
    }

    fun saveCategory() {
        val state = _uiState.value
        val name = state.inputName.trim()
        if (name.isBlank()) return

        val parentId = if (state.sheetLevel == CategoryLevel.SUBCATEGORY) {
            state.selectedParentGroup?.id
        } else {
            null
        }

        val type = if (state.sheetLevel == CategoryLevel.SUBCATEGORY) {
            state.selectedParentGroup?.type ?: state.selectedCategoryType
        } else {
            state.selectedCategoryType
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            val result = if (state.editingCategoryId != null) {
                categoryRepository.updateCategory(
                    id = state.editingCategoryId,
                    name = name,
                    parentId = parentId,
                    iconKey = state.selectedIconKey,
                    colorCode = state.selectedColorHex
                )
            } else {
                categoryRepository.createCategory(
                    name = name,
                    groupName = state.selectedParentGroup?.name ?: name,
                    type = type,
                    parentId = parentId,
                    iconKey = state.selectedIconKey,
                    colorCode = state.selectedColorHex
                )
            }

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isCreating = false,
                        isBottomSheetOpen = false,
                        editingCategoryId = null,
                        inputName = ""
                    )
                }
                loadCategories()
            }.onFailure { exc ->
                _uiState.update { it.copy(isCreating = false, error = exc.message) }
            }
        }
    }

    fun requestDeleteGroup(group: Category) {
        _uiState.update { it.copy(groupToDelete = group) }
    }

    fun cancelDeleteGroup() {
        _uiState.update { it.copy(groupToDelete = null) }
    }

    fun confirmDeleteGroup() {
        val target = _uiState.value.groupToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(groupToDelete = null, isLoading = true) }
            categoryRepository.deleteCategory(target.id)
            loadCategories()
        }
    }

    fun deleteSubcategory(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.deleteCategory(id)
            loadCategories()
        }
    }

    // Legacy helpers
    fun onNameChanged(name: String) = onInputNameChanged(name)
    fun onGroupNameChanged(groupName: String) { _uiState.update { it.copy(newGroupName = groupName) } }
    fun onTypeChanged(type: TransactionType) = onCategoryTypeChanged(type)

    fun createCategory() {
        saveCategory()
    }

    fun createCategoryGroup(name: String, icon: String, colorHex: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            categoryRepository.createCategory(
                name = trimmed,
                groupName = trimmed,
                type = TransactionType.EXPENSE,
                parentId = null,
                iconKey = icon,
                colorCode = colorHex
            ).onSuccess {
                _uiState.update { it.copy(isCreating = false) }
                loadCategories()
            }.onFailure { exc ->
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
