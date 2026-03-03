package com.expensemanager.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.dao.AccountDao
import com.expensemanager.data.local.entities.AccountEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountDao: AccountDao
) : ViewModel() {

    var name = ""
    var number = ""
    var type = "General"
    var initialValue = ""
    var currency = detectCurrency()
    var color: Long = 0xFF00C853

    var error: String? = null

    fun save(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            error = "Account name required"
            return
        }

        viewModelScope.launch {
            accountDao.insert(
                AccountEntity(
                    name = name,
                    accountNumber = number.takeIf { it.isNotBlank() },
                    type = type,
                    initialValue = initialValue.toDoubleOrNull() ?: 0.0,
                    currency = currency,
                    color = color
                )
            )
            onSuccess()
        }
    }

    private fun detectCurrency(): String {
        return try {
            Currency.getInstance(Locale.getDefault()).currencyCode
        } catch (e: Exception) {
            "INR"
        }
    }
}