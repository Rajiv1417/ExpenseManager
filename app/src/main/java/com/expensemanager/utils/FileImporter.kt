package com.expensemanager.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.opencsv.CSVReaderBuilder
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.io.InputStreamReader

data class RawTransaction(
    val row: Map<String, String>
)

data class ColumnMapping(
    val amount: String,
    val date: String,
    val type: String?,
    val category: String?,
    val description: String?,
    val account: String?
)

sealed class ImportResult {
    data class Success(val transactions: List<RawTransaction>, val headers: List<String>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

object CsvImporter {
    fun parse(inputStream: InputStream): ImportResult {
        return try {
            val reader = CSVReaderBuilder(InputStreamReader(inputStream)).build()
            val headers = reader.readNext()?.toList() ?: return ImportResult.Error("Empty file")

            val rows = mutableListOf<RawTransaction>()
            var line = reader.readNext()
            while (line != null) {
                if (line.isNotEmpty() && line.any { it.isNotBlank() }) {
                    val row = headers.zip(line.toList()).toMap()
                    rows.add(RawTransaction(row))
                }
                line = reader.readNext()
            }
            reader.close()
            ImportResult.Success(rows, headers)
        } catch (e: Exception) {
            ImportResult.Error("CSV parsing failed: ${e.message}")
        }
    }
}

object ExcelImporter {
    fun parse(inputStream: InputStream): ImportResult {
        return try {
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(0) ?: return ImportResult.Error("Empty sheet")

            val headers = (0 until headerRow.lastCellNum).map { col ->
                headerRow.getCell(col)?.toString()?.trim() ?: "Column$col"
            }

            val rows = mutableListOf<RawTransaction>()
            for (rowIdx in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIdx) ?: continue
                val map = mutableMapOf<String, String>()
                headers.forEachIndexed { col, header ->
                    map[header] = row.getCell(col)?.toString()?.trim() ?: ""
                }
                if (map.values.any { it.isNotBlank() }) {
                    rows.add(RawTransaction(map))
                }
            }
            workbook.close()
            ImportResult.Success(rows, headers)
        } catch (e: Exception) {
            ImportResult.Error("Excel parsing failed: ${e.message}")
        }
    }
}

object PdfImporter {
    /**
     * PDF bank statements are parsed by extracting text via iText
     * and then running the SMS-style regex over each line.
     */
    fun parse(inputStream: InputStream): ImportResult {
        return try {
            val reader = com.itextpdf.kernel.pdf.PdfReader(inputStream)
            val doc = com.itextpdf.kernel.pdf.PdfDocument(reader)
            val sb = StringBuilder()

            for (i in 1..doc.numberOfPages) {
                val text = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(doc.getPage(i))
                sb.append(text).append("\n")
            }
            doc.close()

            val lines = sb.toString().split("\n").filter { it.isNotBlank() }
            val rows = lines.mapNotNull { line ->
                val parsed = SmsParser.parse(line)
                if (parsed != null) {
                    RawTransaction(
                        mapOf(
                            "amount" to parsed.amount.toString(),
                            "type" to parsed.type.name,
                            "description" to (parsed.payee ?: ""),
                            "raw" to line
                        )
                    )
                } else null
            }

            if (rows.isEmpty()) {
                ImportResult.Error("No transactions found in PDF")
            } else {
                ImportResult.Success(rows, listOf("amount", "type", "description", "raw"))
            }
        } catch (e: Exception) {
            ImportResult.Error("PDF parsing failed: ${e.message}")
        }
    }
}

object FileImporter {
    fun parseUri(context: Context, uri: Uri): ImportResult {
        val contentType = context.contentResolver.getType(uri)
        val stream = context.contentResolver.openInputStream(uri)
            ?: return ImportResult.Error("Cannot open file")

        return when {
            contentType?.contains("csv") == true ||
            uri.lastPathSegment?.endsWith(".csv") == true -> CsvImporter.parse(stream)

            contentType?.contains("spreadsheet") == true ||
            uri.lastPathSegment?.endsWith(".xlsx") == true ||
            uri.lastPathSegment?.endsWith(".xls") == true -> ExcelImporter.parse(stream)

            contentType?.contains("pdf") == true ||
            uri.lastPathSegment?.endsWith(".pdf") == true -> PdfImporter.parse(stream)

            else -> ImportResult.Error("Unsupported file type: $contentType")
        }
    }
}
