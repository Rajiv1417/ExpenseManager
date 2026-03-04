package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.dao.DailySummary
import com.expensemanager.data.local.dao.MonthlySummary
import com.expensemanager.data.local.dao.TransactionDao
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    fun getFilteredTransactions(
        accountId: Long? = null,
        categoryId: Long? = null,
        type: TransactionType? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<TransactionEntity>> = transactionDao.getFilteredTransactions(
        accountId  = accountId,
        categoryId = categoryId,
        type       = type?.name,
        minAmount  = minAmount,
        maxAmount  = maxAmount,
        from       = from,
        to         = to,
        limit      = limit,
        offset     = offset
    )

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<TransactionEntity>) =
        transactions.forEach { insertTransaction(it) }

    suspend fun updateTransaction(old: TransactionEntity, new: TransactionEntity) =
        transactionDao.updateTransaction(new)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    fun getTotalIncome(from: LocalDateTime, to: LocalDateTime): Flow<Double?> =
        transactionDao.getTotalIncome(from, to)

    fun getTotalExpense(from: LocalDateTime, to: LocalDateTime): Flow<Double?> =
        transactionDao.getTotalExpense(from, to)

    fun getTransactionWithRefunds(id: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionWithRefunds(id)

    fun getPendingAutoDetected(): Flow<List<TransactionEntity>> =
        transactionDao.getPendingAutoDetectedTransactions()

    suspend fun getDailyExpenseSummary(from: LocalDateTime, to: LocalDateTime): List<DailySummary> =
        transactionDao.getDailyExpenseSummary(from, to)

    suspend fun getMonthlyExpenseSummary(from: LocalDateTime, to: LocalDateTime): List<MonthlySummary> =
        transactionDao.getMonthlyExpenseSummary(from, to)

    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccount(accountId)
}
