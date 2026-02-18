package com.expensemanager.data.local.dao

import androidx.room.*
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY dateTime DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY dateTime DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateTime DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE dateTime BETWEEN :from AND :to 
        ORDER BY dateTime DESC
    """)
    fun getTransactionsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:accountId IS NULL OR accountId = :accountId)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:type IS NULL OR type = :type)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:from IS NULL OR dateTime >= :from)
        AND (:to IS NULL OR dateTime <= :to)
        ORDER BY dateTime DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getFilteredTransactions(
        accountId: Long? = null,
        categoryId: Long? = null,
        type: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND dateTime BETWEEN :from AND :to")
    fun getTotalIncome(from: LocalDateTime, to: LocalDateTime): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateTime BETWEEN :from AND :to")
    fun getTotalExpense(from: LocalDateTime, to: LocalDateTime): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND categoryId = :categoryId AND dateTime BETWEEN :from AND :to")
    suspend fun getTotalExpenseByCategory(categoryId: Long, from: LocalDateTime, to: LocalDateTime): Double?

    @Query("SELECT * FROM transactions WHERE linkedRefundTransactionId = :transactionId")
    fun getRefundsForTransaction(transactionId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id OR linkedRefundTransactionId = :id")
    fun getTransactionWithRefunds(id: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("""
        SELECT strftime('%Y-%m-%d', dateTime) as date, SUM(amount) as total
        FROM transactions 
        WHERE type = 'EXPENSE' AND dateTime BETWEEN :from AND :to
        GROUP BY strftime('%Y-%m-%d', dateTime)
        ORDER BY date
    """)
    suspend fun getDailyExpenseSummary(from: LocalDateTime, to: LocalDateTime): List<DailySummary>

    @Query("""
        SELECT strftime('%Y-%m', dateTime) as month, SUM(amount) as total
        FROM transactions 
        WHERE type = 'EXPENSE' AND dateTime BETWEEN :from AND :to
        GROUP BY strftime('%Y-%m', dateTime)
        ORDER BY month
    """)
    suspend fun getMonthlyExpenseSummary(from: LocalDateTime, to: LocalDateTime): List<MonthlySummary>

    @Query("SELECT * FROM transactions WHERE isAutoDetected = 1 AND status = 'PENDING'")
    fun getPendingAutoDetectedTransactions(): Flow<List<TransactionEntity>>
}

data class DailySummary(val date: String, val total: Double)
data class MonthlySummary(val month: String, val total: Double)
