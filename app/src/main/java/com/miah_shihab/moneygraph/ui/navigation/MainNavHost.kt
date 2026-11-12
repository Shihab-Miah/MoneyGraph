package com.miah_shihab.moneygraph.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miah_shihab.moneygraph.data.repository.AccountRepository
import com.miah_shihab.moneygraph.data.repository.BudgetRepository
import com.miah_shihab.moneygraph.data.repository.CategoryRepository
import com.miah_shihab.moneygraph.data.repository.GoogleDriveBackupService
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.ui.screens.accounts.AccountsScreen
import com.miah_shihab.moneygraph.ui.screens.analysis.AnalysisScreen
import com.miah_shihab.moneygraph.ui.screens.budgets.BudgetsScreen
import com.miah_shihab.moneygraph.ui.screens.categories.CategoriesScreen
import com.miah_shihab.moneygraph.ui.screens.home.HomeScreen
import com.miah_shihab.moneygraph.ui.screens.loans.LoanAddScreen
import com.miah_shihab.moneygraph.ui.screens.loans.LoanDetailScreen
import com.miah_shihab.moneygraph.ui.screens.loans.LoansScreen
import com.miah_shihab.moneygraph.ui.screens.records.RecordAddEditScreen
import com.miah_shihab.moneygraph.ui.screens.records.RecordsScreen
import com.miah_shihab.moneygraph.ui.screens.settings.AboutScreen
import com.miah_shihab.moneygraph.ui.screens.settings.SettingsScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Records.route, "Records", Icons.Default.ReceiptLong),
    BottomNavItem(Screen.Budgets.route, "Budgets", Icons.Default.PieChart),
    BottomNavItem(Screen.Loans.route, "Loans", Icons.Default.Handshake),
    BottomNavItem(Screen.Analysis.route, "Analysis", Icons.Default.Analytics)
)

@Composable
fun MainNavHost(
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    recordRepository: RecordRepository,
    budgetRepository: BudgetRepository,
    loanRepository: LoanRepository,
    preferencesRepository: PreferencesRepository,
    backupService: GoogleDriveBackupService
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main Bottom Tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddRecord = { navController.navigate(Screen.AddRecord.createRoute()) },
                    onNavigateToRecordDetail = { id -> navController.navigate(Screen.AddRecord.createRoute(id)) },
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToLoans = { navController.navigate(Screen.Loans.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLoanDetail = { id -> navController.navigate(Screen.LoanDetail.createRoute(id)) }
                )
            }

            composable(Screen.Records.route) {
                RecordsScreen(
                    onNavigateToAddRecord = { navController.navigate(Screen.AddRecord.createRoute()) },
                    onNavigateToRecordDetail = { id -> navController.navigate(Screen.AddRecord.createRoute(id)) }
                )
            }

            composable(Screen.Budgets.route) {
                BudgetsScreen()
            }

            composable(Screen.Loans.route) {
                LoansScreen(
                    onNavigateToAddLoan = { navController.navigate(Screen.AddLoan.route) },
                    onNavigateToLoanDetail = { id -> navController.navigate(Screen.LoanDetail.createRoute(id)) }
                )
            }

            composable(Screen.Analysis.route) {
                AnalysisScreen()
            }

            // Secondary Screens
            composable(
                route = Screen.AddRecord.route,
                arguments = listOf(navArgument("recordId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: -1L
                RecordAddEditScreen(
                    recordId = recordId,
                    recordRepository = recordRepository,
                    accountRepository = accountRepository,
                    categoryRepository = categoryRepository,
                    preferencesRepository = preferencesRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Accounts.route) {
                AccountsScreen(
                    accountRepository = accountRepository,
                    preferencesRepository = preferencesRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    categoryRepository = categoryRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddLoan.route) {
                LoanAddScreen(
                    loanRepository = loanRepository,
                    preferencesRepository = preferencesRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LoanDetail.route,
                arguments = listOf(navArgument("loanId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val loanId = backStackEntry.arguments?.getLong("loanId") ?: -1L
                LoanDetailScreen(
                    loanId = loanId,
                    loanRepository = loanRepository,
                    preferencesRepository = preferencesRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    preferencesRepository = preferencesRepository,
                    backupService = backupService,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
