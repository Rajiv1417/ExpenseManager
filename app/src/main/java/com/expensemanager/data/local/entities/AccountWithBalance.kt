package com.expensemanager.data.local.entities

import androidx.room.Embedded

data class AccountWithBalance(
    @Embedded
    val account: AccountEntity,
    val balance: Double
)