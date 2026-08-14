package com.tracker.finance_app.core.util

data class ParsedTransaction(
    val amount: Double?,
    val merchant: String?,
    val categoryHint: String?,
    val transactionType: String = "Expense"
) {
    val isValid: Boolean get() = amount != null && amount > 0
}

object MessageParser {
    private val amountRegex = Regex("""(?:₹|Rs\.?|INR)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    private val debitRegex = Regex("""(?:debited|paid|charged|transferred)""", RegexOption.IGNORE_CASE)
    private val creditRegex = Regex("""(?:credited|received|refunded)""", RegexOption.IGNORE_CASE)
    private val merchantRegex = Regex("""(?:at|to|from)\s+([A-Za-z\s]+?)(?:via|on|through|by|\.|$)""", RegexOption.IGNORE_CASE)

    private val categoryMappings = mapOf(
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "swiggy" to "Food",
        "zomato" to "Food",
        "uber" to "Transport",
        "ola" to "Transport",
        "irctc" to "Travel",
        "makemytrip" to "Travel",
        "netflix" to "Entertainment",
        "hotstar" to "Entertainment",
        "electricity" to "Bills",
        "water" to "Bills",
        "gas" to "Bills",
        "phone" to "Bills",
        "mobile" to "Bills",
        "airtel" to "Bills",
        "vodafone" to "Bills",
        "jio" to "Bills"
    )

    fun parse(message: String): ParsedTransaction {
        val lowerMessage = message.lowercase()
        val amount = extractAmount(lowerMessage)
        val merchant = extractMerchant(lowerMessage)
        val categoryHint = merchant?.let { guessCategory(it) }
        val type = when {
            debitRegex.containsMatchIn(lowerMessage) -> "Expense"
            creditRegex.containsMatchIn(lowerMessage) -> "Income"
            else -> "Expense"
        }
        return ParsedTransaction(amount, merchant, if (type == "Expense") categoryHint else null, type)
    }

    private fun extractAmount(message: String): Double? {
        return amountRegex.find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractMerchant(message: String): String? {
        val match = merchantRegex.find(message) ?: return null
        val merchant = match.groupValues[1].trim()
            .replace(Regex("""^(the|a|an)\s+""", RegexOption.IGNORE_CASE), "")
        return merchant.ifBlank { null }
    }

    private fun guessCategory(merchant: String): String? {
        val clean = merchant.lowercase()
        return categoryMappings.entries.firstOrNull { clean.contains(it.key) }?.value
    }
}
