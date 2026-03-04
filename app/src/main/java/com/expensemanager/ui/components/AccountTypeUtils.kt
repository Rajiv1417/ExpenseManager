package com.expensemanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*

fun getAccountIcon(type: String) = when (type) {
    "Cash" -> Icons.Default.Payments
    "Credit card" -> Icons.Default.CreditCard
    "Loan" -> Icons.Default.AccountBalance
    "Investment" -> Icons.AutoMirrored.Filled.TrendingUp
    else -> Icons.Default.AccountBalanceWallet
}
