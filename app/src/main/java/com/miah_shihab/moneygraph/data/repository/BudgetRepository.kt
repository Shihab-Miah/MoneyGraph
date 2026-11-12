package com.miah_shihab.moneygraph.data.repository

import com.miah_shihab.moneygraph.data.db.BudgetDao
import com.miah_shihab.moneygraph.data.db.BudgetEntity
import com.miah_shihab.moneygraph.data.db.CategoryDao
import com.miah_shihab.moneygraph.data.db.RecordDao
import com.miah_shihab.moneygraph.domain.model.Budget
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
import com.miah_shihab.moneygraph.util.endOfMonth
import com.miah_shihab.moneygraph.util.generateId
import com.miah_shihab.moneygraph.util.startOfMonth
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val recordDao: RecordDao
) {

    fun getBudgetsForMonth(monthTimestamp: Long): Flow<List<Budget>> {
        val monthStart = startOfMonth(monthTimestamp)
        val monthEnd = endOfMonth(monthTimestamp)

        return combine(
            budgetDao.getAllFlow(),
            categoryDao.getAllFlow(),
            recordDao.getByDateRangeFlow(monthStart, monthEnd)
        ) { budgets, categories, records ->
            val catMap = categories.associate { it.cat_id to it.toDomain() }

            // Group expense records by category and sum amounts
            val spentMap = mutableMapOf<Long, BigDecimal>()
            records.filter { it.rec_type == 2 }.forEach { r ->
                val current = spentMap.getOrDefault(r.rec_cat_id, BigDecimal.ZERO)
                spentMap[r.rec_cat_id] = current.add(r.rec_amount.toBigDecimalSafe())
            }

            budgets.map { b ->
                val spent = spentMap.getOrDefault(b.bug_cat_id, BigDecimal.ZERO)
                Budget(
                    id = b.bug_id,
                    category = catMap[b.bug_cat_id],
                    limit = b.bug_limit.toBigDecimalSafe(),
                    spent = spent,
                    monthTimestamp = b.bug_time
                )
            }
        }
    }

    suspend fun insertOrUpdate(categoryId: Long, limit: BigDecimal, monthTimestamp: Long): Long {
        val monthStart = startOfMonth(monthTimestamp)
        val existing = budgetDao.getByCategoryAndMonth(categoryId, monthStart)
        return if (existing != null) {
            budgetDao.update(existing.copy(bug_limit = limit.toPlainString()))
            existing.bug_id
        } else {
            val id = generateId()
            budgetDao.insert(
                BudgetEntity(
                    bug_id = id,
                    bug_cat_id = categoryId,
                    bug_limit = limit.toPlainString(),
                    bug_time = monthStart
                )
            )
            id
        }
    }

    suspend fun delete(id: Long) {
        budgetDao.deleteById(id)
    }

    private fun com.miah_shihab.moneygraph.data.db.CategoryEntity.toDomain() = Category(
        id = cat_id,
        name = cat_name,
        type = if (cat_type == 1) CategoryType.INCOME else CategoryType.EXPENSE,
        iconRes = cat_icon
    )
}
