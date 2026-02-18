package com.expensemanager.ui.screens.import_screen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.*
import com.expensemanager.data.repository.AccountRepository
import com.expensemanager.data.repository.CategoryRepository
import com.expensemanager.data.repository.TransactionRepository
import com.expensemanager.utils.FileImporter
import com.expensemanager.utils.ImportResult
import com.expensemanager.utils.RawTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

data class ImportUiState(
    val fileName: String? = null,
    val headers: List<String> = emptyList(),
    val previewTransactions: List<RawTransaction> = emptyList(),
    val columnMapping: Map<String, String?> = emptyMap(),
    val isImporting: Boolean = false,
    val importSuccess: Int? = null,
    val error: String? = null
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun parseFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val fileName = uri.lastPathSegment ?: "file"
            _uiState.update { it.copy(fileName = fileName, error = null) }

            withContext(Dispatchers.IO) {
                when (val result = FileImporter.parseUri(context, uri)) {
                    is ImportResult.Success -> {
                        // Auto-suggest mappings based on common header names
                        val autoMapping = autoDetectMapping(result.headers)
                        _uiState.update { state ->
                            state.copy(
                                headers = result.headers,
                                previewTransactions = result.transactions,
                                columnMapping = autoMapping
                            )
                        }
                    }
                    is ImportResult.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                }
            }
        }
    }

    fun setColumnMapping(field: String, column: String?) {
        _uiState.update { it.copy(columnMapping = it.columnMapping + (field to column)) }
    }

    fun importTransactions() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null) }

            val defaultAccountId = accountRepository.getAllAccounts()
                .first().firstOrNull()?.id ?: run {
                _uiState.update { it.copy(error = "No accounts found. Please add an account first.", isImporting = false) }
                return@launch
            }

            val mappedTransactions = state.previewTransactions.mapNotNull { raw ->
                mapRowToTransaction(raw, state.columnMapping, defaultAccountId)
            }

            if (mappedTransactions.isEmpty()) {
                _uiState.update { it.copy(error = "No valid transactions could be mapped. Check column mapping.", isImporting = false) }
                return@launch
            }

            transactionRepository.insertTransactions(mappedTransactions)
            _uiState.update { it.copy(importSuccess = mappedTransactions.size, isImporting = false) }
        }
    }

    private suspend fun mapRowToTransaction(
        raw: RawTransaction,
        mapping: Map<String, String?>,
        defaultAccountId: Long
    ): TransactionEntity? {
        val amountStr = raw.row[mapping["amount"]] ?: return null
        val amount = amountStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return null

        val dateStr = raw.row[mapping["date"]] ?: ""
        val dateTime = parseDateFlexible(dateStr) ?: LocalDateTime.now()

        val typeStr = raw.row[mapping["type"]]?.uppercase() ?: "EXPENSE"
        val type = when {
            typeStr.contains("CREDIT") || typeStr.contains("INCOME") -> TransactionType.INCOME
            typeStr.contains("TRANSFER") -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }

        val categoryName = raw.row[mapping["category"]] ?: when (type) {
            TransactionType.EXPENSE -> "Other Expense"
            TransactionType.INCOME -> "Other Income"
            else -> "Other Expense"
        }
        val categoryId = categoryRepository.getOrCreateCategory(categoryName, type)

        val description = raw.row[mapping["description"]]

        return TransactionEntity(
            type = type,
            amount = amount,
            accountId = defaultAccountId,
            categoryId = categoryId,
            dateTime = dateTime,
            notes = description,
            isAutoDetected = true,
            status = PaymentStatus.CLEARED
        )
    }

    private fun parseDateFlexible(dateStr: String): LocalDateTime? {
        val formats = listOf(
            "dd-MM-yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd/MM/yyyy",
            "dd MMM yyyy", "d MMM yyyy", "yyyy-MM-dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss", "MM/dd/yyyy hh:mm a"
        )
        for (format in formats) {
            try {
                return LocalDateTime.parse(dateStr.trim(), DateTimeFormatter.ofPattern(format))
            } catch (_: DateTimeParseException) {
                try {
                    val date = java.time.LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(format))
                    return date.atStartOfDay()
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun autoDetectMapping(headers: List<String>): Map<String, String?> {
        fun findHeader(vararg candidates: String): String? {
            return headers.firstOrNull { h ->
                candidates.any { c -> h.equals(c, ignoreCase = true) || h.contains(c, ignoreCase = true) }
            }
        }
        return mapOf(
            "amount" to findHeader("amount", "debit", "credit", "value", "sum"),
            "date" to findHeader("date", "time", "transaction date", "value date"),
            "type" to findHeader("type", "transaction type", "dr/cr"),
            "category" to findHeader("category", "narration", "description", "particulars"),
            "description" to findHeader("description", "narration", "details", "remarks", "note"),
            "account" to findHeader("account", "bank", "card")
        )
    }
}
