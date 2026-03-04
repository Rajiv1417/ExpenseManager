package com.expensemanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.expensemanager.data.local.converters.Converters
import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.dao.CategoryDao
import com.expensemanager.data.local.dao.TransactionDao
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.CategoryEntity
import com.expensemanager.data.local.entities.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "expense_manager.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
