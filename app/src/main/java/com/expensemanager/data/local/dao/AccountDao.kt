package com.expensemanager.data.local.dao

import androidx.room.*
import com.expensemanager.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("""
        SELECT a.id,
               a.name,
               a.accountNumber,
               a.type,
               a.initialValue,
               a.currency,
               a.color,
               a.createdAt,
               (a.initialValue + IFNULL(
                    SUM(
                        CASE
                            WHEN t.type = 'INCOME' THEN t.amount
                            WHEN t.type = 'EXPENSE' THEN -t.amount
                            WHEN t.type = 'TRANSFER' THEN 0
                        END
                    ), 0
               )) as balance
        FROM accounts a
        LEFT JOIN transactions t ON a.id = t.accountId
        GROUP BY a.id
        ORDER BY a.createdAt DESC
    """)
    fun getAccountsWithBalance(): Flow<List<AccountWithBalance>>
}
