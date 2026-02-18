package com.expensemanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expensemanager.data.local.entities.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType, // EXPENSE or INCOME
    val icon: String = "category",
    val color: Long = 0xFF6200EE,
    val isDefault: Boolean = false
)
