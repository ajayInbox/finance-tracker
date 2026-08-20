package com.tracker.finance_app.presentation.navigation

sealed class Screen(val route: String) {
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Dashboard : Screen("dashboard")
    data object Accounts : Screen("accounts")
    data object AddAccount : Screen("add_account")
    data object Transactions : Screen("transactions")
    data object AddTransaction : Screen("add_transaction")
    data object CategoryManagement : Screen("categories")
    data object AddCategoryGroup : Screen("add_category_group")
    data object SmsReview : Screen("sms_review")
    data object Settings : Screen("settings")
}
