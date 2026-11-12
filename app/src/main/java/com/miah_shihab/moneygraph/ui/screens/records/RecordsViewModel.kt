package com.miah_shihab.moneygraph.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miah_shihab.moneygraph.data.repository.AccountRepository
import com.miah_shihab.moneygraph.data.repository.CategoryRepository
import com.miah_shihab.moneygraph.data.repository.PreferencesRepository
import com.miah_shihab.moneygraph.data.repository.RecordRepository
import com.miah_shihab.moneygraph.domain.model.Account
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
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
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject

data class RecordsUiState(
    val records: List<Record> = emptyList(),
    val filteredRecords: List<Record> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currencySymbol: String = "৳",
    val selectedFilterType: RecordType? = null,
    val currentMonthTimestamp: Long = System.currentTimeMillis(),
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())
    private val _filterType = MutableStateFlow<RecordType?>(null)

    val uiState: StateFlow<RecordsUiState> = combine(
        recordRepository.getAll(),
        accountRepository.getAll(),
        categoryRepository.getAll(),
        preferencesRepository.currencySymbolFlow,
        _currentMonth,
        _filterType
    ) { records, accounts, categories, symbol, monthTimestamp, filterType ->
        val start = startOfMonth(monthTimestamp)
        val end = endOfMonth(monthTimestamp)

        val monthRecords = records.filter { it.time in start..end }
        val filtered = if (filterType != null) {
            monthRecords.filter { it.type == filterType }
        } else {
            monthRecords
        }

        var income = BigDecimal.ZERO
        var expense = BigDecimal.ZERO
        monthRecords.forEach { r ->
            when (r.type) {
                RecordType.INCOME -> income = income.add(r.amount)
                RecordType.EXPENSE -> expense = expense.add(r.amount)
                RecordType.TRANSFER -> {}
            }
        }

        RecordsUiState(
            records = monthRecords,
            filteredRecords = filtered,
            accounts = accounts,
            categories = categories,
            currencySymbol = symbol,
            selectedFilterType = filterType,
            currentMonthTimestamp = monthTimestamp,
            totalIncome = income,
            totalExpense = expense
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordsUiState()
    )

    fun setFilterType(type: RecordType?) {
        _filterType.value = type
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

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            recordRepository.delete(id)
        }
    }
}
