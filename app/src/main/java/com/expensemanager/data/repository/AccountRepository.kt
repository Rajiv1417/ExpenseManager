package com.expensemanager.data.repository

import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllActiveAccounts()

    fun getTotalBalance(): Flow<Double?> = accountDao.getTotalBalance()

    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)
}
