package com.miah_shihab.moneygraph.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.domain.model.Category
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
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject

data class CategoryBreakdown(
    val categoryName: String,
    val amount: BigDecimal,
    val percentage: Float
)

data class AnalysisUiState(
    val currentMonthTimestamp: Long = System.currentTimeMillis(),
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val netSavings: BigDecimal = BigDecimal.ZERO,
    val savingsRate: Float = 0f,
    val expenseBreakdown: List<CategoryBreakdown> = emptyList(),
    val incomeBreakdown: List<CategoryBreakdown> = emptyList(),
    val currencySymbol: String = "৳"
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<AnalysisUiState> = combine(
        recordRepository.getAll(),
        preferencesRepository.currencySymbolFlow,
        _currentMonth
    ) { records, symbol, monthTimestamp ->
        val start = startOfMonth(monthTimestamp)
        val end = endOfMonth(monthTimestamp)
        val monthRecords = records.filter { it.time in start..end }

        var income = BigDecimal.ZERO
        var expense = BigDecimal.ZERO

        val expenseMap = mutableMapOf<String, BigDecimal>()
        val incomeMap = mutableMapOf<String, BigDecimal>()

        monthRecords.forEach { r ->
            when (r.type) {
                RecordType.INCOME -> {
                    income = income.add(r.amount)
                    val catName = r.category?.name ?: "Other Income"
                    incomeMap[catName] = incomeMap.getOrDefault(catName, BigDecimal.ZERO).add(r.amount)
                }
                RecordType.EXPENSE -> {
                    expense = expense.add(r.amount)
                    val catName = r.category?.name ?: "Other Expense"
                    expenseMap[catName] = expenseMap.getOrDefault(catName, BigDecimal.ZERO).add(r.amount)
                }
                RecordType.TRANSFER -> {}
            }
        }

        val netSavings = income.subtract(expense)
        val savingsRate = if (income > BigDecimal.ZERO) {
            netSavings.divide(income, 4, RoundingMode.HALF_UP).toFloat() * 100f
        } else 0f

        val expenseBreakdowns = expenseMap.map { (cat, amt) ->
            val pct = if (expense > BigDecimal.ZERO) {
                amt.divide(expense, 4, RoundingMode.HALF_UP).toFloat() * 100f
            } else 0f
            CategoryBreakdown(cat, amt, pct)
        }.sortedByDescending { it.amount }

        val incomeBreakdowns = incomeMap.map { (cat, amt) ->
            val pct = if (income > BigDecimal.ZERO) {
                amt.divide(income, 4, RoundingMode.HALF_UP).toFloat() * 100f
            } else 0f
            CategoryBreakdown(cat, amt, pct)
        }.sortedByDescending { it.amount }

        AnalysisUiState(
            currentMonthTimestamp = monthTimestamp,
            totalIncome = income,
            totalExpense = expense,
            netSavings = netSavings,
            savingsRate = savingsRate,
            expenseBreakdown = expenseBreakdowns,
            incomeBreakdown = incomeBreakdowns,
            currencySymbol = symbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalysisUiState()
    )

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
