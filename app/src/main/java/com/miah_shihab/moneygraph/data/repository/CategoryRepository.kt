package com.miah_shihab.moneygraph.data.repository

import com.miah_shihab.moneygraph.data.db.CategoryDao
import com.miah_shihab.moneygraph.data.db.CategoryEntity
import com.miah_shihab.moneygraph.domain.model.Category
import com.miah_shihab.moneygraph.domain.model.CategoryType
import com.miah_shihab.moneygraph.util.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAll(): Flow<List<Category>> =
        categoryDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getAllByType(type: CategoryType): Flow<List<Category>> =
        categoryDao.getAllByTypeFlow(type.value).map { list -> list.map { it.toDomain() } }

    suspend fun getAllList(): List<Category> =
        categoryDao.getAll().map { it.toDomain() }

    suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    suspend fun insert(name: String, type: CategoryType, iconRes: Int): Long {
        val id = generateId()
        val entity = CategoryEntity(
            cat_id = id,
            cat_name = name,
            cat_type = type.value,
            cat_icon = iconRes
        )
        categoryDao.insert(entity)
        return id
    }

    suspend fun update(category: Category) {
        val entity = CategoryEntity(
            cat_id = category.id,
            cat_name = category.name,
            cat_type = category.type.value,
            cat_icon = category.iconRes
        )
        categoryDao.update(entity)
    }

    suspend fun delete(id: Long) {
        categoryDao.deleteById(id)
    }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = cat_id,
        name = cat_name,
        type = if (cat_type == 1) CategoryType.INCOME else CategoryType.EXPENSE,
        iconRes = cat_icon
    )
}
