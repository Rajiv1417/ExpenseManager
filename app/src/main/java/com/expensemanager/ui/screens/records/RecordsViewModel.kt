package com.expensemanager.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    fun load(accountIds: List<Long>) {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { all ->
                val filtered = if (accountIds.isEmpty()) all else all.filter { it.accountId in accountIds }
                _uiState.update { it.copy(transactions = filtered, isLoading = false) }
            }
        }
    }
}
