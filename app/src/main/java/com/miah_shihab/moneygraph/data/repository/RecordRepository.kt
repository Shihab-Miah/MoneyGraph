package com.miah_shihab.moneygraph.data.repository

import com.miah_shihab.moneygraph.data.db.AccountDao
import com.miah_shihab.moneygraph.data.db.CategoryDao
import com.miah_shihab.moneygraph.data.db.RecordDao
import com.miah_shihab.moneygraph.data.db.RecordEntity
import com.miah_shihab.moneygraph.domain.model.Account
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
import com.miah_shihab.moneygraph.domain.model.Record
import com.miah_shihab.moneygraph.domain.model.RecordType
import com.miah_shihab.moneygraph.util.generateId
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val recordDao: RecordDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) {

    fun getAll(): Flow<List<Record>> {
        return combine(
            recordDao.getAllFlow(),
            accountDao.getAllFlow(),
            categoryDao.getAllFlow()
        ) { records, accounts, categories ->
            val accountMap = accounts.associate { it.acc_id to it.toDomain() }
            val categoryMap = categories.associate { it.cat_id to it.toDomain() }
            records.map { entity ->
                entity.toDomain(accountMap, categoryMap)
            }
        }
    }

    fun getByDateRange(start: Long, end: Long): Flow<List<Record>> {
        return combine(
            recordDao.getByDateRangeFlow(start, end),
            accountDao.getAllFlow(),
            categoryDao.getAllFlow()
        ) { records, accounts, categories ->
            val accountMap = accounts.associate { it.acc_id to it.toDomain() }
            val categoryMap = categories.associate { it.cat_id to it.toDomain() }
            records.map { entity ->
                entity.toDomain(accountMap, categoryMap)
            }
        }
    }

    fun getByAccount(accountId: Long): Flow<List<Record>> {
        return combine(
            recordDao.getByAccountFlow(accountId),
            accountDao.getAllFlow(),
            categoryDao.getAllFlow()
        ) { records, accounts, categories ->
            val accountMap = accounts.associate { it.acc_id to it.toDomain() }
            val categoryMap = categories.associate { it.cat_id to it.toDomain() }
            records.map { entity ->
                entity.toDomain(accountMap, categoryMap)
            }
        }
    }

    suspend fun getById(id: Long): Record? {
        val entity = recordDao.getById(id) ?: return null
        val account = accountDao.getById(entity.rec_acc_id)?.toDomain()
        val category = categoryDao.getById(entity.rec_cat_id)?.toDomain()
        val fromAccount = if (entity.rec_from_id > 0) accountDao.getById(entity.rec_from_id)?.toDomain() else null
        val toAccount = if (entity.rec_to_id > 0) accountDao.getById(entity.rec_to_id)?.toDomain() else null

        val type = when (entity.rec_type) {
            1 -> RecordType.INCOME
            2 -> RecordType.EXPENSE
            3 -> RecordType.TRANSFER
            else -> RecordType.EXPENSE
        }

        return Record(
            id = entity.rec_id,
            time = entity.rec_time,
            type = type,
            amount = entity.rec_amount.toBigDecimalSafe(),
            note = entity.rec_note,
            account = account,
            category = category,
            fromAccount = fromAccount,
            toAccount = toAccount,
            isRecurring = entity.rec_is_recurring
        )
    }

    suspend fun insert(
        type: RecordType,
        amount: BigDecimal,
        note: String,
        accountId: Long,
        categoryId: Long,
        fromAccountId: Long = -1,
        toAccountId: Long = -1,
        time: Long = System.currentTimeMillis()
    ): Long {
        val id = generateId()
        val entity = RecordEntity(
            rec_id = id,
            rec_time = time,
            rec_type = type.value,
            rec_amount = amount.toPlainString(),
            rec_note = note,
            rec_acc_id = accountId,
            rec_cat_id = categoryId,
            rec_from_id = fromAccountId,
            rec_to_id = toAccountId
        )
        recordDao.insert(entity)

        // Adjust account balances
        when (type) {
            RecordType.INCOME -> {
                adjustBalance(accountId, amount)
            }
            RecordType.EXPENSE -> {
                adjustBalance(accountId, amount.negate())
            }
            RecordType.TRANSFER -> {
                if (fromAccountId > 0) adjustBalance(fromAccountId, amount.negate())
                if (toAccountId > 0) adjustBalance(toAccountId, amount)
            }
        }
        return id
    }

    suspend fun update(newRecord: Record) {
        val old = recordDao.getById(newRecord.id) ?: return
        // Revert old balance adjustment
        revertBalanceChange(old)

        val entity = RecordEntity(
            rec_id = newRecord.id,
            rec_time = newRecord.time,
            rec_type = newRecord.type.value,
            rec_amount = newRecord.amount.toPlainString(),
            rec_note = newRecord.note,
            rec_acc_id = newRecord.account?.id ?: -1,
            rec_cat_id = newRecord.category?.id ?: -1,
            rec_from_id = newRecord.fromAccount?.id ?: -1,
            rec_to_id = newRecord.toAccount?.id ?: -1,
            rec_is_recurring = newRecord.isRecurring
        )
        recordDao.update(entity)

        // Apply new balance adjustment
        when (newRecord.type) {
            RecordType.INCOME -> {
                newRecord.account?.let { adjustBalance(it.id, newRecord.amount) }
            }
            RecordType.EXPENSE -> {
                newRecord.account?.let { adjustBalance(it.id, newRecord.amount.negate()) }
            }
            RecordType.TRANSFER -> {
                newRecord.fromAccount?.let { adjustBalance(it.id, newRecord.amount.negate()) }
                newRecord.toAccount?.let { adjustBalance(it.id, newRecord.amount) }
            }
        }
    }

    suspend fun delete(id: Long) {
        val old = recordDao.getById(id) ?: return
        revertBalanceChange(old)
        recordDao.deleteById(id)
    }

    private suspend fun revertBalanceChange(entity: RecordEntity) {
        val amount = entity.rec_amount.toBigDecimalSafe()
        when (entity.rec_type) {
            1 -> adjustBalance(entity.rec_acc_id, amount.negate())
            2 -> adjustBalance(entity.rec_acc_id, amount)
            3 -> {
                if (entity.rec_from_id > 0) adjustBalance(entity.rec_from_id, amount)
                if (entity.rec_to_id > 0) adjustBalance(entity.rec_to_id, amount.negate())
            }
        }
    }

    private suspend fun adjustBalance(accountId: Long, delta: BigDecimal) {
        accountDao.getById(accountId)?.let { acc ->
            val current = acc.acc_amount.toBigDecimalSafe()
            val updated = current.add(delta)
            accountDao.updateAmount(accountId, updated.toPlainString())
        }
    }

    private fun com.miah_shihab.moneygraph.data.db.AccountEntity.toDomain() = Account(
        id = acc_id,
        name = acc_name,
        amount = acc_amount.toBigDecimalSafe(),
        initialAmount = acc_initial.toBigDecimalSafe(),
        iconRes = acc_icon
    )

    private fun com.miah_shihab.moneygraph.data.db.CategoryEntity.toDomain() = Category(
        id = cat_id,
        name = cat_name,
        type = if (cat_type == 1) CategoryType.INCOME else CategoryType.EXPENSE,
        iconRes = cat_icon
    )

    private fun RecordEntity.toDomain(
        accounts: Map<Long, Account>,
        categories: Map<Long, Category>
    ): Record {
        val type = when (rec_type) {
            1 -> RecordType.INCOME
            2 -> RecordType.EXPENSE
            3 -> RecordType.TRANSFER
            else -> RecordType.EXPENSE
        }
        return Record(
            id = rec_id,
            time = rec_time,
            type = type,
            amount = rec_amount.toBigDecimalSafe(),
            note = rec_note,
            account = accounts[rec_acc_id],
            category = categories[rec_cat_id],
            fromAccount = accounts[rec_from_id],
            toAccount = accounts[rec_to_id],
            isRecurring = rec_is_recurring
        )
    }
}
