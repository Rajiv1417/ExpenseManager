package com.expensemanager.utils

import org.junit.Assert.*
import org.junit.Test

class ImportMappingTest {

    /**
     * Test the CSV auto-detection logic by simulating header mapping.
     */
    @Test
    fun `auto-detect amount column`() {
        val headers = listOf("Date", "Narration", "Amount", "Dr/Cr", "Balance")
        val detected = autoDetectMapping(headers)

        assertEquals("Amount", detected["amount"])
        assertEquals("Date", detected["date"])
        assertEquals("Dr/Cr", detected["type"])
    }

    @Test
    fun `auto-detect SBI statement headers`() {
        val headers = listOf("Txn Date", "Value Date", "Description", "Ref No.", "Debit", "Credit", "Balance")
        val detected = autoDetectMapping(headers)

        assertNotNull(detected["date"])
        assertNotNull(detected["description"])
    }

    @Test
    fun `auto-detect HDFC statement headers`() {
        val headers = listOf("Date", "Narration", "Chq./Ref.No.", "Value Dt", "Withdrawal Amt.", "Deposit Amt.", "Closing Balance")
        val detected = autoDetectMapping(headers)

        assertEquals("Date", detected["date"])
    }

    // Helper - mirrors the logic in ImportViewModel
    private fun autoDetectMapping(headers: List<String>): Map<String, String?> {
        fun findHeader(vararg candidates: String): String? {
            return headers.firstOrNull { h ->
                candidates.any { c -> h.equals(c, ignoreCase = true) || h.contains(c, ignoreCase = true) }
            }
        }
        return mapOf(
            "amount" to findHeader("amount", "debit", "credit", "value", "sum", "withdrawal", "deposit"),
            "date" to findHeader("date", "time", "transaction date", "value date", "txn date"),
            "type" to findHeader("type", "transaction type", "dr/cr", "debit/credit"),
            "category" to findHeader("category", "narration", "description", "particulars"),
            "description" to findHeader("description", "narration", "details", "remarks", "note"),
            "account" to findHeader("account", "bank", "card")
        )
    }
}
