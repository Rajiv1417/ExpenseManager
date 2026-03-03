package com.expensemanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val accountNumber: String?,
    val type: String,
    val initialValue: Double,
    val currency: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis()
)