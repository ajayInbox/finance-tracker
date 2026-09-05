package com.tracker.finance_app

import com.tracker.finance_app.data.remote.FinanceApiService
import com.tracker.finance_app.data.repository.AccountRepositoryImpl
import com.tracker.finance_app.data.repository.CategoryRepositoryImpl
import com.tracker.finance_app.data.repository.TransactionRepositoryImpl
import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryCachingTest {

    private val testDispatcher = StandardTestDispatcher()
    private val apiService: FinanceApiService = mockk(relaxed = true)
    private val mockAccountRepository: AccountRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `TransactionRepositoryImpl caches transactions and does not hit API on second fetch`() = runTest {
        val sampleTxList = listOf(
            Transaction(
                id = "tx-1",
                amount = 250.0,
                type = TransactionType.EXPENSE,
                description = "Coffee",
                timestamp = "2026-09-05T10:00:00"
            )
        )
        coEvery { apiService.getTransactions() } returns sampleTxList

        val repo = TransactionRepositoryImpl(
            apiService = apiService,
            accountRepositoryProvider = { mockAccountRepository }
        )

        // 1. First fetch -> should call API
        val result1 = repo.fetchTransactions(forceRefresh = false)
        assertTrue(result1.isSuccess)
        assertEquals(1, result1.getOrNull()?.size)
        coVerify(exactly = 1) { apiService.getTransactions() }

        // 2. Second fetch without forceRefresh -> should use cache, 0 new API calls
        val result2 = repo.fetchTransactions(forceRefresh = false)
        assertTrue(result2.isSuccess)
        assertEquals(1, result2.getOrNull()?.size)
        coVerify(exactly = 1) { apiService.getTransactions() } // still 1!

        // 3. Third fetch WITH forceRefresh = true -> should hit API
        val result3 = repo.fetchTransactions(forceRefresh = true)
        assertTrue(result3.isSuccess)
        coVerify(exactly = 2) { apiService.getTransactions() } // increased to 2
    }

    @Test
    fun `TransactionRepositoryImpl addTransaction updates cache and invalidates account cache`() = runTest {
        val initialTx = Transaction(id = "tx-1", amount = 100.0, type = TransactionType.EXPENSE, description = "Lunch")
        coEvery { apiService.getTransactions() } returns listOf(initialTx)

        val newTx = Transaction(id = "tx-2", amount = 50.0, type = TransactionType.EXPENSE, description = "Tea")
        coEvery { apiService.createTransaction(any()) } returns newTx

        val repo = TransactionRepositoryImpl(
            apiService = apiService,
            accountRepositoryProvider = { mockAccountRepository }
        )

        repo.fetchTransactions(forceRefresh = false)
        coVerify(exactly = 1) { apiService.getTransactions() }

        // Add transaction
        val addResult = repo.addTransaction(
            accountId = "acc-1",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat-1",
            transactionName = "Tea",
            notes = "Evening tea",
            occurredAt = LocalDateTime.now()
        )
        assertTrue(addResult.isSuccess)

        // Verifying account cache was invalidated
        verify(atLeast = 1) { mockAccountRepository.invalidateCache() }

        // Verifying cache has 2 transactions now without an extra getTransactions() network call
        val currentTxList = repo.fetchTransactions(forceRefresh = false).getOrNull()
        assertEquals(2, currentTxList?.size)
        coVerify(exactly = 1) { apiService.getTransactions() } // still only 1 API call!
    }

    @Test
    fun `TransactionRepositoryImpl deleteTransaction updates cache and invalidates account cache`() = runTest {
        val tx1 = Transaction(id = "tx-1", amount = 100.0, type = TransactionType.EXPENSE, description = "Lunch")
        val tx2 = Transaction(id = "tx-2", amount = 50.0, type = TransactionType.EXPENSE, description = "Tea")
        coEvery { apiService.getTransactions() } returns listOf(tx1, tx2)
        coEvery { apiService.deleteTransaction("tx-1") } returns Unit

        val repo = TransactionRepositoryImpl(
            apiService = apiService,
            accountRepositoryProvider = { mockAccountRepository }
        )

        repo.fetchTransactions(forceRefresh = false)
        assertEquals(2, repo.fetchTransactions(forceRefresh = false).getOrNull()?.size)

        // Delete tx-1
        val deleteResult = repo.deleteTransaction("tx-1")
        assertTrue(deleteResult.isSuccess)
        verify(atLeast = 1) { mockAccountRepository.invalidateCache() }

        // Cache now only has tx-2
        val updated = repo.fetchTransactions(forceRefresh = false).getOrNull()
        assertEquals(1, updated?.size)
        assertEquals("tx-2", updated?.first()?.id)
        coVerify(exactly = 1) { apiService.getTransactions() }
    }

    @Test
    fun `AccountRepositoryImpl caches accounts and net worth summary`() = runTest {
        val sampleAccount = Account(
            id = "acc-1",
            name = "Checking Account",
            balance = 5000.0,
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET
        )
        val sampleSummary = NetWorthSummary(
            totalAssets = 5000.0,
            totalLiabilities = 0.0,
            netWorth = 5000.0,
            currency = "INR"
        )

        coEvery { apiService.getAccounts() } returns listOf(sampleAccount)
        coEvery { apiService.getNetWorthSummary() } returns sampleSummary

        val repo = AccountRepositoryImpl(apiService)

        // 1. First fetch
        val accounts1 = repo.fetchAccounts(forceRefresh = false)
        val summary1 = repo.getNetWorthSummary(forceRefresh = false)
        assertTrue(accounts1.isSuccess)
        assertTrue(summary1.isSuccess)
        coVerify(exactly = 1) { apiService.getAccounts() }
        coVerify(exactly = 1) { apiService.getNetWorthSummary() }

        // 2. Second fetch without forceRefresh -> should use cache
        val accounts2 = repo.fetchAccounts(forceRefresh = false)
        val summary2 = repo.getNetWorthSummary(forceRefresh = false)
        assertTrue(accounts2.isSuccess)
        assertTrue(summary2.isSuccess)
        coVerify(exactly = 1) { apiService.getAccounts() } // still 1
        coVerify(exactly = 1) { apiService.getNetWorthSummary() } // still 1

        // 3. Invalidate cache -> next fetch hits API
        repo.invalidateCache()
        val accounts3 = repo.fetchAccounts(forceRefresh = false)
        assertTrue(accounts3.isSuccess)
        coVerify(exactly = 2) { apiService.getAccounts() } // increased to 2
    }

    @Test
    fun `CategoryRepositoryImpl caches categories and supports forceRefresh`() = runTest {
        val sampleCategory = Category(id = "cat-1", name = "Groceries", type = TransactionType.EXPENSE)
        coEvery { apiService.getCategories() } returns listOf(sampleCategory)

        val repo = CategoryRepositoryImpl(apiService)

        // First fetch
        repo.fetchCategories(forceRefresh = false)
        coVerify(exactly = 1) { apiService.getCategories() }

        // Second fetch (cached)
        repo.fetchCategories(forceRefresh = false)
        coVerify(exactly = 1) { apiService.getCategories() }

        // Third fetch (forceRefresh)
        repo.fetchCategories(forceRefresh = true)
        coVerify(exactly = 2) { apiService.getCategories() }
    }

    @Test
    fun `Transaction mutations advance lastMutationTime and emit transactionUpdates`() = runTest {
        val initialTx = Transaction(id = "tx-1", amount = 100.0, type = TransactionType.EXPENSE, description = "Lunch")
        coEvery { apiService.createTransaction(any()) } returns initialTx
        coEvery { apiService.deleteTransaction(any()) } returns Unit

        val repo = TransactionRepositoryImpl(
            apiService = apiService,
            accountRepositoryProvider = { mockAccountRepository }
        )

        val initialTime = repo.lastMutationTime

        // 1. addTransaction advances lastMutationTime
        repo.addTransaction(
            accountId = "acc-1",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat-1",
            transactionName = "Lunch",
            notes = "Lunch",
            occurredAt = LocalDateTime.now()
        )
        val afterAddTime = repo.lastMutationTime
        assertTrue(afterAddTime >= initialTime)

        // 2. deleteTransaction advances lastMutationTime
        repo.deleteTransaction("tx-1")
        val afterDeleteTime = repo.lastMutationTime
        assertTrue(afterDeleteTime >= afterAddTime)
    }

    @Test
    fun `DashboardViewModel reloads data reactively when transactionUpdates emits`() = runTest {
        val mockAuthRepo = mockk<com.tracker.finance_app.domain.repository.AuthRepository>(relaxed = true)
        val mockTxRepo = mockk<com.tracker.finance_app.domain.repository.TransactionRepository>(relaxed = true)
        val mockAccRepo = mockk<com.tracker.finance_app.domain.repository.AccountRepository>(relaxed = true)

        val txUpdateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
        val accUpdateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
        every { mockTxRepo.transactionUpdates } returns txUpdateFlow
        every { mockAccRepo.accountUpdates } returns accUpdateFlow
        coEvery { mockAuthRepo.getUserProfile() } returns Result.success(UserProfile(id = "user-1", email = "test@example.com", name = "Test User"))
        coEvery { mockTxRepo.fetchTransactions(any()) } returns Result.success(emptyList())
        coEvery { mockTxRepo.fetchExpenseReport(any(), any(), any()) } returns Result.success(MonthlyExpenseResponse(total = 0.0))
        coEvery { mockAccRepo.fetchAccounts(any()) } returns Result.success(emptyList())
        coEvery { mockAccRepo.getNetWorthSummary(any()) } returns Result.success(NetWorthSummary(totalAssets = 0.0, totalLiabilities = 0.0, netWorth = 0.0, currency = "INR"))

        val viewModel = com.tracker.finance_app.presentation.ui.dashboard.DashboardViewModel(
            accountRepository = mockAccRepo,
            transactionRepository = mockTxRepo,
            authRepository = mockAuthRepo
        )

        // Initial load called once
        advanceUntilIdle()
        coVerify(exactly = 1) { mockTxRepo.fetchTransactions(any()) }

        // Emit transaction update
        txUpdateFlow.emit(Unit)
        advanceUntilIdle()

        // Verifying fetchTransactions was called a 2nd time because DashboardViewModel refreshed!
        coVerify(exactly = 2) { mockTxRepo.fetchTransactions(any()) }
    }
}
