package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.CategoryDao
import com.expensemanager.data.local.entities.CategoryEntity
import com.expensemanager.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    suspend fun getOrCreateCategory(name: String, type: TransactionType): Long {
        val existing = categoryDao.getCategoryByNameAndType(name, type)
        return existing?.id ?: categoryDao.insertCategory(
            CategoryEntity(name = name, type = type)
        )
    }
}
