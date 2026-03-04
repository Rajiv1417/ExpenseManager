package com.expensemanager.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountType
import com.expensemanager.data.local.entities.AccountWithBalance
import com.expensemanager.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountWithBalance> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    private val accountColors = listOf(
        0xFF43A047,
        0xFF1A6EDD,
        0xFFE53935,
        0xFF8E24AA,
        0xFF00B67B,
        0xFFFF8F00
    )

    init {
        viewModelScope.launch {
            accountRepository.getAllAccounts()
                .collect { accounts ->
                    val total = accounts.sumOf { it.balance }
                    _uiState.value = AccountsUiState(
                        accounts = accounts,
                        totalBalance = total,
                        isLoading = false
                    )
                }
        }
    }

    fun addAccount(
        name: String,
        type: AccountType,
        balance: Double,
        color: Long,
        symbol: String?
    ) {
        viewModelScope.launch {
            accountRepository.insertAccount(
                AccountEntity(
                    name = name,
                    accountNumber = symbol.ifBlank { null },
                    type = type,
                    initialValue = balance,
                    currency = "INR",
                    color = color,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}

    fun updateAccount(
        account: AccountEntity,
        name: String,
        type: AccountType,
        balance: Double,
        color: Long,
        symbol: String
    ) {
        viewModelScope.launch {
            accountRepository.updateAccount(
                account.copy(
                    name = name,
                    type = type,
                    initialValue = balance,
                    color = color,
                    accountNumber = symbol.ifBlank { null }
                )
            )
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
        }
    }

    fun getSuggestedColor(): Long {
        val used = _uiState.value.accounts.map { it.account.color }.toSet()
        return accountColors.firstOrNull { it !in used } ?: accountColors.first()
    }
}
