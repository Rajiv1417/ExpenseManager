package com.expensemanager.utils

import org.junit.Assert.*
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `formatCompact shows lakhs correctly`() {
        val result = CurrencyFormatter.formatCompact(1500000.0)
        assertEquals("₹15.0L", result)
    }

    @Test
    fun `formatCompact shows thousands correctly`() {
        val result = CurrencyFormatter.formatCompact(2500.0)
        assertEquals("₹2.5K", result)
    }

    @Test
    fun `formatCompact shows small amounts as-is`() {
        val result = CurrencyFormatter.formatCompact(350.0)
        assertEquals("₹350", result)
    }
}
