package com.expensemanager.ui.screens.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.*
import com.expensemanager.data.repository.AccountRepository
import com.expensemanager.data.repository.CategoryRepository
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val selectedAccount: AccountWithBalance? = null,
    val selectedCategory: CategoryEntity? = null,
    val selectedToAccount: AccountWithBalance? = null,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val payee: String = "",
    val labels: List<String> = emptyList(),
    val paymentType: PaymentType = PaymentType.UPI,
    val status: PaymentStatus = PaymentStatus.CLEARED,
    val isRecurring: Boolean = false,
    val recurringIntervalDays: Int = 30,
    val showRefundSheet: Boolean = false,
    val refundAmount: String = "",
    val refundAccountId: Long? = null,
    val accounts: List<AccountWithBalance> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val editingTransactionId: Long? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
        loadCategories()
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.getTransactionById(transactionId) ?: return@launch
            val category = categoryRepository.getCategoryById(tx.categoryId)
            _uiState.update { state ->
                state.copy(
                    isEditMode            = true,
                    editingTransactionId  = transactionId,
                    transactionType       = tx.type,
                    amount                = tx.amount.toString(),
                    selectedCategory      = category,
                    dateTime              = tx.dateTime,
                    notes                 = tx.notes ?: "",
                    payee                 = tx.payee ?: "",
                    labels                = tx.labels,
                    paymentType           = tx.paymentType,
                    status                = tx.status,
                    isRecurring           = tx.isRecurring,
                    recurringIntervalDays = tx.recurringIntervalDays ?: 30
                )
            }
            // Match to already-loaded accounts
            val matched = _uiState.value.accounts.firstOrNull { it.account.id == tx.accountId }
            if (matched != null) _uiState.update { it.copy(selectedAccount = matched) }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(_uiState.value.transactionType).collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type, selectedCategory = null) }
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(type).first().let { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun setAmount(amount: String)                 = _uiState.update { it.copy(amount = amount) }
    fun setAccount(account: AccountWithBalance)   = _uiState.update { it.copy(selectedAccount = account) }
    fun setToAccount(account: AccountWithBalance) = _uiState.update { it.copy(selectedToAccount = account) }
    fun setCategory(cat: CategoryEntity)          = _uiState.update { it.copy(selectedCategory = cat) }
    fun setDateTime(dt: LocalDateTime)            = _uiState.update { it.copy(dateTime = dt) }
    fun setNotes(notes: String)                   = _uiState.update { it.copy(notes = notes) }
    fun setPayee(payee: String)                   = _uiState.update { it.copy(payee = payee) }
    fun setPaymentType(type: PaymentType)         = _uiState.update { it.copy(paymentType = type) }
    fun setStatus(status: PaymentStatus)          = _uiState.update { it.copy(status = status) }
    fun setRecurring(r: Boolean)                  = _uiState.update { it.copy(isRecurring = r) }
    fun setRecurringInterval(days: Int)           = _uiState.update { it.copy(recurringIntervalDays = days) }
    fun addLabel(label: String)                   = _uiState.update { it.copy(labels = it.labels + label) }
    fun removeLabel(label: String)                = _uiState.update { it.copy(labels = it.labels - label) }
    fun setRefundAmount(a: String)                = _uiState.update { it.copy(refundAmount = a) }
    fun setRefundAccount(id: Long)                = _uiState.update { it.copy(refundAccountId = id) }
    fun showRefundSheet(show: Boolean)            = _uiState.update { it.copy(showRefundSheet = show) }
    fun clearError()                              = _uiState.update { it.copy(error = null) }

    fun saveTransaction() {
        val state  = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }; return
        }
        val account = state.selectedAccount
        if (account == null) {
            _uiState.update { it.copy(error = "Please select an account") }; return
        }
        if (state.selectedCategory == null && state.transactionType != TransactionType.TRANSFER) {
            _uiState.update { it.copy(error = "Please select a category") }; return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transaction = TransactionEntity(
                    id                    = state.editingTransactionId ?: 0L,
                    type                  = state.transactionType,
                    amount                = amount,
                    accountId             = account.account.id,
                    categoryId            = state.selectedCategory?.id ?: 1L,
                    toAccountId           = state.selectedToAccount?.account?.id,
                    dateTime              = state.dateTime,
                    notes                 = state.notes.ifBlank { null },
                    payee                 = state.payee.ifBlank { null },
                    labels                = state.labels,
                    paymentType           = state.paymentType,
                    status                = state.status,
                    isRecurring           = state.isRecurring,
                    recurringIntervalDays = if (state.isRecurring) state.recurringIntervalDays else null,
                    updatedAt             = LocalDateTime.now()
                )
                if (state.isEditMode) {
                    val old = transactionRepository.getTransactionById(state.editingTransactionId!!)!!
                    transactionRepository.updateTransaction(old, transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun saveRefund(originalTransactionId: Long) {
        val state        = _uiState.value
        val refundAmount = state.refundAmount.toDoubleOrNull() ?: return
        val refundAccId  = state.refundAccountId ?: state.selectedAccount?.account?.id ?: return

        viewModelScope.launch {
            try {
                val categoryId = categoryRepository.getOrCreateCategory("Cashback", TransactionType.INCOME)
                transactionRepository.insertTransaction(
                    TransactionEntity(
                        type                     = TransactionType.INCOME,
                        amount                   = refundAmount,
                        accountId                = refundAccId,
                        categoryId               = categoryId,
                        dateTime                 = LocalDateTime.now(),
                        notes                    = "Cashback/Refund for tx#$originalTransactionId",
                        status                   = PaymentStatus.CLEARED,
                        linkedRefundTransactionId = originalTransactionId
                    )
                )
                _uiState.update { it.copy(showRefundSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
