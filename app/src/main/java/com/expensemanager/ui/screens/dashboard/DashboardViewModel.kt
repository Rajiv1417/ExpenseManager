package com.expensemanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entities.TransactionEntity
import com.expensemanager.data.local.entities.AccountEntity
import com.expensemanager.data.repository.AccountRepository
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val pendingAutoDetected: List<TransactionEntity> = emptyList(),
    val dailyExpenses: List<Pair<String, Double>> = emptyList(),
    val categoryExpenses: List<Pair<Long, Double>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)

        viewModelScope.launch {
            combine(
                accountRepository.getTotalBalance().map { it ?: 0.0 },
                transactionRepository.getTotalIncome(startOfMonth, endOfMonth).map { it ?: 0.0 },
                transactionRepository.getTotalExpense(startOfMonth, endOfMonth).map { it ?: 0.0 },
                transactionRepository.getRecentTransactions(15),
                accountRepository.getAllAccounts(),
                transactionRepository.getPendingAutoDetected()
            ) { values ->
                val totalBalance = values[0] as Double
                val income = values[1] as Double
                val expense = values[2] as Double
                @Suppress("UNCHECKED_CAST")
                val recent = values[3] as List<TransactionEntity>
                @Suppress("UNCHECKED_CAST")
                val accounts = values[4] as List<AccountEntity>
                @Suppress("UNCHECKED_CAST")
                val pending = values[5] as List<TransactionEntity>

                DashboardUiState(
                    totalBalance = totalBalance,
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    recentTransactions = recent,
                    accounts = accounts,
                    pendingAutoDetected = pending,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect { state ->
                _uiState.value = state
                loadChartData(startOfMonth, endOfMonth)
            }
        }
    }

    private fun loadChartData(from: LocalDateTime, to: LocalDateTime) {
        viewModelScope.launch {
            val dailySummary = transactionRepository.getDailyExpenseSummary(from, to)
            _uiState.update { state ->
                state.copy(
                    dailyExpenses = dailySummary.map { it.date to it.total }
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
