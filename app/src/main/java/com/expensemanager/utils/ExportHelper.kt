package com.expensemanager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.expensemanager.data.local.entities.TransactionEntity
import com.opencsv.CSVWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.format.DateTimeFormatter

object ExportHelper {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    fun exportToCsv(context: Context, transactions: List<TransactionEntity>): Uri? {
        return try {
            val file = File(context.cacheDir, "transactions_export.csv")
            val writer = CSVWriter(FileWriter(file))

            // Headers
            writer.writeNext(arrayOf("Date", "Type", "Amount", "Category", "Account", "Payee", "Notes", "Status", "Payment Type"))

            // Rows
            transactions.forEach { tx ->
                writer.writeNext(arrayOf(
                    tx.dateTime.format(dateFormatter),
                    tx.type.name,
                    tx.amount.toString(),
                    tx.categoryId.toString(),
                    tx.accountId.toString(),
                    tx.payee ?: "",
                    tx.notes ?: "",
                    tx.status.name,
                    tx.paymentType.name
                ))
            }
            writer.close()
            getUriForFile(context, file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportToExcel(context: Context, transactions: List<TransactionEntity>): Uri? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transactions")

            // Header style
            val headerStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont()
                font.bold = true
                setFont(font)
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
            }

            // Create header row
            val headers = listOf("Date", "Type", "Amount", "Category ID", "Account ID", "Payee", "Notes", "Status", "Payment Type")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, title ->
                headerRow.createCell(i).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            // Data rows
            transactions.forEachIndexed { idx, tx ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(tx.dateTime.format(dateFormatter))
                row.createCell(1).setCellValue(tx.type.name)
                row.createCell(2).setCellValue(tx.amount)
                row.createCell(3).setCellValue(tx.categoryId.toDouble())
                row.createCell(4).setCellValue(tx.accountId.toDouble())
                row.createCell(5).setCellValue(tx.payee ?: "")
                row.createCell(6).setCellValue(tx.notes ?: "")
                row.createCell(7).setCellValue(tx.status.name)
                row.createCell(8).setCellValue(tx.paymentType.name)
            }

            headers.indices.forEach { sheet.autoSizeColumn(it) }

            val file = File(context.cacheDir, "transactions_export.xlsx")
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            getUriForFile(context, file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportToPdf(context: Context, transactions: List<TransactionEntity>): Uri? {
        return try {
            val file = File(context.cacheDir, "transactions_export.pdf")

            val writer = com.itextpdf.kernel.pdf.PdfWriter(file)
            val pdf = com.itextpdf.kernel.pdf.PdfDocument(writer)
            val document = com.itextpdf.layout.Document(pdf)

            // Title
            document.add(
                com.itextpdf.layout.element.Paragraph("Transaction Export")
                    .setFontSize(18f)
                    .setBold()
            )

            // Table
            val table = com.itextpdf.layout.element.Table(5)
            listOf("Date", "Type", "Amount", "Payee", "Status").forEach { header ->
                table.addHeaderCell(
                    com.itextpdf.layout.element.Cell()
                        .add(com.itextpdf.layout.element.Paragraph(header).setBold())
                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                )
            }

            transactions.forEach { tx ->
                table.addCell(tx.dateTime.format(dateFormatter))
                table.addCell(tx.type.name)
                table.addCell("₹${tx.amount}")
                table.addCell(tx.payee ?: "-")
                table.addCell(tx.status.name)
            }

            document.add(table)
            document.close()
            getUriForFile(context, file)
        } catch (e: Exception) {
            null
        }
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Transactions"))
    }
}
