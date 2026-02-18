package com.expensemanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Double = 0.0,
    val type: AccountType = AccountType.BANK,
    val currency: String = "INR",
    val color: Long = 0xFF6200EE,
    val icon: String = "account_balance",
    val isActive: Boolean = true,
    val initialBalance: Double = 0.0
)

enum class AccountType {
    BANK, CASH, WALLET, CREDIT_CARD, SAVINGS, INVESTMENT, OTHER
}
