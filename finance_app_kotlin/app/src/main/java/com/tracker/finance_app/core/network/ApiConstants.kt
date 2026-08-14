package com.tracker.finance_app.core.network

object ApiConstants {
    // Auth
    const val LOGIN = "/auth/login"
    const val REGISTER = "/auth/register"
    const val REFRESH = "/auth/refresh"
    const val LOGOUT = "/auth/logout"
    const val USER_PROFILE = "/auth/user"
    const val PASSWORD_RESET = "/auth/password-reset"

    // Transactions
    const val TRANSACTIONS = "/api/v1/transactions"
    const val TRANSACTIONS_BATCH = "/api/v1/transactions/batch"
    const val AVG_DAILY = "/api/v1/transactions/avg-daily"
    const val EXPENSE_REPORT = "/api/v1/transactions/analysis"
    const val EXPORT_MESSAGES = "/api/v1/transactions/export-messages"
    const val DRAFTS_BATCH_DELETE = "/api/v1/transactions/drafts/batch-delete"

    // Accounts
    const val ACCOUNTS = "/api/v1/accounts"
    const val CREATE_ACCOUNT = "/api/v1/account"
    const val INITIALIZE_DEFAULTS = "/api/v1/accounts/initialize-defaults"
    const val NETWORTH_SUMMARY = "/api/v1/networth"

    // Categories
    const val CATEGORIES = "/api/v1/categories"
    const val SUBCATEGORIES = "/api/v1/categories/subcategories"

    // Sync
    const val SYNC_LATEST_TIMESTAMP = "/api/sync/latest-timestamp"
    const val SYNC_BATCH_UPLOAD = "/api/sync/batch-upload"
}
