package com.miah_shihab.moneygraph.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// AccountDao
// ─────────────────────────────────────────────
@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM table_account WHERE acc_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_account ORDER BY acc_name ASC")
    fun getAllFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM table_account ORDER BY acc_name ASC")
    suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM table_account WHERE acc_id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM table_account WHERE acc_id = :id")
    fun getByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("UPDATE table_account SET acc_amount = :amount WHERE acc_id = :id")
    suspend fun updateAmount(id: Long, amount: String)
}

// ─────────────────────────────────────────────
// CategoryDao
// ─────────────────────────────────────────────
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM table_category WHERE cat_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_category ORDER BY cat_name ASC")
    fun getAllFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM table_category ORDER BY cat_name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM table_category WHERE cat_type = :type ORDER BY cat_name ASC")
    fun getAllByTypeFlow(type: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM table_category WHERE cat_type = :type ORDER BY cat_name ASC")
    suspend fun getAllByType(type: Int): List<CategoryEntity>

    @Query("SELECT * FROM table_category WHERE cat_id = :id")
    suspend fun getById(id: Long): CategoryEntity?
}

// ─────────────────────────────────────────────
// RecordDao
// ─────────────────────────────────────────────
@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)

    @Query("DELETE FROM table_record WHERE rec_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_record ORDER BY rec_time DESC")
    fun getAllFlow(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM table_record ORDER BY rec_time DESC")
    suspend fun getAll(): List<RecordEntity>

    @Query("SELECT * FROM table_record WHERE rec_id = :id")
    suspend fun getById(id: Long): RecordEntity?

    @Query("""
        SELECT * FROM table_record
        WHERE rec_time >= :startMs AND rec_time <= :endMs
        ORDER BY rec_time DESC
    """)
    fun getAllByDateRangeFlow(startMs: Long, endMs: Long): Flow<List<RecordEntity>>

    @Query("""
        SELECT * FROM table_record
        WHERE rec_time >= :startMs AND rec_time <= :endMs
        ORDER BY rec_time DESC
    """)
    suspend fun getAllByDateRange(startMs: Long, endMs: Long): List<RecordEntity>

    @Query("""
        SELECT SUM(CAST(rec_amount AS REAL)) FROM table_record
        WHERE rec_type = :type AND rec_time >= :startMs AND rec_time <= :endMs
    """)
    suspend fun getTotalByTypeAndDateRange(type: Int, startMs: Long, endMs: Long): Double?

    @Query("""
        SELECT * FROM table_record
        WHERE rec_acc_id = :accountId OR rec_from_id = :accountId OR rec_to_id = :accountId
        ORDER BY rec_time DESC
    """)
    fun getByAccountFlow(accountId: Long): Flow<List<RecordEntity>>

    @Query("""
        SELECT * FROM table_record
        WHERE rec_acc_id = :accountId OR rec_from_id = :accountId OR rec_to_id = :accountId
        ORDER BY rec_time DESC
    """)
    suspend fun getByAccount(accountId: Long): List<RecordEntity>

    @Query("""
        SELECT * FROM table_record
        WHERE rec_cat_id = :categoryId
        ORDER BY rec_time DESC
    """)
    fun getByCategoryFlow(categoryId: Long): Flow<List<RecordEntity>>

    @Query("""
        SELECT * FROM table_record
        WHERE rec_cat_id = :categoryId AND rec_time >= :startMs AND rec_time <= :endMs
        ORDER BY rec_time DESC
    """)
    suspend fun getByCategoryAndDateRange(
        categoryId: Long,
        startMs: Long,
        endMs: Long
    ): List<RecordEntity>

    @Query("""
        SELECT SUM(CAST(rec_amount AS REAL)) FROM table_record
        WHERE rec_cat_id = :categoryId AND rec_type = :type
          AND rec_time >= :startMs AND rec_time <= :endMs
    """)
    suspend fun getTotalByCategoryTypeAndDateRange(
        categoryId: Long,
        type: Int,
        startMs: Long,
        endMs: Long
    ): Double?

    @Query("SELECT * FROM table_record WHERE rec_recurring_id = :recurringId ORDER BY rec_time DESC")
    suspend fun getByRecurringId(recurringId: Long): List<RecordEntity>
}

