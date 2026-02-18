package com.expensemanager.utils


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ParsedSmsTransaction(
    val type: SmsTransactionType,
    val amount: Double,
    val accountLast4: String?,
    val payee: String?,
    val merchant: String?,
    val balance: Double?,
    val rawSms: String,
    val bank: String?,
    val dateTime: LocalDateTime = LocalDateTime.now()
)

enum class SmsTransactionType {
    DEBIT, CREDIT, UNKNOWN
}

object SmsParser {


    // ─── Amount Patterns ───────────────────────────────────────────────────────
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+\.?\d*)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE),
        Regex("""(?:debited|credited|spent|paid|payment of|amount of)\s+(?:Rs\.?|INR|₹)?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    )

    // ─── Debit/Credit Patterns ──────────────────────────────────────────────────
    private val DEBIT_KEYWORDS = Regex(
        """(?:debited|debit|withdrawn|spent|payment made|paid|purchase|pos|neft to|imps to|upi:.*?debit)""",
        RegexOption.IGNORE_CASE
    )
    private val CREDIT_KEYWORDS = Regex(
        """(?:credited|credit|received|deposit|refund|cashback|upi:.*?credit|neft from|imps from)""",
        RegexOption.IGNORE_CASE
    )

    // ─── Account Number ─────────────────────────────────────────────────────────
    private val ACCOUNT_PATTERN = Regex(
        """(?:a/c|ac|account|acct)[\s\w]*?[xX*]{2,}(\d{4})|ending\s+(?:with\s+)?(\d{4})|xxxx(\d{4})""",
        RegexOption.IGNORE_CASE
    )

    // ─── Payee/Merchant Patterns ────────────────────────────────────────────────
    private val MERCHANT_PATTERNS = listOf(
        Regex("""(?:at|to merchant|merchant)\s+([A-Za-z0-9 &.'-]{3,40})""", RegexOption.IGNORE_CASE),
        Regex("""(?:UPI/P2M/|UPI-)([A-Za-z0-9@._-]{3,40})""", RegexOption.IGNORE_CASE),
        Regex("""(?:to|at)\s+([A-Z][A-Za-z0-9 &.'-]{2,30})""", RegexOption.IGNORE_CASE),
        Regex("""VPA\s+(\S+)""", RegexOption.IGNORE_CASE)
    )

    // ─── Balance Pattern ────────────────────────────────────────────────────────
    private val BALANCE_PATTERN = Regex(
        """(?:available balance|avail bal|bal|balance)[:\s]+(?:Rs\.?|INR|₹)?\s*([\d,]+\.?\d*)""",
        RegexOption.IGNORE_CASE
    )

    // ─── Bank Identifiers ───────────────────────────────────────────────────────
    private val BANK_PATTERNS = mapOf(
        "SBI" to Regex("""SBI|State Bank""", RegexOption.IGNORE_CASE),
        "HDFC" to Regex("""HDFC""", RegexOption.IGNORE_CASE),
        "ICICI" to Regex("""ICICI""", RegexOption.IGNORE_CASE),
        "Axis" to Regex("""Axis Bank""", RegexOption.IGNORE_CASE),
        "Kotak" to Regex("""Kotak""", RegexOption.IGNORE_CASE),
        "PNB" to Regex("""PNB|Punjab National""", RegexOption.IGNORE_CASE),
        "BOB" to Regex("""Bank of Baroda""", RegexOption.IGNORE_CASE),
        "Paytm" to Regex("""Paytm""", RegexOption.IGNORE_CASE),
        "GPay" to Regex("""Google Pay|GPay""", RegexOption.IGNORE_CASE),
        "PhonePe" to Regex("""PhonePe""", RegexOption.IGNORE_CASE),
        "IDBI" to Regex("""IDBI""", RegexOption.IGNORE_CASE),
        "Yes Bank" to Regex("""Yes Bank""", RegexOption.IGNORE_CASE),
        "IndusInd" to Regex("""IndusInd""", RegexOption.IGNORE_CASE),
        "Federal" to Regex("""Federal Bank""", RegexOption.IGNORE_CASE),
    )

    // ─── Bank-Specific SMS Templates ────────────────────────────────────────────
    // SBI: "INR 500.00 debited from your SBI account XX1234 to VPA xyz@upi..."
    // HDFC: "Rs.500 debited from HDFC Bank account **1234 for UPI/Merchant..."
    // ICICI: "ICICI Bk Acct XX1234 debited for Rs 500 on..."
    // Paytm: "You paid Rs 500 to Merchant Name via Paytm..."

    fun parse(sms: String, sender: String = ""): ParsedSmsTransaction? {
        if (!isTransactionSms(sms)) return null

        return try {
            val type = detectType(sms)
            val amount = extractAmount(sms) ?: return null
            val accountLast4 = extractAccountLast4(sms)
            val payee = extractMerchant(sms)
            val balance = extractBalance(sms)
            val bank = detectBank(sms, sender)

            ParsedSmsTransaction(
                type = type,
                amount = amount,
                accountLast4 = accountLast4,
                payee = payee,
                merchant = payee,
                balance = balance,
                rawSms = sms,
                bank = bank
            )
        } catch (e: Exception) {
            // Log: Error parsing SMS
            null
        }
    }

    private fun isTransactionSms(sms: String): Boolean {
        val transactionKeywords = Regex(
            """debited|credited|debit|credit|withdrawn|deposit|payment|transaction|transferred|spent|received|paid""",
            RegexOption.IGNORE_CASE
        )
        val amountPresent = AMOUNT_PATTERNS.any { it.containsMatchIn(sms) }
        return transactionKeywords.containsMatchIn(sms) && amountPresent
    }

    private fun detectType(sms: String): SmsTransactionType {
        val hasDebit = DEBIT_KEYWORDS.containsMatchIn(sms)
        val hasCredit = CREDIT_KEYWORDS.containsMatchIn(sms)
        return when {
            hasDebit && !hasCredit -> SmsTransactionType.DEBIT
            hasCredit && !hasDebit -> SmsTransactionType.CREDIT
            hasDebit -> SmsTransactionType.DEBIT // Prefer debit when both present
            else -> SmsTransactionType.UNKNOWN
        }
    }

    private fun extractAmount(sms: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(sms) ?: continue
            val raw = match.groupValues.drop(1).firstOrNull { it.isNotEmpty() } ?: continue
            return raw.replace(",", "").toDoubleOrNull()
        }
        return null
    }

    private fun extractAccountLast4(sms: String): String? {
        val match = ACCOUNT_PATTERN.find(sms) ?: return null
        return match.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
    }

    private fun extractMerchant(sms: String): String? {
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(sms) ?: continue
            val merchant = match.groupValues.getOrNull(1)?.trim() ?: continue
            if (merchant.length > 2) return cleanMerchant(merchant)
        }
        return null
    }

    private fun cleanMerchant(raw: String): String {
        return raw
            .replace(Regex("""(?i)\s*(on|from|to|at|for|via|using)\s*$"""), "")
            .trim()
            .split("\\s+".toRegex())
            .take(5) // Max 5 words
            .joinToString(" ")
    }

    private fun extractBalance(sms: String): Double? {
        val match = BALANCE_PATTERN.find(sms) ?: return null
        return match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun detectBank(sms: String, sender: String): String? {
        val combined = "$sender $sms"
        return BANK_PATTERNS.entries.firstOrNull { (_, pattern) ->
            pattern.containsMatchIn(combined)
        }?.key
    }

    /**
     * Sample regex-validated SMS patterns for testing:
     *
     * SBI:
     *   "INR 2,500.00 debited from A/c No. XX1234 on 18-02-26. Info: UPI/SWIGGY. Avail Bal: INR 15,000.00."
     *
     * HDFC:
     *   "Rs.1,200.00 debited from your HDFC Bank A/c **5678 for purchase at AMAZON. Available Bal: Rs.8,000.00"
     *
     * ICICI:
     *   "ICICI Bank Acct XX4321 debited for Rs 500 on 18-Feb-26. Info: UPI-PhonePe/ZOMATO"
     *
     * Paytm:
     *   "You have paid Rs.350.00 to Dominos Pizza via Paytm on 18-Feb-26 13:45:00. UPI Ref: 123456789"
     *
     * GPay/UPI:
     *   "Rs. 150 sent via Google Pay to PhonePe on 18 Feb 2026. UPI ID: merchant@ok"
     */
}

// ─── Batch SMS Reader ──────────────────────────────────────────────────────────
object SmsReader {
    fun readRecentTransactionSms(
        context: android.content.Context,
        limit: Int = 100
    ): List<ParsedSmsTransaction> {
        val results = mutableListOf<ParsedSmsTransaction>()
        val uri = android.net.Uri.parse("content://sms/inbox")
        val projection = arrayOf("address", "body", "date")

        try {
            context.contentResolver.query(
                uri, projection, null, null, "date DESC LIMIT $limit"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val sender = cursor.getString(0) ?: ""
                    val body = cursor.getString(1) ?: ""
                    SmsParser.parse(body, sender)?.let { results.add(it) }
                }
            }
        } catch (e: Exception) {
            // Log: Error reading SMS
        }

        return results
    }
}
