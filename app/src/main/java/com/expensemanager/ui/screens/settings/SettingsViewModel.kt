package com.expensemanager.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val smsDetectionEnabled: Boolean = true,
    val recurringRemindersEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkMode(enabled: Boolean) = _uiState.update { it.copy(isDarkMode = enabled) }
    fun setSmsDetection(enabled: Boolean) = _uiState.update { it.copy(smsDetectionEnabled = enabled) }
    fun setRecurringReminders(enabled: Boolean) = _uiState.update { it.copy(recurringRemindersEnabled = enabled) }

    fun getTransactions(onResult: (List<TransactionEntity>) -> Unit) {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().first().let { onResult(it) }
        }
    }
}
