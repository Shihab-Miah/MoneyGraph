package com.miah_shihab.moneygraph.ui.navigation

sealed class Screen(val route: String) {
    // 5 Main Bottom Nav Tabs
    object Home : Screen("home")
    object Records : Screen("records")
    object Budgets : Screen("budgets")
    object Loans : Screen("loans")
    object Analysis : Screen("analysis")

    // Management screens
    object Accounts : Screen("accounts")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object About : Screen("about")

    // Add / Edit screens
    object AddRecord : Screen("add_record?recordId={recordId}") {
        fun createRoute(recordId: Long = -1L) = "add_record?recordId=$recordId"
    }

    object AddAccount : Screen("add_account?accountId={accountId}") {
        fun createRoute(accountId: Long = -1L) = "add_account?accountId=$accountId"
    }

    object AddCategory : Screen("add_category?categoryId={categoryId}") {
        fun createRoute(categoryId: Long = -1L) = "add_category?categoryId=$categoryId"
    }

    object AddLoan : Screen("add_loan")

    object LoanDetail : Screen("loan_detail/{loanId}") {
        fun createRoute(loanId: Long) = "loan_detail/$loanId"
    }

    object AddBudget : Screen("add_budget")
    object PinLock : Screen("pin_lock")
}
