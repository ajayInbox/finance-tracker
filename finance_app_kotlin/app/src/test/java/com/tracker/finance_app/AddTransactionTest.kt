package com.tracker.finance_app

import com.tracker.finance_app.data.remote.CreateTransactionRequest
import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.repository.TransactionRepositoryImpl
import com.tracker.finance_app.domain.model.Transaction
import com.tracker.finance_app.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class AddTransactionTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun `CreateTransactionRequest serializes with null accountId`() {
        val request = CreateTransactionRequest(
            transactionName = "Dinner",
            amount = 450.0,
            type = "EXPENSE",
            categoryId = "cat-123",
            accountId = null,
            occurredAt = "2026-09-04T18:30:00",
            notes = "Dinner with friends"
        )

        val serialized = json.encodeToString(request)
        assertTrue(serialized.contains("\"accountId\":null"))
        assertTrue(serialized.contains("\"amount\":450.0"))
        assertTrue(serialized.contains("\"type\":\"EXPENSE\""))

        val deserialized = json.decodeFromString<CreateTransactionRequest>(serialized)
        assertNull(deserialized.accountId)
        assertEquals("Dinner", deserialized.transactionName)
        assertEquals(450.0, deserialized.amount, 0.001)
    }

    @Test
    fun `TransactionRepositoryImpl sends null accountId when account is not selected`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val requestSlot = slot<CreateTransactionRequest>()

        val returnedTx = Transaction(
            id = "tx-1",
            accountId = null,
            amount = 120.0,
            type = TransactionType.EXPENSE,
            description = "Coffee",
            timestamp = "2026-09-04T18:00:00"
        )

        coEvery { apiService.createTransaction(capture(requestSlot)) } returns returnedTx

        val repo = TransactionRepositoryImpl(apiService)
        val result = repo.addTransaction(
            accountId = null,
            amount = 120.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat-coffee",
            categoryName = "Coffee & Snacks",
            notes = "Evening coffee",
            occurredAt = LocalDateTime.of(2026, 9, 4, 18, 0)
        )

        assertTrue(result.isSuccess)
        val captured = requestSlot.captured
        assertNull("accountId should be null when user did not select an account", captured.accountId)
        assertEquals("Evening coffee", captured.transactionName)
        assertEquals(120.0, captured.amount, 0.001)
        assertEquals("EXPENSE", captured.type)
        assertEquals("cat-coffee", captured.categoryId)

        coVerify(exactly = 1) { apiService.createTransaction(any()) }
    }

    @Test
    fun `Transaction normalized handles transactionName and occurredAt properly`() {
        val modernTx = Transaction(
            id = "tx-2",
            accountId = null,
            amount = 500.0,
            type = TransactionType.INCOME,
            transactionName = "Freelance Project",
            occurredAt = "2026-09-04T14:00:00"
        )

        val normalized = modernTx.normalized()
        assertEquals("Freelance Project", normalized.description)
        assertEquals("2026-09-04T14:00:00", normalized.timestamp)
        assertNull(normalized.accountId)
    }

    @Test
    fun `Subcategories are extracted from category tree and inherit parent metadata`() {
        val parentGroup = com.tracker.finance_app.domain.model.Category(
            id = "parent-food",
            name = "Food & Dining",
            type = TransactionType.EXPENSE,
            iconName = "🍽️",
            colorHex = "#087B3D",
            children = listOf(
                com.tracker.finance_app.domain.model.Category(
                    id = "sub-groceries",
                    name = "Groceries",
                    type = TransactionType.EXPENSE,
                    parentId = "parent-food",
                    iconName = "🛒",
                    colorHex = "#10B981"
                ),
                com.tracker.finance_app.domain.model.Category(
                    id = "sub-restaurants",
                    name = "Restaurants",
                    type = TransactionType.EXPENSE,
                    parentId = "parent-food"
                )
            )
        )

        val categories = listOf(parentGroup)
        val extractedSubs = categories
            .filter { it.type == TransactionType.EXPENSE }
            .flatMap { parent ->
                parent.children.map { child ->
                    child.copy(
                        type = parent.type,
                        groupName = parent.name,
                        iconName = child.iconName ?: parent.iconName,
                        colorHex = child.colorHex ?: parent.colorHex
                    )
                }
            }

        assertEquals(2, extractedSubs.size)

        val groceries = extractedSubs[0]
        assertEquals("sub-groceries", groceries.id)
        assertEquals("Groceries", groceries.name)
        assertEquals("Food & Dining", groceries.groupName)
        assertEquals("🛒", groceries.iconName)
        assertEquals("#10B981", groceries.colorHex)

        val restaurants = extractedSubs[1]
        assertEquals("sub-restaurants", restaurants.id)
        assertEquals("Restaurants", restaurants.name)
        assertEquals("Food & Dining", restaurants.groupName)
        // Inherits from parent
        assertEquals("🍽️", restaurants.iconName)
        assertEquals("#087B3D", restaurants.colorHex)
    }

    @Test
    fun `TransactionRepositoryImpl sends custom transactionName when user inputs name`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val requestSlot = slot<CreateTransactionRequest>()

        val returnedTx = Transaction(
            id = "tx-1",
            accountId = null,
            amount = 300.0,
            type = TransactionType.EXPENSE,
            transactionName = "Supermarket Grocery Run"
        )
        coEvery { apiService.createTransaction(capture(requestSlot)) } returns returnedTx

        val repo = TransactionRepositoryImpl(apiService)
        val result = repo.addTransaction(
            accountId = null,
            amount = 300.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat-groceries",
            categoryName = "Groceries",
            transactionName = "Supermarket Grocery Run",
            notes = null
        )

        assertTrue(result.isSuccess)
        assertEquals("Supermarket Grocery Run", requestSlot.captured.transactionName)
    }

    @Test
    fun `TransactionRepositoryImpl defaults transactionName to New Transaction when user input is blank`() = runTest {
        val apiService = mockk<FinanceApiService>()
        val requestSlot = slot<CreateTransactionRequest>()

        val returnedTx = Transaction(
            id = "tx-2",
            accountId = null,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            transactionName = "New Transaction"
        )
        coEvery { apiService.createTransaction(capture(requestSlot)) } returns returnedTx

        val repo = TransactionRepositoryImpl(apiService)
        val result = repo.addTransaction(
            accountId = null,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat-1",
            categoryName = null,
            transactionName = "",
            notes = null
        )

        assertTrue(result.isSuccess)
        assertEquals("New Transaction", requestSlot.captured.transactionName)
    }
}
