package com.miah_shihab.moneygraph.domain.model

import java.math.BigDecimal

// Record types
enum class RecordType(val value: Int) { INCOME(1), EXPENSE(2), TRANSFER(3) }
enum class CategoryType(val value: Int) { INCOME(1), EXPENSE(2) }
enum class LoanType(val value: Int) { LENT(1), BORROWED(2) }
enum class LoanStatus(val value: Int) { ACTIVE(1), PARTIAL(2), SETTLED(3) }
enum class RecurringFrequency(val value: Int) { DAILY(1), WEEKLY(2), MONTHLY(3), YEARLY(4) }

data class Account(
    val id: Long,
    val name: String,
    val amount: BigDecimal,
    val initialAmount: BigDecimal,
    val iconRes: Int
)

data class Category(
    val id: Long,
    val name: String,
    val type: CategoryType,
    val iconRes: Int
)

data class Record(
    val id: Long,
    val time: Long,
    val type: RecordType,
    val amount: BigDecimal,
    val note: String,
    val account: Account?,
    val category: Category?,
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val isRecurring: Boolean = false
)

data class Budget(
    val id: Long,
    val category: Category?,
    val limit: BigDecimal,
    val spent: BigDecimal,
    val monthTimestamp: Long
)

data class Loan(
    val id: Long,
    val personName: String,
    val amount: BigDecimal,
    val paidAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val dueDate: Long,
    val note: String,
    val type: LoanType,
    val status: LoanStatus,
    val createdTime: Long,
    val payments: List<LoanPayment> = emptyList()
)

data class LoanPayment(
    val id: Long,
    val loanId: Long,
    val amount: BigDecimal,
    val time: Long,
    val note: String
)

data class RecurringTransaction(
    val id: Long,
    val name: String,
    val amount: BigDecimal,
    val type: RecordType,
    val category: Category?,
    val account: Account?,
    val note: String,
    val frequency: RecurringFrequency,
    val startDate: Long,
    val nextDate: Long,
    val isActive: Boolean,
    val createdTime: Long
)

data class DashboardSummary(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val balance: BigDecimal,
    val totalAssets: BigDecimal,
    val activeLoanCount: Int
)
