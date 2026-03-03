package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountWithBalance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {

    fun getAllAccounts(): Flow<List<AccountWithBalance>> =
        accountDao.getAccountsWithBalance()

    suspend fun getAccountById(id: Long): AccountEntity? =
        accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long =
        accountDao.insert(account)

    suspend fun updateAccount(account: AccountEntity) =
        accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) =
        accountDao.delete(account)
}
