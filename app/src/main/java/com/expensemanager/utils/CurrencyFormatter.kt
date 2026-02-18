package com.expensemanager.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun format(amount: Double): String {
        return inrFormat.format(amount)
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 10_00_000 -> "₹${String.format("%.1f", amount / 10_00_000)}L"
            amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
            else -> "₹${String.format("%.0f", amount)}"
        }
    }
}
