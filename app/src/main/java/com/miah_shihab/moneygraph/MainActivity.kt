package com.miah_shihab.moneygraph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.miah_shihab.moneygraph.data.repository.AccountRepository
import com.miah_shihab.moneygraph.data.repository.BudgetRepository
import com.miah_shihab.moneygraph.data.repository.CategoryRepository
import com.miah_shihab.moneygraph.data.repository.GoogleDriveBackupService
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.ui.navigation.MainNavHost
import com.miah_shihab.moneygraph.ui.theme.MoneyGraphTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var categoryRepository: CategoryRepository
    @Inject lateinit var recordRepository: RecordRepository
    @Inject lateinit var budgetRepository: BudgetRepository
    @Inject lateinit var loanRepository: LoanRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var backupService: GoogleDriveBackupService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModePreference by preferencesRepository.darkModeFlow.collectAsState(initial = 0)
            val themeColor by preferencesRepository.themeColorFlow.collectAsState(initial = 0xFF2563EBL)

            val isDark = when (darkModePreference) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            MoneyGraphTheme(
                darkTheme = isDark,
                seedColor = themeColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavHost(
                        accountRepository = accountRepository,
                        categoryRepository = categoryRepository,
                        recordRepository = recordRepository,
                        budgetRepository = budgetRepository,
                        loanRepository = loanRepository,
                        preferencesRepository = preferencesRepository,
                        backupService = backupService
                    )
                }
            }
        }
    }
}
