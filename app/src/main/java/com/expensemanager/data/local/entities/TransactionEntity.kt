package com.expensemanager.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("accountId"), Index("categoryId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val accountId: Long,
    val categoryId: Long,
    val dateTime: LocalDateTime,
    val notes: String? = null,
    val payee: String? = null,
    val labels: List<String> = emptyList(),
    val paymentType: PaymentType = PaymentType.UPI,
    val status: PaymentStatus = PaymentStatus.CLEARED,
    val isRecurring: Boolean = false,
    val recurringIntervalDays: Int? = null,
    // For transfer transactions
    val toAccountId: Long? = null,
    // Refund/cashback linkage
    val linkedRefundTransactionId: Long? = null,
    val refundAmount: Double? = null,
    val isPartialRefund: Boolean = false,
    // SMS/OCR auto-detected
    val isAutoDetected: Boolean = false,
    val smsSource: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

enum class PaymentType {
    CASH, CARD, UPI, BANK_TRANSFER, CHEQUE, WALLET, OTHER
}

enum class PaymentStatus {
    CLEARED, PENDING, RECONCILED
}
