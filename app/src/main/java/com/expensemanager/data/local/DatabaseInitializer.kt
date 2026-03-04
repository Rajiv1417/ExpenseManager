package com.expensemanager.data.local

import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.dao.CategoryDao
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountType
import com.expensemanager.data.local.entities.CategoryEntity
import com.expensemanager.data.local.entities.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) {
    suspend fun initializeIfNeeded() {
        seedDefaultCategories()
        seedDefaultAccounts()
    }

    private suspend fun seedDefaultCategories() {
        val existing = categoryDao.getAllCategoriesOnce()
        if (existing.isNotEmpty()) return

        val defaults = listOf(
            CategoryEntity(name = "Food & Dining",    type = TransactionType.EXPENSE, color = 0xFFE53935, icon = "restaurant"),
            CategoryEntity(name = "Transport",         type = TransactionType.EXPENSE, color = 0xFF1A6EDD, icon = "directions_car"),
            CategoryEntity(name = "Shopping",          type = TransactionType.EXPENSE, color = 0xFFFF8F00, icon = "shopping_bag"),
            CategoryEntity(name = "Entertainment",     type = TransactionType.EXPENSE, color = 0xFF8E24AA, icon = "movie"),
            CategoryEntity(name = "Health",            type = TransactionType.EXPENSE, color = 0xFF43A047, icon = "favorite"),
            CategoryEntity(name = "Bills & Utilities", type = TransactionType.EXPENSE, color = 0xFF00ACC1, icon = "receipt"),
            CategoryEntity(name = "Other Expense",     type = TransactionType.EXPENSE, color = 0xFF757575, icon = "more_horiz"),
            CategoryEntity(name = "Salary",            type = TransactionType.INCOME,  color = 0xFF43A047, icon = "work"),
            CategoryEntity(name = "Freelance",         type = TransactionType.INCOME,  color = 0xFF00B67B, icon = "laptop"),
            CategoryEntity(name = "Investment Return", type = TransactionType.INCOME,  color = 0xFF1A6EDD, icon = "trending_up"),
            CategoryEntity(name = "Cashback",          type = TransactionType.INCOME,  color = 0xFF3DDAAE, icon = "savings"),
            CategoryEntity(name = "Other Income",      type = TransactionType.INCOME,  color = 0xFF757575, icon = "more_horiz")
        )
        defaults.forEach { categoryDao.insertCategory(it) }
    }

    private suspend fun seedDefaultAccounts() {
        val existing = accountDao.getAllAccountsOnce()
        if (existing.isNotEmpty()) return

        listOf(
            AccountEntity(name = "Cash",        accountNumber = null, type = AccountType.CASH,        initialValue = 0.0,  currency = "INR", color = 0xFF43A047),
            AccountEntity(name = "Bank Account",accountNumber = null, type = AccountType.BANK,        initialValue = 0.0,  currency = "INR", color = 0xFF1A6EDD),
            AccountEntity(name = "Credit Card", accountNumber = null, type = AccountType.CREDIT_CARD, initialValue = 0.0,  currency = "INR", color = 0xFFE53935),
            AccountEntity(name = "Savings",     accountNumber = null, type = AccountType.SAVINGS,     initialValue = 0.0,  currency = "INR", color = 0xFF00B67B)
        ).forEach { accountDao.insert(it) }
    }
}
