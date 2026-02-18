package com.expensemanager.utils

import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {

    @Test
    fun `SBI debit SMS parses correctly`() {
        val sms = "INR 2,500.00 debited from A/c No. XX1234 on 18-02-26. Info: UPI/SWIGGY. Avail Bal: INR 15,000.00."
        val result = SmsParser.parse(sms, "SBI-ALERTS")

        assertNotNull(result)
        assertEquals(SmsTransactionType.DEBIT, result!!.type)
        assertEquals(2500.0, result.amount, 0.01)
        assertEquals("1234", result.accountLast4)
        assertEquals("SBI", result.bank)
    }

    @Test
    fun `HDFC debit SMS parses correctly`() {
        val sms = "Rs.1,200.00 debited from your HDFC Bank A/c **5678 for purchase at AMAZON. Available Bal: Rs.8,000.00"
        val result = SmsParser.parse(sms, "HDFC")

        assertNotNull(result)
        assertEquals(SmsTransactionType.DEBIT, result!!.type)
        assertEquals(1200.0, result.amount, 0.01)
        assertEquals("HDFC", result.bank)
    }

    @Test
    fun `ICICI credit SMS parses correctly`() {
        val sms = "ICICI Bank: Rs 5000.00 credited to Acct XX4321 on 18-Feb-26 from SALARY"
        val result = SmsParser.parse(sms, "ICICI")

        assertNotNull(result)
        assertEquals(SmsTransactionType.CREDIT, result!!.type)
        assertEquals(5000.0, result.amount, 0.01)
        assertEquals("ICICI", result.bank)
    }

    @Test
    fun `Paytm payment SMS parses correctly`() {
        val sms = "You have paid Rs.350.00 to Dominos Pizza via Paytm on 18-Feb-26 13:45:00. UPI Ref: 123456789"
        val result = SmsParser.parse(sms, "PAYTM")

        assertNotNull(result)
        assertEquals(SmsTransactionType.DEBIT, result!!.type)
        assertEquals(350.0, result.amount, 0.01)
        assertEquals("Paytm", result.bank)
    }

    @Test
    fun `Non-transaction SMS returns null`() {
        val sms = "Your OTP for login is 123456. Do not share it with anyone."
        val result = SmsParser.parse(sms)
        assertNull(result)
    }

    @Test
    fun `Promotional SMS returns null`() {
        val sms = "Get 50% off on all products. Shop now at ourstore.com"
        val result = SmsParser.parse(sms)
        assertNull(result)
    }

    @Test
    fun `Amount with comma separators parsed correctly`() {
        val sms = "Rs.1,00,000.50 debited from your account XX1111"
        val result = SmsParser.parse(sms)
        assertNotNull(result)
        assertEquals(100000.50, result!!.amount, 0.01)
    }

    @Test
    fun `Balance extracted correctly`() {
        val sms = "INR 500.00 debited. Available balance: INR 12,500.00"
        val result = SmsParser.parse(sms)
        assertNotNull(result)
        assertEquals(12500.0, result!!.balance, 0.01)
    }

    @Test
    fun `GPay UPI transfer parses correctly`() {
        val sms = "Rs. 150 sent via Google Pay to PhonePe merchant on 18 Feb 2026"
        val result = SmsParser.parse(sms, "GPAY")
        assertNotNull(result)
        assertEquals(SmsTransactionType.DEBIT, result!!.type)
        assertEquals(150.0, result.amount, 0.01)
        assertEquals("GPay", result.bank)
    }
}
