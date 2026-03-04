package com.expensemanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.expensemanager.data.local.DatabaseInitializer
import javax.inject.Inject

@HiltAndroidApp
class ExpenseManagerApp : Application()
