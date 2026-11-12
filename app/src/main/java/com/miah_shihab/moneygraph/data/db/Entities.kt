package com.miah_shihab.moneygraph.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "table_account")
data class AccountEntity(
    @PrimaryKey val acc_id: Long,
    val acc_name: String,
    val acc_amount: String,   // BigDecimal as String
    val acc_initial: String,  // BigDecimal as String
    val acc_icon: Int
)

@Entity(tableName = "table_category")
data class CategoryEntity(
    @PrimaryKey val cat_id: Long,
    val cat_name: String,
    val cat_type: Int,  // 1=Income, 2=Expense
    val cat_icon: Int
)

@Entity(
    tableName = "table_record",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["acc_id"],
            childColumns = ["rec_acc_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["cat_id"],
            childColumns = ["rec_cat_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class RecordEntity(
    @PrimaryKey val rec_id: Long,
    val rec_time: Long,          // Unix millis
    val rec_type: Int,           // 1=Income, 2=Expense, 3=Transfer
    val rec_amount: String,      // BigDecimal as String
    val rec_note: String,
    val rec_acc_id: Long,
    val rec_cat_id: Long,
    val rec_from_id: Long = -1,  // Transfer from account
    val rec_to_id: Long = -1,    // Transfer to account
    val rec_is_recurring: Boolean = false,
    val rec_recurring_id: Long = -1
)

@Entity(tableName = "table_budget")
data class BudgetEntity(
    @PrimaryKey val bug_id: Long,
    val bug_cat_id: Long,
    val bug_limit: String,  // BigDecimal as String
    val bug_time: Long      // Unix millis (month timestamp)
)

@Entity(tableName = "table_loan")
data class LoanEntity(
    @PrimaryKey val loan_id: Long,
    val loan_person_name: String,
    val loan_amount: String,      // BigDecimal as String, original amount
    val loan_paid_amount: String, // BigDecimal as String, amount paid back
    val loan_due_date: Long,      // Unix millis, 0 if no due date
    val loan_note: String,
    val loan_type: Int,           // 1=LENT (I lent money), 2=BORROWED (I borrowed)
    val loan_status: Int,         // 1=ACTIVE, 2=PARTIAL, 3=SETTLED
    val loan_created_time: Long   // Unix millis
)

@Entity(
    tableName = "table_loan_payment",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["loan_id"],
            childColumns = ["payment_loan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LoanPaymentEntity(
    @PrimaryKey val payment_id: Long,
    val payment_loan_id: Long,
    val payment_amount: String,  // BigDecimal as String
    val payment_time: Long,      // Unix millis
    val payment_note: String
)

@Entity(tableName = "table_recurring")
data class RecurringTransactionEntity(
    @PrimaryKey val rec_id: Long,
    val rec_name: String,
    val rec_amount: String,      // BigDecimal as String
    val rec_type: Int,           // 1=Income, 2=Expense
    val rec_cat_id: Long,
    val rec_acc_id: Long,
    val rec_note: String,
    val rec_frequency: Int,      // 1=Daily, 2=Weekly, 3=Monthly, 4=Yearly
    val rec_start_date: Long,    // Unix millis
    val rec_next_date: Long,     // Unix millis - when to trigger next
    val rec_is_active: Boolean,
    val rec_created_time: Long   // Unix millis
)
