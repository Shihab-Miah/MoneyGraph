package com.miah_shihab.moneygraph.data.repository

import com.miah_shihab.moneygraph.data.db.AccountDao
import com.miah_shihab.moneygraph.data.db.AccountEntity
import com.miah_shihab.moneygraph.domain.model.Account
import com.miah_shihab.moneygraph.util.generateId
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    fun getAll(): Flow<List<Account>> =
        accountDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getAllList(): List<Account> =
        accountDao.getAll().map { it.toDomain() }

    suspend fun getById(id: Long): Account? =
        accountDao.getById(id)?.toDomain()

    fun getByIdFlow(id: Long): Flow<Account?> =
        accountDao.getByIdFlow(id).map { it?.toDomain() }

    suspend fun insert(name: String, initialAmount: BigDecimal, iconRes: Int): Long {
        val id = generateId()
        val entity = AccountEntity(
            acc_id = id,
            acc_name = name,
            acc_amount = initialAmount.toPlainString(),
            acc_initial = initialAmount.toPlainString(),
            acc_icon = iconRes
        )
        accountDao.insert(entity)
        return id
    }

    suspend fun update(account: Account) {
        val entity = AccountEntity(
            acc_id = account.id,
            acc_name = account.name,
            acc_amount = account.amount.toPlainString(),
            acc_initial = account.initialAmount.toPlainString(),
            acc_icon = account.iconRes
        )
        accountDao.update(entity)
    }

    suspend fun updateBalance(id: Long, newBalance: BigDecimal) {
        accountDao.updateAmount(id, newBalance.toPlainString())
    }

    suspend fun delete(id: Long) {
        accountDao.deleteById(id)
    }

    private fun AccountEntity.toDomain(): Account = Account(
        id = acc_id,
        name = acc_name,
        amount = acc_amount.toBigDecimalSafe(),
        initialAmount = acc_initial.toBigDecimalSafe(),
        iconRes = acc_icon
    )
}
