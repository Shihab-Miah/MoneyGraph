package com.miah_shihab.moneygraph.ui.screens.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miah_shihab.moneygraph.data.repository.LoanRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.domain.model.Loan
import com.miah_shihab.moneygraph.domain.model.LoanStatus
import com.miah_shihab.moneygraph.domain.model.LoanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class LoansUiState(
    val loans: List<Loan> = emptyList(),
    val filteredLoans: List<Loan> = emptyList(),
    val totalLent: BigDecimal = BigDecimal.ZERO,
    val totalLentRemaining: BigDecimal = BigDecimal.ZERO,
    val totalBorrowed: BigDecimal = BigDecimal.ZERO,
    val totalBorrowedRemaining: BigDecimal = BigDecimal.ZERO,
    val currencySymbol: String = "৳",
    val selectedFilter: LoanFilter = LoanFilter.ALL
)

enum class LoanFilter { ALL, LENT, BORROWED, SETTLED }

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(LoanFilter.ALL)

    val uiState: StateFlow<LoansUiState> = combine(
        loanRepository.getAll(),
        preferencesRepository.currencySymbolFlow,
        _filter
    ) { loans, symbol, filter ->
        var lentTotal = BigDecimal.ZERO
        var lentRem = BigDecimal.ZERO
        var borrowedTotal = BigDecimal.ZERO
        var borrowedRem = BigDecimal.ZERO

        loans.forEach { loan ->
            if (loan.type == LoanType.LENT) {
                lentTotal = lentTotal.add(loan.amount)
                lentRem = lentRem.add(loan.remainingAmount)
            } else {
                borrowedTotal = borrowedTotal.add(loan.amount)
                borrowedRem = borrowedRem.add(loan.remainingAmount)
            }
        }

        val filtered = when (filter) {
            LoanFilter.ALL -> loans.filter { it.status != LoanStatus.SETTLED }
            LoanFilter.LENT -> loans.filter { it.type == LoanType.LENT && it.status != LoanStatus.SETTLED }
            LoanFilter.BORROWED -> loans.filter { it.type == LoanType.BORROWED && it.status != LoanStatus.SETTLED }
            LoanFilter.SETTLED -> loans.filter { it.status == LoanStatus.SETTLED }
        }

        LoansUiState(
            loans = loans,
            filteredLoans = filtered,
            totalLent = lentTotal,
            totalLentRemaining = lentRem,
            totalBorrowed = borrowedTotal,
            totalBorrowedRemaining = borrowedRem,
            currencySymbol = symbol,
            selectedFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoansUiState()
    )

    fun setFilter(filter: LoanFilter) {
        _filter.value = filter
    }

    fun addPayment(loanId: Long, amount: BigDecimal, note: String) {
        viewModelScope.launch {
            loanRepository.addPayment(loanId, amount, note)
        }
    }

    fun deleteLoan(loanId: Long) {
        viewModelScope.launch {
            loanRepository.delete(loanId)
        }
    }
}
