package com.expensemanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.TransactionType
import com.expensemanager.data.local.entities.PaymentStatus
import com.expensemanager.data.repository.TransactionRepository
import com.expensemanager.data.repository.CategoryRepository
import com.expensemanager.data.repository.AccountRepository

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages.forEach { smsMessage ->
            val sender = smsMessage.originatingAddress ?: ""
            val body = smsMessage.messageBody ?: ""

            Log.d("SmsReceiver", "Received SMS from $sender")

            val parsed = SmsParser.parse(body, sender) ?: return@forEach

            scope.launch {
                saveParsedTransaction(parsed)
            }
        }
    }

    private suspend fun saveParsedTransaction(parsed: ParsedSmsTransaction) {
        try {
            // Find matching account by last4 digits or bank name
            val accounts = mutableListOf<com.expensemanager.data.local.entities.AccountEntity>()
            accountRepository.getAllAccounts().collect { accounts.addAll(it) }

            val matchedAccount = accounts.firstOrNull { account ->
                parsed.accountLast4?.let { account.name.contains(it) } == true ||
                        parsed.bank?.let { account.name.contains(it, ignoreCase = true) } == true
            } ?: accounts.firstOrNull() ?: return

            val type = when (parsed.type) {
                SmsTransactionType.DEBIT -> TransactionType.EXPENSE
                SmsTransactionType.CREDIT -> TransactionType.INCOME
                SmsTransactionType.UNKNOWN -> TransactionType.EXPENSE
            }

            val categoryId = categoryRepository.getOrCreateCategory(
                name = when (type) {
                    TransactionType.EXPENSE -> "Other Expense"
                    TransactionType.INCOME -> "Other Income"
                    else -> "Other Expense"
                },
                type = type
            )

            val transaction = TransactionEntity(
                type = type,
                amount = parsed.amount,
                accountId = matchedAccount.id,
                categoryId = categoryId,
                dateTime = parsed.dateTime,
                payee = parsed.payee,
                notes = "Auto-detected from SMS: ${parsed.bank ?: "Unknown"}",
                isAutoDetected = true,
                smsSource = parsed.rawSms,
                status = PaymentStatus.PENDING // Requires user confirmation
            )

            transactionRepository.insertTransaction(transaction)
            Log.d("SmsReceiver", "Saved auto-detected transaction: ${parsed.amount}")
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Failed to save transaction: ${e.message}")
        }
    }
}
