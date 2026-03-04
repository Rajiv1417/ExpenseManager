package com.expensemanager.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountWithBalance
import com.expensemanager.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
        balance: Double
    ) {
        viewModelScope.launch {
            accountRepository.insertAccount(
                AccountEntity(
                    name = name,
                    accountNumber = null,
                    type = type,
                    initialValue = balance,
                    currency = "INR",
                    color = 0xFF4CAF50,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}