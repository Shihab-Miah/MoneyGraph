package com.miah_shihab.moneygraph.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miah_shihab.moneygraph.data.repository.AccountRepository
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.domain.model.Account
import com.miah_shihab.moneygraph.domain.model.Loan
import com.miah_shihab.moneygraph.domain.model.Record
import com.miah_shihab.moneygraph.domain.model.RecordType
import com.miah_shihab.moneygraph.util.endOfMonth
import com.miah_shihab.moneygraph.util.startOfMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject

data class HomeUiState(
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currencySymbol: String = "৳",
    val recentRecords: List<Record> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val activeLoans: List<Loan> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val accountRepository: AccountRepository,
    private val loanRepository: LoanRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val currentMonthStart = startOfMonth()
    private val currentMonthEnd = endOfMonth()

    val uiState: StateFlow<HomeUiState> = combine(
        recordRepository.getByDateRange(currentMonthStart, currentMonthEnd),
        accountRepository.getAll(),
        loanRepository.getActive(),
        preferencesRepository.currencySymbolFlow
    ) { records, accounts, activeLoans, symbol ->
        var income = BigDecimal.ZERO
        var expense = BigDecimal.ZERO

        records.forEach { r ->
            when (r.type) {
                RecordType.INCOME -> income = income.add(r.amount)
                RecordType.EXPENSE -> expense = expense.add(r.amount)
                RecordType.TRANSFER -> { /* balance unchanged across accounts */ }
            }
        }

        val totalAccountBalance = accounts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.amount) }

        HomeUiState(
            totalIncome = income,
            totalExpense = expense,
            balance = totalAccountBalance,
            currencySymbol = symbol,
            recentRecords = records.take(5),
            accounts = accounts,
            activeLoans = activeLoans
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
