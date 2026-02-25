package com.expensemanager.ui.screens.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.repository.AccountRepository
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailUiState(
    val account: AccountEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    fun load(accountId: Long) {
        viewModelScope.launch {
            val account = accountRepository.getAccountById(accountId)
            _uiState.update { it.copy(account = account, isLoading = false) }
        }
        viewModelScope.launch {
            transactionRepository.getTransactionsByAccount(accountId).collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }
    }
}
