package com.expensemanager.data.local.entities

data class AccountWithBalance(
    val id: Long,
    val name: String,
    val accountNumber: String?,
    val type: String,
    val initialValue: Double,
    val currency: String,
    val color: Long,
    val createdAt: Long,
    val balance: Double
)
