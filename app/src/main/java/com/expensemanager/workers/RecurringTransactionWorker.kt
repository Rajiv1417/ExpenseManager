package com.expensemanager.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expensemanager.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val transactionId = inputData.getLong("transaction_id", -1L)
        if (transactionId == -1L) return Result.failure()

        val template = transactionRepository.getTransactionById(transactionId)
            ?: return Result.failure()

        // Create new transaction from template
        val newTransaction = template.copy(
            id = 0,
            dateTime = LocalDateTime.now(),
            isAutoDetected = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        transactionRepository.insertTransaction(newTransaction)
        showNotification(template.amount, template.accountId)

        return Result.success()
    }

    private fun showNotification(amount: Double, accountId: Long) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recurring Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recurring Transaction")
            .setContentText("₹$amount has been recorded automatically")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "recurring_transactions"

        fun schedule(
            context: Context,
            transactionId: Long,
            intervalDays: Int
        ): androidx.work.Operation {
            val inputData = workDataOf("transaction_id" to transactionId)

            val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                intervalDays.toLong(), TimeUnit.DAYS
            )
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .addTag("recurring_$transactionId")
                .build()

            return WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "recurring_$transactionId",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }

        fun cancel(context: Context, transactionId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("recurring_$transactionId")
        }
    }
}
