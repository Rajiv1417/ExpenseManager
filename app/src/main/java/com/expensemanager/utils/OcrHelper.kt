package com.expensemanager.utils

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrResult(
    val amount: Double?,
    val merchant: String?,
    val date: String?,
    val rawText: String
)

object OcrHelper {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractFromUri(context: Context, uri: Uri): OcrResult {
        val image = InputImage.fromFilePath(context, uri)
        val rawText = processImage(image)
        return parseOcrText(rawText)
    }

    private suspend fun processImage(image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    private fun parseOcrText(text: String): OcrResult {
        val parsed = SmsParser.parse(text)

        // Amount extraction
        val amount = parsed?.amount ?: extractAmountFromText(text)

        // Merchant name - look for company-like patterns in first few lines
        val merchant = parsed?.merchant ?: extractMerchantFromReceipt(text)

        // Date
        val date = extractDateFromText(text)

        return OcrResult(amount = amount, merchant = merchant, date = date, rawText = text)
    }

    private fun extractAmountFromText(text: String): Double? {
        val patterns = listOf(
            Regex("""Total[:\s]+(?:Rs\.?|INR|₹)?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""(?:Grand Total|Amount Due|Net Amount)[:\s]+(?:Rs\.?|INR|₹)?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            return match.groupValues.drop(1).firstOrNull()?.replace(",", "")?.toDoubleOrNull()
        }
        return null
    }

    private fun extractMerchantFromReceipt(text: String): String? {
        val lines = text.lines()
        // Usually merchant name is in first 3 lines, longest line
        return lines.take(3)
            .filter { it.length > 3 }
            .maxByOrNull { it.length }
            ?.trim()
            ?.take(50)
    }

    private fun extractDateFromText(text: String): String? {
        val datePatterns = listOf(
            Regex("""(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})"""),
            Regex("""(\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+\d{2,4})""", RegexOption.IGNORE_CASE),
            Regex("""Date[:\s]+(\d{1,2}[-/. ]\d{1,2}[-/. ]\d{2,4})""", RegexOption.IGNORE_CASE)
        )
        for (pattern in datePatterns) {
            return pattern.find(text)?.groupValues?.get(1) ?: continue
        }
        return null
    }
}
