package com.expensemanager.ui.screens.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.*
import com.expensemanager.data.repository.AccountRepository
import com.expensemanager.data.repository.CategoryRepository
import com.expensemanager.data.repository.TransactionRepository
import com.expensemanager.domain.usecase.LinkRefundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val selectedAccount: AccountEntity? = null,
    val selectedCategory: CategoryEntity? = null,
    val selectedToAccount: AccountEntity? = null,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val payee: String = "",
    val labels: List<String> = emptyList(),
    val paymentType: PaymentType = PaymentType.UPI,
    val status: PaymentStatus = PaymentStatus.CLEARED,
    val isRecurring: Boolean = false,
    val recurringIntervalDays: Int = 30,
    // Refund
    val showRefundSheet: Boolean = false,
    val refundAmount: String = "",
    val refundAccountId: Long? = null,
    // UI state
    val accounts: List<AccountEntity> = emptyList(),
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
    private val categoryRepository: CategoryRepository,
    private val linkRefundUseCase: LinkRefundUseCase
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
            val account = accountRepository.getAccountById(tx.accountId)
            val category = categoryRepository.getCategoryById(tx.categoryId)

            _uiState.update { state ->
                state.copy(
                    isEditMode = true,
                    editingTransactionId = transactionId,
                    transactionType = tx.type,
                    amount = tx.amount.toString(),
                    selectedAccount = account,
                    selectedCategory = category,
                    dateTime = tx.dateTime,
                    notes = tx.notes ?: "",
                    payee = tx.payee ?: "",
                    labels = tx.labels,
                    paymentType = tx.paymentType,
                    status = tx.status,
                    isRecurring = tx.isRecurring,
                    recurringIntervalDays = tx.recurringIntervalDays ?: 30
                )
            }
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

    fun setAmount(amount: String) = _uiState.update { it.copy(amount = amount) }
    fun setAccount(account: AccountEntity) = _uiState.update { it.copy(selectedAccount = account) }
    fun setToAccount(account: AccountEntity) = _uiState.update { it.copy(selectedToAccount = account) }
    fun setCategory(category: CategoryEntity) = _uiState.update { it.copy(selectedCategory = category) }
    fun setDateTime(dateTime: LocalDateTime) = _uiState.update { it.copy(dateTime = dateTime) }
    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun setPayee(payee: String) = _uiState.update { it.copy(payee = payee) }
    fun setPaymentType(type: PaymentType) = _uiState.update { it.copy(paymentType = type) }
    fun setStatus(status: PaymentStatus) = _uiState.update { it.copy(status = status) }
    fun setRecurring(isRecurring: Boolean) = _uiState.update { it.copy(isRecurring = isRecurring) }
    fun setRecurringInterval(days: Int) = _uiState.update { it.copy(recurringIntervalDays = days) }
    fun addLabel(label: String) = _uiState.update { it.copy(labels = it.labels + label) }
    fun removeLabel(label: String) = _uiState.update { it.copy(labels = it.labels - label) }
    fun setRefundAmount(amount: String) = _uiState.update { it.copy(refundAmount = amount) }
    fun setRefundAccount(accountId: Long) = _uiState.update { it.copy(refundAccountId = accountId) }
    fun showRefundSheet(show: Boolean) = _uiState.update { it.copy(showRefundSheet = show) }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (state.selectedAccount == null) {
            _uiState.update { it.copy(error = "Please select an account") }
            return
        }
        if (state.selectedCategory == null && state.transactionType != TransactionType.TRANSFER) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transaction = TransactionEntity(
                    id = state.editingTransactionId ?: 0L,
                    type = state.transactionType,
                    amount = amount,
                    accountId = state.selectedAccount.id,
                    categoryId = state.selectedCategory?.id ?: 1L,
                    toAccountId = state.selectedToAccount?.id,
                    dateTime = state.dateTime,
                    notes = state.notes.ifBlank { null },
                    payee = state.payee.ifBlank { null },
                    labels = state.labels,
                    paymentType = state.paymentType,
                    status = state.status,
                    isRecurring = state.isRecurring,
                    recurringIntervalDays = if (state.isRecurring) state.recurringIntervalDays else null,
                    updatedAt = LocalDateTime.now()
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
        val state = _uiState.value
        val refundAmount = state.refundAmount.toDoubleOrNull() ?: return
        val refundAccountId = state.refundAccountId ?: state.selectedAccount?.id ?: return

        viewModelScope.launch {
            try {
                val categoryId = categoryRepository.getOrCreateCategory("Cashback", TransactionType.INCOME)
                val refundTx = TransactionEntity(
                    type = TransactionType.INCOME,
                    amount = refundAmount,
                    accountId = refundAccountId,
                    categoryId = categoryId,
                    dateTime = LocalDateTime.now(),
                    notes = "Cashback/Refund",
                    status = PaymentStatus.CLEARED
                )
                linkRefundUseCase(originalTransactionId, refundTx)
                _uiState.update { it.copy(showRefundSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
