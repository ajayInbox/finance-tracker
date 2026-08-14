package com.tracker.finance_app

import com.tracker.finance_app.domain.model.Account
import com.tracker.finance_app.domain.model.AccountCategory
import com.tracker.finance_app.domain.model.AccountType
import com.tracker.finance_app.domain.model.NetWorthSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDomainTest {

    @Test
    fun `calculate net worth summary correctly`() {
        val accounts = listOf(
            Account(
                id = "1",
                name = "Savings",
                type = AccountType.SAVINGS,
                category = AccountCategory.ASSET,
                balance = 10000.00
            ),
            Account(
                id = "2",
                name = "Credit Card",
                type = AccountType.CREDIT_CARD,
                category = AccountCategory.LIABILITY,
                balance = 2500.00
            )
        )

        val assets = accounts.filter { it.category == AccountCategory.ASSET }.sumOf { it.balance }
        val liabilities = accounts.filter { it.category == AccountCategory.LIABILITY }.sumOf { it.balance }
        val summary = NetWorthSummary(totalAssets = assets, totalLiabilities = liabilities, netWorth = assets - liabilities)

        assertEquals(10000.00, summary.totalAssets, 0.01)
        assertEquals(2500.00, summary.totalLiabilities, 0.01)
        assertEquals(7500.00, summary.netWorth, 0.01)
    }

    @Test
    fun `account default active status is true`() {
        val account = Account(
            id = "3",
            name = "Checking",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            balance = 1500.0
        )
        assertTrue(account.isActive)
    }
}
