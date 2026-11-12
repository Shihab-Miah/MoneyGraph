package com.miah_shihab.moneygraph.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.miah_shihab.moneygraph.data.db.AccountDao
import com.miah_shihab.moneygraph.data.db.BudgetDao
import com.miah_shihab.moneygraph.data.db.CategoryDao
import com.miah_shihab.moneygraph.data.db.LoanDao
import com.miah_shihab.moneygraph.data.db.LoanPaymentDao
import com.miah_shihab.moneygraph.data.db.MoneyGraphDatabase
import com.miah_shihab.moneygraph.data.db.RecordDao
import com.miah_shihab.moneygraph.data.db.RecurringTransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): MoneyGraphDatabase {
        return MoneyGraphDatabase.getInstance(context, scope)
    }

    @Provides
    fun provideAccountDao(db: MoneyGraphDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: MoneyGraphDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideRecordDao(db: MoneyGraphDatabase): RecordDao = db.recordDao()

    @Provides
    fun provideBudgetDao(db: MoneyGraphDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideLoanDao(db: MoneyGraphDatabase): LoanDao = db.loanDao()

    @Provides
    fun provideLoanPaymentDao(db: MoneyGraphDatabase): LoanPaymentDao = db.loanPaymentDao()

    @Provides
    fun provideRecurringDao(db: MoneyGraphDatabase): RecurringTransactionDao = db.recurringDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("moneygraph_settings") }
        )
    }
}
