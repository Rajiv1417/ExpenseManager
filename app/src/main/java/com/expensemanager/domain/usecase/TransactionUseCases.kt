package com.expensemanager.domain.usecase

import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.TransactionType
import com.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        accountId: Long? = null,
        categoryId: Long? = null,
        type: TransactionType? = null,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): Flow<List<TransactionEntity>> = repository.getFilteredTransactions(
        accountId = accountId,
        categoryId = categoryId,
        type = type,
        from = from,
        to = to
    )
}

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity): Long =
        repository.insertTransaction(transaction)
}

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(old: TransactionEntity, new: TransactionEntity) =
        repository.updateTransaction(old, new)
}

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) =
        repository.deleteTransaction(transaction)
}

class LinkRefundUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * Links a cashback/refund transaction to the original expense.
     * Creates the refund as a new income transaction, and links them bidirectionally.
     */
    suspend operator fun invoke(
        originalTransactionId: Long,
        refundTransaction: TransactionEntity
    ): Long {
        val original = repository.getTransactionById(originalTransactionId)
            ?: throw IllegalArgumentException("Original transaction not found")

        val isPartial = refundTransaction.amount < original.amount

        // Insert refund transaction linked to original
        val refundWithLink = refundTransaction.copy(
            linkedRefundTransactionId = originalTransactionId
        )
        val refundId = repository.insertTransaction(refundWithLink)

        // Update original transaction with refund info
        val updatedOriginal = original.copy(
            linkedRefundTransactionId = refundId,
            refundAmount = refundTransaction.amount,
            isPartialRefund = isPartial,
            updatedAt = LocalDateTime.now()
        )
        repository.updateTransaction(original, updatedOriginal)

        return refundId
    }
}

class ImportTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactions: List<TransactionEntity>) {
        repository.insertTransactions(transactions)
    }
}
