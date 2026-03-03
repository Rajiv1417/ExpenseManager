package com.expensemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.entities.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}