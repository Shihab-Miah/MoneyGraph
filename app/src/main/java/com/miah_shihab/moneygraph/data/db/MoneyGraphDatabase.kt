package com.miah_shihab.moneygraph.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        RecordEntity::class,
        BudgetEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MoneyGraphDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao
    abstract fun budgetDao(): BudgetDao
    abstract fun loanDao(): LoanDao
    abstract fun loanPaymentDao(): LoanPaymentDao
    abstract fun recurringDao(): RecurringTransactionDao

    companion object {
        const val DATABASE_NAME = "moneygraph.db"

        @Volatile
        private var instance: MoneyGraphDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): MoneyGraphDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoneyGraphDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate default seed categories & accounts
            val defaultCategories = listOf(
                // Income (type = 1)
                Triple("Salary", 1, 11),
                Triple("Business", 1, 12),
                Triple("Investment", 1, 13),
                Triple("Gift", 1, 14),
                Triple("Other Income", 1, 15),
                // Expense (type = 2)
                Triple("Food & Dining", 2, 16),
                Triple("Transportation", 2, 17),
                Triple("Shopping", 2, 18),
                Triple("Bills & Utilities", 2, 19),
                Triple("Entertainment", 2, 20),
                Triple("Health & Medical", 2, 21),
                Triple("Education", 2, 22),
                Triple("Other Expense", 2, 23)
            )

            var catId = 1L
            for ((name, type, icon) in defaultCategories) {
                db.execSQL(
                    "INSERT INTO table_category (cat_id, cat_name, cat_type, cat_icon) VALUES (?, ?, ?, ?)",
                    arrayOf(catId++, name, type, icon)
                )
            }

            // Default Accounts: Cash, Bank Account
            db.execSQL(
                "INSERT INTO table_account (acc_id, acc_name, acc_amount, acc_initial, acc_icon) VALUES (?, ?, ?, ?, ?)",
                arrayOf(1L, "Cash", "0.00", "0.00", 1)
            )
            db.execSQL(
                "INSERT INTO table_account (acc_id, acc_name, acc_amount, acc_initial, acc_icon) VALUES (?, ?, ?, ?, ?)",
                arrayOf(2L, "Bank Account", "0.00", "0.00", 2)
            )
        }
    }
}
