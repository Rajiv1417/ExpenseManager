package com.expensemanager.data.local.dao

import androidx.room.*
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?
    
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC")
    suspend fun getAllAccountsOnce(): List<AccountEntity>
    
     @Query("""
SELECT 
    a.*,
    a.initialValue
    + COALESCE(SUM(
        CASE 
            WHEN t.type = 'INCOME' AND t.accountId = a.id THEN t.amount
            WHEN t.type = 'EXPENSE' AND t.accountId = a.id THEN -t.amount
            WHEN t.type = 'TRANSFER' AND t.accountId = a.id THEN -t.amount
            WHEN t.type = 'TRANSFER' AND t.toAccountId = a.id THEN t.amount
            ELSE 0
        END
    ), 0) AS balance
FROM accounts a
LEFT JOIN transactions t
ON t.accountId = a.id OR t.toAccountId = a.id
GROUP BY a.id
ORDER BY a.createdAt DESC
""")
    fun getAccountsWithBalance(): Flow<List<AccountWithBalance>>
}
