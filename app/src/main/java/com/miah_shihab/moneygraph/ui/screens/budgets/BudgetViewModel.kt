package com.miah_shihab.moneygraph.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miah_shihab.moneygraph.data.repository.BudgetRepository
import com.miah_shihab.moneygraph.data.repository.CategoryRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.domain.model.Budget
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject

data class BudgetsUiState(
    val budgets: List<Budget> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val totalBudgetLimit: BigDecimal = BigDecimal.ZERO,
    val totalBudgetSpent: BigDecimal = BigDecimal.ZERO,
    val currencySymbol: String = "৳",
    val currentMonthTimestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<BudgetsUiState> = combine(
        _currentMonth,
        categoryRepository.getAllByType(CategoryType.EXPENSE),
        preferencesRepository.currencySymbolFlow
    ) { month, expenseCategories, symbol ->
        BudgetsUiState(
            expenseCategories = expenseCategories,
            currencySymbol = symbol,
            currentMonthTimestamp = month
        )
    }.combine(budgetRepository.getBudgetsForMonth(System.currentTimeMillis())) { state, budgets ->
        val totalLimit = budgets.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.limit) }
        val totalSpent = budgets.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.spent) }

        state.copy(
            budgets = budgets,
            totalBudgetLimit = totalLimit,
            totalBudgetSpent = totalSpent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState()
    )

    fun setBudget(categoryId: Long, limit: BigDecimal) {
        viewModelScope.launch {
            budgetRepository.insertOrUpdate(categoryId, limit, _currentMonth.value)
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.delete(budgetId)
        }
    }

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = cal.timeInMillis
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = cal.timeInMillis
    }
}