// ─────────────────────────────────────────────
// BudgetDao
// ─────────────────────────────────────────────
@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM table_budget WHERE bug_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_budget ORDER BY bug_time DESC")
    fun getAllFlow(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM table_budget ORDER BY bug_time DESC")
    suspend fun getAll(): List<BudgetEntity>

    @Query("SELECT * FROM table_budget WHERE bug_id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT * FROM table_budget WHERE bug_cat_id = :categoryId ORDER BY bug_time DESC")
    fun getByCategoryFlow(categoryId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM table_budget WHERE bug_cat_id = :categoryId ORDER BY bug_time DESC")
    suspend fun getByCategory(categoryId: Long): List<BudgetEntity>

    @Query("""
        SELECT * FROM table_budget
        WHERE bug_time >= :monthStart AND bug_time <= :monthEnd
        ORDER BY bug_cat_id ASC
    """)
    fun getByMonthFlow(monthStart: Long, monthEnd: Long): Flow<List<BudgetEntity>>

    @Query("""
        SELECT * FROM table_budget
        WHERE bug_time >= :monthStart AND bug_time <= :monthEnd
        ORDER BY bug_cat_id ASC
    """)
    suspend fun getByMonth(monthStart: Long, monthEnd: Long): List<BudgetEntity>

    @Query("""
        SELECT * FROM table_budget
        WHERE bug_cat_id = :categoryId AND bug_time >= :monthStart AND bug_time <= :monthEnd
        LIMIT 1
    """)
    suspend fun getByCategoryAndMonth(
        categoryId: Long,
        monthStart: Long,
        monthEnd: Long
    ): BudgetEntity?
}

// ─────────────────────────────────────────────
// LoanDao
// ─────────────────────────────────────────────
@Dao
interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Delete
    suspend fun delete(loan: LoanEntity)

    @Query("DELETE FROM table_loan WHERE loan_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_loan ORDER BY loan_created_time DESC")
    fun getAllFlow(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM table_loan ORDER BY loan_created_time DESC")
    suspend fun getAll(): List<LoanEntity>

    @Query("SELECT * FROM table_loan WHERE loan_id = :id")
    suspend fun getById(id: Long): LoanEntity?

    @Query("SELECT * FROM table_loan WHERE loan_status = :status ORDER BY loan_created_time DESC")
    fun getByStatusFlow(status: Int): Flow<List<LoanEntity>>

    @Query("SELECT * FROM table_loan WHERE loan_status = :status ORDER BY loan_created_time DESC")
    suspend fun getByStatus(status: Int): List<LoanEntity>

    // Active = status 1 (ACTIVE) or 2 (PARTIAL)
    @Query("""
        SELECT * FROM table_loan
        WHERE loan_status IN (1, 2)
        ORDER BY loan_due_date ASC, loan_created_time DESC
    """)
    fun getAllActiveFlow(): Flow<List<LoanEntity>>

    @Query("""
        SELECT * FROM table_loan
        WHERE loan_status IN (1, 2)
        ORDER BY loan_due_date ASC, loan_created_time DESC
    """)
    suspend fun getAllActive(): List<LoanEntity>

    @Query("""
        SELECT * FROM table_loan
        WHERE loan_type = :type
        ORDER BY loan_created_time DESC
    """)
    fun getByTypeFlow(type: Int): Flow<List<LoanEntity>>

    @Query("""
        SELECT * FROM table_loan
        WHERE loan_due_date > 0 AND loan_due_date <= :beforeMs AND loan_status IN (1, 2)
        ORDER BY loan_due_date ASC
    """)
    suspend fun getOverdue(beforeMs: Long): List<LoanEntity>

    @Query("UPDATE table_loan SET loan_paid_amount = :paidAmount, loan_status = :status WHERE loan_id = :id")
    suspend fun updatePaymentStatus(id: Long, paidAmount: String, status: Int)
}

// ─────────────────────────────────────────────
// LoanPaymentDao
// ─────────────────────────────────────────────
@Dao
interface LoanPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: LoanPaymentEntity): Long

    @Update
    suspend fun update(payment: LoanPaymentEntity)

    @Delete
    suspend fun delete(payment: LoanPaymentEntity)

    @Query("DELETE FROM table_loan_payment WHERE payment_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_loan_payment WHERE payment_loan_id = :loanId ORDER BY payment_time DESC")
    fun getByLoanIdFlow(loanId: Long): Flow<List<LoanPaymentEntity>>

    @Query("SELECT * FROM table_loan_payment WHERE payment_loan_id = :loanId ORDER BY payment_time DESC")
    suspend fun getByLoanId(loanId: Long): List<LoanPaymentEntity>

    @Query("SELECT * FROM table_loan_payment WHERE payment_id = :id")
    suspend fun getById(id: Long): LoanPaymentEntity?

    @Query("SELECT SUM(CAST(payment_amount AS REAL)) FROM table_loan_payment WHERE payment_loan_id = :loanId")
    suspend fun getTotalPaidForLoan(loanId: Long): Double?
}

// ─────────────────────────────────────────────
// RecurringTransactionDao
// ─────────────────────────────────────────────
@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)

    @Query("DELETE FROM table_recurring WHERE rec_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM table_recurring ORDER BY rec_next_date ASC")
    fun getAllFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM table_recurring ORDER BY rec_next_date ASC")
    suspend fun getAll(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM table_recurring WHERE rec_id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query("SELECT * FROM table_recurring WHERE rec_is_active = 1 ORDER BY rec_next_date ASC")
    fun getAllActiveFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM table_recurring WHERE rec_is_active = 1 ORDER BY rec_next_date ASC")
    suspend fun getAllActive(): List<RecurringTransactionEntity>

    @Query("""
        SELECT * FROM table_recurring
        WHERE rec_is_active = 1 AND rec_next_date <= :beforeMs
        ORDER BY rec_next_date ASC
    """)
    suspend fun getByNextDateBefore(beforeMs: Long): List<RecurringTransactionEntity>

    @Query("UPDATE table_recurring SET rec_next_date = :nextDate WHERE rec_id = :id")
    suspend fun updateNextDate(id: Long, nextDate: Long)

    @Query("UPDATE table_recurring SET rec_is_active = :isActive WHERE rec_id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)
}
