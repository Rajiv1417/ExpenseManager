package com.expensemanager.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.local.entities.AccountType
import com.expensemanager.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
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
            combine(
                accountRepository.getAllAccounts(),
                accountRepository.getTotalBalance().map { it ?: 0.0 }
            ) { accounts, total ->
                AccountsUiState(accounts = accounts, totalBalance = total, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    suspend fun getAccountById(id: Long): AccountEntity? = accountRepository.getAccountById(id)

    fun saveAccount(
        accountId: Long?,
        name: String,
        type: AccountType,
        currency: String,
        color: Long,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val existing = accountId?.let { accountRepository.getAccountById(it) }
            if (existing == null) {
                accountRepository.insertAccount(
                    AccountEntity(
                        name = name,
                        type = type,
                        currency = currency,
                        color = color,
                        balance = 0.0,
                        initialBalance = 0.0,
                        isActive = isActive
                    )
                )
            } else {
                accountRepository.updateAccount(
                    existing.copy(
                        name = name,
                        type = type,
                        currency = currency,
                        color = color,
                        isActive = isActive
                    )
                )
            }
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            accountRepository.getAccountById(accountId)?.let { accountRepository.deleteAccount(it) }
        }
    }
}
