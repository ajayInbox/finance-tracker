package com.tracker.finance_app

import com.tracker.finance_app.domain.model.*
import com.tracker.finance_app.domain.repository.AccountRepository
import com.tracker.finance_app.presentation.ui.account.AccountsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsScreenTest {

    private val testDispatcher = StandardTestDispatcher()
    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private lateinit var viewModel: AccountsViewModel

    private val sampleAccounts = listOf(
        Account(
            id = "acc-1",
            name = "HDFC Salary",
            institution = "HDFC Bank",
            accountNumber = "8492",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            balance = 50000.0,
            currency = "INR"
        ),
        Account(
            id = "acc-2",
            name = "HDFC Regalia",
            institution = "HDFC Bank",
            accountNumber = "6019",
            type = AccountType.CREDIT_CARD,
            category = AccountCategory.LIABILITY,
            balance = 12000.0,
            currency = "INR",
            creditLimit = 200000.0
        )
    )

    private val sampleNetWorth = NetWorthSummary(
        totalAssets = 50000.0,
        totalLiabilities = 12000.0,
        netWorth = 38000.0,
        currency = "INR"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { accountRepository.fetchAccounts() } returns Result.success(sampleAccounts)
        coEvery { accountRepository.getNetWorthSummary() } returns Result.success(sampleNetWorth)
        coEvery { accountRepository.getAccountsFlow() } returns flowOf(sampleAccounts)
        viewModel = AccountsViewModel(accountRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData populates accounts list and net worth summary`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.accounts.size)
        assertNotNull(state.netWorthSummary)
        assertEquals(38000.0, state.netWorthSummary?.netWorth ?: 0.0, 0.01)
    }

    @Test
    fun `openAddAccountSheet initializes empty form state`() = runTest {
        advanceUntilIdle()

        viewModel.openAddAccountSheet()
        val state = viewModel.uiState.value

        assertTrue(state.isBottomSheetOpen)
        assertNull(state.editingAccountId)
        assertEquals("", state.newAccountName)
        assertEquals(AccountCategory.ASSET, state.newAccountCategory)
        assertEquals(AccountType.CHECKING, state.newAccountType)
    }

    @Test
    fun `openEditAccountSheet pre-fills existing account fields`() = runTest {
        advanceUntilIdle()

        val target = sampleAccounts[1] // Credit card
        viewModel.openEditAccountSheet(target)
        val state = viewModel.uiState.value

        assertTrue(state.isBottomSheetOpen)
        assertEquals("acc-2", state.editingAccountId)
        assertEquals("HDFC Regalia", state.newAccountName)
        assertEquals("HDFC Bank", state.newAccountInstitution)
        assertEquals("6019", state.newAccountNumber)
        assertEquals("12000.0", state.newAccountBalance)
        assertEquals("200000.0", state.newAccountCreditLimit)
        assertEquals(AccountCategory.LIABILITY, state.newAccountCategory)
        assertEquals(AccountType.CREDIT_CARD, state.newAccountType)
    }

    @Test
    fun `onCategoryChanged switches account type to appropriate category default`() = runTest {
        advanceUntilIdle()

        viewModel.openAddAccountSheet()
        // Default is ASSET, CHECKING
        assertEquals(AccountCategory.ASSET, viewModel.uiState.value.newAccountCategory)
        assertEquals(AccountType.CHECKING, viewModel.uiState.value.newAccountType)

        // Switch to LIABILITY -> should auto-switch to CREDIT_CARD
        viewModel.onCategoryChanged(AccountCategory.LIABILITY)
        assertEquals(AccountCategory.LIABILITY, viewModel.uiState.value.newAccountCategory)
        assertEquals(AccountType.CREDIT_CARD, viewModel.uiState.value.newAccountType)

        // Switch back to ASSET -> should auto-switch to CHECKING
        viewModel.onCategoryChanged(AccountCategory.ASSET)
        assertEquals(AccountCategory.ASSET, viewModel.uiState.value.newAccountCategory)
        assertEquals(AccountType.CHECKING, viewModel.uiState.value.newAccountType)
    }

    @Test
    fun `saveAccount in create mode calls createAccount on repository`() = runTest {
        advanceUntilIdle()

        viewModel.openAddAccountSheet()
        viewModel.onNameChanged("ICICI Savings")
        viewModel.onInstitutionChanged("ICICI Bank")
        viewModel.onAccountNumberChanged("1122")
        viewModel.onBalanceChanged("25000")
        viewModel.onTypeChanged(AccountType.SAVINGS)

        val createdAccount = Account(
            id = "acc-new",
            name = "ICICI Savings",
            institution = "ICICI Bank",
            accountNumber = "1122",
            type = AccountType.SAVINGS,
            category = AccountCategory.ASSET,
            balance = 25000.0
        )
        coEvery { accountRepository.createAccount(any()) } returns Result.success(createdAccount)

        val slot = slot<AccountCreateUpdateRequest>()
        coEvery { accountRepository.createAccount(capture(slot)) } returns Result.success(createdAccount)

        viewModel.saveAccount()
        advanceUntilIdle()

        coVerify { accountRepository.createAccount(any()) }
        assertEquals("ICICI Savings", slot.captured.name)
        assertEquals("ICICI Bank", slot.captured.institution)
        assertEquals("1122", slot.captured.lastFour)
        assertEquals(25000.0, slot.captured.balance, 0.01)
        assertEquals(AccountType.SAVINGS, slot.captured.type)
        assertEquals(AccountCategory.ASSET, slot.captured.category)

        assertFalse(viewModel.uiState.value.isBottomSheetOpen)
        assertEquals("Account created successfully", viewModel.uiState.value.toastMessage)
    }

    @Test
    fun `saveAccount in edit mode calls updateAccount on repository`() = runTest {
        advanceUntilIdle()

        val existing = sampleAccounts[0]
        viewModel.openEditAccountSheet(existing)
        viewModel.onNameChanged("HDFC Premier Salary")
        viewModel.onBalanceChanged("75000")

        val updatedAccount = existing.copy(name = "HDFC Premier Salary", balance = 75000.0)
        val slot = slot<AccountCreateUpdateRequest>()
        coEvery { accountRepository.updateAccount(eq("acc-1"), capture(slot)) } returns Result.success(updatedAccount)

        viewModel.saveAccount()
        advanceUntilIdle()

        coVerify { accountRepository.updateAccount("acc-1", any()) }
        assertEquals("HDFC Premier Salary", slot.captured.name)
        assertEquals(75000.0, slot.captured.balance, 0.01)

        assertFalse(viewModel.uiState.value.isBottomSheetOpen)
        assertEquals("Account updated successfully", viewModel.uiState.value.toastMessage)
    }

    @Test
    fun `saveAccount with blank name produces validation error without calling repository`() = runTest {
        advanceUntilIdle()

        viewModel.openAddAccountSheet()
        viewModel.onNameChanged("   ")
        viewModel.onBalanceChanged("1000")

        viewModel.saveAccount()

        coVerify(exactly = 0) { accountRepository.createAccount(any()) }
        coVerify(exactly = 0) { accountRepository.updateAccount(any(), any()) }
        assertEquals("Account name is required", viewModel.uiState.value.error)
    }

    @Test
    fun `delete confirmation flow prompts dialog and deletes on confirmation`() = runTest {
        advanceUntilIdle()

        val target = sampleAccounts[0]
        coEvery { accountRepository.deleteAccount("acc-1") } returns Result.success(Unit)

        viewModel.requestDeleteAccount(target)
        assertEquals(target, viewModel.uiState.value.accountToDelete)

        viewModel.cancelDeleteAccount()
        assertNull(viewModel.uiState.value.accountToDelete)

        viewModel.requestDeleteAccount(target)
        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.accountToDelete)
        coVerify { accountRepository.deleteAccount("acc-1") }
    }
}
