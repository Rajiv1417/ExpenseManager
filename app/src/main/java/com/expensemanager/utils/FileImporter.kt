package com.expensemanager.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.opencsv.CSVReaderBuilder
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.io.InputStreamReader

data class RawTransaction(val row: Map<String, String>)

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
                if (line.any { it.isNotBlank() }) {
                    rows.add(RawTransaction(headers.zip(line.toList()).toMap()))
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
                if (map.values.any { it.isNotBlank() }) rows.add(RawTransaction(map))
            }
            workbook.close()
            ImportResult.Success(rows, headers)
        } catch (e: Exception) {
            ImportResult.Error("Excel parsing failed: ${e.message}")
        }
    }
}

object PdfImporter {
    fun parse(inputStream: InputStream): ImportResult {
        return try {
            // iText 8: PdfReader accepts InputStream directly
            val reader = PdfReader(inputStream)
            val doc = PdfDocument(reader)
            val sb = StringBuilder()
            for (i in 1..doc.numberOfPages) {
                sb.append(PdfTextExtractor.getTextFromPage(doc.getPage(i))).append("\n")
            }
            doc.close()

            val rows = sb.toString().split("\n")
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    SmsParser.parse(line)?.let { parsed ->
                        RawTransaction(
                            mapOf(
                                "amount" to parsed.amount.toString(),
                                "type" to parsed.type.name,
                                "description" to (parsed.payee ?: ""),
                                "raw" to line
                            )
                        )
                    }
                }

            if (rows.isEmpty()) ImportResult.Error("No transactions found in PDF")
            else ImportResult.Success(rows, listOf("amount", "type", "description", "raw"))
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
        val name = uri.lastPathSegment?.lowercase() ?: ""
        return when {
            contentType?.contains("csv") == true || name.endsWith(".csv") -> CsvImporter.parse(stream)
            contentType?.contains("spreadsheet") == true || name.endsWith(".xlsx") || name.endsWith(".xls") -> ExcelImporter.parse(stream)
            contentType?.contains("pdf") == true || name.endsWith(".pdf") -> PdfImporter.parse(stream)
            else -> ImportResult.Error("Unsupported file type: $contentType")
        }
    }
}
