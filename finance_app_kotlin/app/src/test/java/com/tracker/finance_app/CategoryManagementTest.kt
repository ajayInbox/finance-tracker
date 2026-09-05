package com.tracker.finance_app

import com.tracker.finance_app.data.remote.CreateCategoryRequest
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.repository.CategoryRepositoryImpl
import com.tracker.finance_app.domain.model.Category
import com.tracker.finance_app.domain.model.TransactionType
import com.tracker.finance_app.presentation.ui.category.CategoryLevel
import com.tracker.finance_app.presentation.ui.category.CategoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryManagementTest {

    private val testDispatcher = StandardTestDispatcher()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Category serializes and deserializes correctly with hierarchy and backend field mappings`() {
        val jsonString = """
            {
                "id": "group-1",
                "name": "Food & Dining",
                "type": "EXPENSE",
                "iconKey": "🍽️",
                "colorCode": "#F97316",
                "parentId": null,
                "children": [
                    {
                        "id": "sub-1",
                        "name": "Groceries",
                        "type": "EXPENSE",
                        "parentId": "group-1",
                        "iconKey": "🛒",
                        "colorCode": "#F97316"
                    }
                ]
            }
        """.trimIndent()

        val category = json.decodeFromString<Category>(jsonString)
        assertEquals("group-1", category.id)
        assertEquals("Food & Dining", category.name)
        assertEquals(TransactionType.EXPENSE, category.type)
        assertEquals("🍽️", category.iconName)
        assertEquals("#F97316", category.colorHex)
        assertNull(category.parentId)
        assertEquals(1, category.children.size)

        val sub = category.children.first()
        assertEquals("sub-1", sub.id)
        assertEquals("Groceries", sub.name)
        assertEquals("group-1", sub.parentId)
        assertEquals("🛒", sub.iconName)
    }

    @Test
    fun `CreateCategoryRequest serializes correctly for Parent Group and Subcategory`() {
        // Parent Group: parentId is null
        val groupReq = CreateCategoryRequest(
            name = "Salary & Professional",
            type = "INCOME",
            parentId = null,
            iconKey = "💼",
            colorCode = "#087B3D"
        )
        val groupJson = json.encodeToString(groupReq)
        assertTrue(groupJson.contains("\"name\":\"Salary & Professional\""))
        assertTrue(groupJson.contains("\"type\":\"INCOME\""))
        assertTrue(groupJson.contains("\"parentId\":null"))

        // Subcategory: parentId is present
        val subReq = CreateCategoryRequest(
            name = "Freelancing",
            type = "INCOME",
            parentId = "group-income-id",
            iconKey = "💰",
            colorCode = "#087B3D"
        )
        val subJson = json.encodeToString(subReq)
        assertTrue(subJson.contains("\"parentId\":\"group-income-id\""))
        assertTrue(subJson.contains("\"name\":\"Freelancing\""))
    }

    @Test
    fun `CategoryRepositoryImpl createCategory passes hierarchy parameters correctly`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val slot = slot<CreateCategoryRequest>()

        val createdSub = Category(
            id = "sub-created",
            name = "Fuel & Gas",
            type = TransactionType.EXPENSE,
            parentId = "group-transport-id"
        )

        coEvery { apiService.createCategory(capture(slot)) } returns createdSub
        coEvery { apiService.getCategories() } returns listOf(
            Category(id = "group-transport-id", name = "Transportation", children = listOf(createdSub))
        )

        val repo = CategoryRepositoryImpl(apiService)
        val result = repo.createCategory(
            name = "Fuel & Gas",
            groupName = "Transportation",
            type = TransactionType.EXPENSE,
            parentId = "group-transport-id",
            iconKey = "🚗",
            colorCode = "#3B82F6"
        )

        assertTrue(result.isSuccess)
        val captured = slot.captured
        assertEquals("Fuel & Gas", captured.name)
        assertEquals("EXPENSE", captured.type)
        assertEquals("group-transport-id", captured.parentId)
        assertEquals("🚗", captured.iconKey)
        assertEquals("#3B82F6", captured.colorCode)

        coVerify(exactly = 1) { apiService.createCategory(any()) }
    }

    @Test
    fun `CategoryViewModel handles expand toggle, bottom sheet state and delete confirmation`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val group = Category(id = "group-1", name = "Housing & Bills")

        coEvery { apiService.getCategories() } returns listOf(group)
        coEvery { apiService.deleteCategory("group-1") } returns Unit

        val repo = CategoryRepositoryImpl(apiService)
        val viewModel = CategoryViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Test expand toggle
        assertFalse(viewModel.uiState.value.expandedGroupIds.contains("group-1"))
        viewModel.toggleGroupExpanded("group-1")
        assertTrue(viewModel.uiState.value.expandedGroupIds.contains("group-1"))
        viewModel.toggleGroupExpanded("group-1")
        assertFalse(viewModel.uiState.value.expandedGroupIds.contains("group-1"))

        // 2. Test bottom sheet open & level change
        viewModel.openAddGroupSheet()
        assertTrue(viewModel.uiState.value.isBottomSheetOpen)
        assertEquals(CategoryLevel.PARENT_GROUP, viewModel.uiState.value.sheetLevel)

        viewModel.setSheetLevel(CategoryLevel.SUBCATEGORY)
        assertEquals(CategoryLevel.SUBCATEGORY, viewModel.uiState.value.sheetLevel)

        viewModel.onInputNameChanged("Electricity")
        assertEquals("Electricity", viewModel.uiState.value.inputName)

        viewModel.closeBottomSheet()
        assertFalse(viewModel.uiState.value.isBottomSheetOpen)

        // 3. Test delete confirmation flow
        viewModel.requestDeleteGroup(group)
        assertEquals(group, viewModel.uiState.value.groupToDelete)

        viewModel.confirmDeleteGroup()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.groupToDelete)

        coVerify(exactly = 1) { apiService.deleteCategory("group-1") }
    }

    @Test
    fun `CategoryViewModel handles editing of parent group and subcategory`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val parentGroup = Category(id = "group-food", name = "Food", iconName = "🍽️", colorHex = "#F97316")
        val subcategory = Category(id = "sub-groceries", name = "Groceries", parentId = "group-food", iconName = "🛒", colorHex = "#F97316")

        val updateSlot = slot<com.tracker.finance_app.data.remote.UpdateCategoryRequest>()

        coEvery { apiService.getCategories() } returns listOf(parentGroup)
        coEvery { apiService.updateCategory(any(), capture(updateSlot)) } returns parentGroup

        val repo = CategoryRepositoryImpl(apiService)
        val viewModel = CategoryViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Test opening Edit for Parent Group
        viewModel.openEditGroupSheet(parentGroup)
        assertTrue(viewModel.uiState.value.isBottomSheetOpen)
        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("group-food", viewModel.uiState.value.editingCategoryId)
        assertEquals("Food", viewModel.uiState.value.inputName)
        assertEquals("🍽️", viewModel.uiState.value.selectedIconKey)

        // 2. Modify name and save update
        viewModel.onInputNameChanged("Food & Groceries")
        viewModel.saveCategory()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Food & Groceries", updateSlot.captured.name)
        assertNull(viewModel.uiState.value.editingCategoryId)
        assertFalse(viewModel.uiState.value.isBottomSheetOpen)

        // 3. Test opening Edit for Subcategory
        viewModel.openEditSubcategorySheet(subcategory, parentGroup)
        assertTrue(viewModel.uiState.value.isBottomSheetOpen)
        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("sub-groceries", viewModel.uiState.value.editingCategoryId)
        assertEquals(CategoryLevel.SUBCATEGORY, viewModel.uiState.value.sheetLevel)
        assertEquals("Groceries", viewModel.uiState.value.inputName)
        assertEquals(parentGroup, viewModel.uiState.value.selectedParentGroup)

        viewModel.closeBottomSheet()
        assertFalse(viewModel.uiState.value.isBottomSheetOpen)
        assertNull(viewModel.uiState.value.editingCategoryId)
    }
}

