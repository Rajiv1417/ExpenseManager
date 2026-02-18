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
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    suspend fun initializeIfNeeded() {
        if (categoryDao.getCategoryCount() == 0) {
            insertDefaultCategories()
            insertDefaultAccounts()
        }
    }

    private suspend fun insertDefaultCategories() {
        val expenseCategories = listOf(
            CategoryEntity(name = "Food & Dining", type = TransactionType.EXPENSE, icon = "restaurant", color = 0xFFE53935, isDefault = true),
            CategoryEntity(name = "Transportation", type = TransactionType.EXPENSE, icon = "directions_car", color = 0xFF1E88E5, isDefault = true),
            CategoryEntity(name = "Shopping", type = TransactionType.EXPENSE, icon = "shopping_bag", color = 0xFF8E24AA, isDefault = true),
            CategoryEntity(name = "Utilities", type = TransactionType.EXPENSE, icon = "bolt", color = 0xFFFB8C00, isDefault = true),
            CategoryEntity(name = "Entertainment", type = TransactionType.EXPENSE, icon = "movie", color = 0xFF43A047, isDefault = true),
            CategoryEntity(name = "Health", type = TransactionType.EXPENSE, icon = "favorite", color = 0xFFEF5350, isDefault = true),
            CategoryEntity(name = "Education", type = TransactionType.EXPENSE, icon = "school", color = 0xFF039BE5, isDefault = true),
            CategoryEntity(name = "Rent", type = TransactionType.EXPENSE, icon = "home", color = 0xFF6D4C41, isDefault = true),
            CategoryEntity(name = "EMI", type = TransactionType.EXPENSE, icon = "credit_card", color = 0xFF546E7A, isDefault = true),
            CategoryEntity(name = "Subscriptions", type = TransactionType.EXPENSE, icon = "subscriptions", color = 0xFF00ACC1, isDefault = true),
            CategoryEntity(name = "Travel", type = TransactionType.EXPENSE, icon = "flight", color = 0xFF5C6BC0, isDefault = true),
            CategoryEntity(name = "Gifts", type = TransactionType.EXPENSE, icon = "card_giftcard", color = 0xFFEC407A, isDefault = true),
            CategoryEntity(name = "Groceries", type = TransactionType.EXPENSE, icon = "local_grocery_store", color = 0xFF66BB6A, isDefault = true),
            CategoryEntity(name = "Insurance", type = TransactionType.EXPENSE, icon = "security", color = 0xFF78909C, isDefault = true),
            CategoryEntity(name = "Other Expense", type = TransactionType.EXPENSE, icon = "category", color = 0xFF9E9E9E, isDefault = true),
        )

        val incomeCategories = listOf(
            CategoryEntity(name = "Salary", type = TransactionType.INCOME, icon = "work", color = 0xFF43A047, isDefault = true),
            CategoryEntity(name = "Freelance", type = TransactionType.INCOME, icon = "laptop", color = 0xFF00ACC1, isDefault = true),
            CategoryEntity(name = "Business", type = TransactionType.INCOME, icon = "business", color = 0xFF1E88E5, isDefault = true),
            CategoryEntity(name = "Investment", type = TransactionType.INCOME, icon = "trending_up", color = 0xFF8E24AA, isDefault = true),
            CategoryEntity(name = "Rental Income", type = TransactionType.INCOME, icon = "home", color = 0xFF6D4C41, isDefault = true),
            CategoryEntity(name = "Cashback", type = TransactionType.INCOME, icon = "monetization_on", color = 0xFF00BFA5, isDefault = true),
            CategoryEntity(name = "Refund", type = TransactionType.INCOME, icon = "replay", color = 0xFF26A69A, isDefault = true),
            CategoryEntity(name = "Gift", type = TransactionType.INCOME, icon = "card_giftcard", color = 0xFFEC407A, isDefault = true),
            CategoryEntity(name = "Other Income", type = TransactionType.INCOME, icon = "category", color = 0xFF9E9E9E, isDefault = true),
        )

        categoryDao.insertCategories(expenseCategories + incomeCategories)
    }

    private suspend fun insertDefaultAccounts() {
        val defaultAccounts = listOf(
            AccountEntity(name = "Cash", type = AccountType.CASH, color = 0xFF43A047, icon = "payments"),
            AccountEntity(name = "SBI Bank", type = AccountType.BANK, color = 0xFF1E88E5, icon = "account_balance"),
            AccountEntity(name = "HDFC Credit Card", type = AccountType.CREDIT_CARD, color = 0xFFE53935, icon = "credit_card"),
            AccountEntity(name = "Paytm Wallet", type = AccountType.WALLET, color = 0xFF039BE5, icon = "account_balance_wallet"),
        )
        defaultAccounts.forEach { accountDao.insertAccount(it) }
    }
}
