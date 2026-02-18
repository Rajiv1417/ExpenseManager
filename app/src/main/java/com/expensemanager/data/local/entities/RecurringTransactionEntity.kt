package com.expensemanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateTransactionId: Long,
    val nextDueDate: LocalDateTime,
    val intervalDays: Int,
    val endDate: LocalDateTime? = null,
    val isActive: Boolean = true,
    val workManagerRequestId: String? = null
)
