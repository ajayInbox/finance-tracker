package com.tracker.finance_app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFilterTest {

    object TransactionFilter {
        private val keywords = listOf("debited", "credited", "spent", "upi")

        fun isTransaction(text: String): Boolean {
            val msg = text.lowercase()
            return keywords.any { msg.contains(it) }
        }
    }

    @Test
    fun `isTransaction returns true for valid debit SMS`() {
        val sms = "Rs 450.00 debited from A/C XX1234 on 01-Aug-26 via UPI"
        assertTrue(TransactionFilter.isTransaction(sms))
    }

    @Test
    fun `isTransaction returns true for valid credit SMS`() {
        val sms = "Your account has been credited with INR 5000.00"
        assertTrue(TransactionFilter.isTransaction(sms))
    }

    @Test
    fun `isTransaction returns false for non finance message`() {
        val sms = "Hey, what are you doing tonight? Let's catch up!"
        assertFalse(TransactionFilter.isTransaction(sms))
    }
}
