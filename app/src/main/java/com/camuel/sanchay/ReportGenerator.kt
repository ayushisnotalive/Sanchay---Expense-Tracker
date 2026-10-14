package com.camuel.sanchay

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    fun exportToCSV(context: Context, expenses: List<Expense>): String {
        val fileName = "expenses_report_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)

        file.printWriter().use { out ->
            out.println("ID,Type,Amount,Category,Date,Payment Method,Note")
            expenses.forEach { expense ->
                out.println("${expense.id},${expense.transactionType},${expense.amount},${expense.category},${expense.date},${expense.paymentMethod},${expense.note}")
            }
        }

        shareFile(context, file, "text/csv")
        return file.absolutePath
    }

    fun exportToPDF(context: Context, expenses: List<Expense>): String {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842 // A4 Size at 72 dpi
        val timeStamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val totalIncome = expenses.filter { it.transactionType == "INCOME" }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val totalExpense = expenses.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val netSavings = totalIncome - totalExpense

        val rowsPerPage = 22
        val totalPages = if (expenses.isEmpty()) 1 else ((expenses.size - 1) / rowsPerPage) + 1

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
        }
        val boldPaint = Paint().apply {
            isAntiAlias = true
            isFakeBoldText = true
            color = Color.rgb(15, 23, 42)
        }
        val bgPaint = Paint().apply {
            isAntiAlias = true
        }

        for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 45f

            // Top Header Bar
            boldPaint.textSize = 20f
            boldPaint.color = Color.rgb(16, 185, 129) // Emerald
            canvas.drawText("SANCHAY", 40f, y, boldPaint)

            textPaint.textSize = 10f
            textPaint.color = Color.rgb(100, 116, 139)
            canvas.drawText("Personal Financial Statement", 145f, y - 2f, textPaint)

            val pageStr = "Page ${pageIndex + 1} of $totalPages"
            canvas.drawText(pageStr, pageWidth - 40f - textPaint.measureText(pageStr), y, textPaint)

            y += 8f
            bgPaint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, bgPaint)
            y += 20f

            // KPI Cards only on Page 1
            if (pageIndex == 0) {
                textPaint.textSize = 10f
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("Generated on: $timeStamp | Total Transactions: ${expenses.size}", 40f, y, textPaint)
                y += 18f

                val cardWidth = (pageWidth - 80f - 24f) / 3f
                val cardHeight = 60f

                // Income Card
                bgPaint.color = Color.rgb(240, 253, 244)
                canvas.drawRoundRect(RectF(40f, y, 40f + cardWidth, y + cardHeight), 8f, 8f, bgPaint)
                textPaint.color = Color.rgb(22, 101, 52)
                textPaint.textSize = 9f
                canvas.drawText("TOTAL INFLOW", 50f, y + 20f, textPaint)
                boldPaint.textSize = 14f
                boldPaint.color = Color.rgb(22, 101, 52)
                canvas.drawText("+ ₹${String.format(Locale.US, "%,.2f", totalIncome)}", 50f, y + 44f, boldPaint)

                // Expense Card
                val expX = 40f + cardWidth + 12f
                bgPaint.color = Color.rgb(255, 241, 242)
                canvas.drawRoundRect(RectF(expX, y, expX + cardWidth, y + cardHeight), 8f, 8f, bgPaint)
                textPaint.color = Color.rgb(159, 18, 57)
                textPaint.textSize = 9f
                canvas.drawText("TOTAL OUTFLOW", expX + 10f, y + 20f, textPaint)
                boldPaint.textSize = 14f
                boldPaint.color = Color.rgb(225, 29, 72)
                canvas.drawText("- ₹${String.format(Locale.US, "%,.2f", totalExpense)}", expX + 10f, y + 44f, boldPaint)

                // Net Savings Card
                val netX = expX + cardWidth + 12f
                val netColorBg = if (netSavings >= 0) Color.rgb(238, 242, 255) else Color.rgb(254, 242, 242)
                val netColorText = if (netSavings >= 0) Color.rgb(67, 56, 202) else Color.rgb(185, 28, 28)
                bgPaint.color = netColorBg
                canvas.drawRoundRect(RectF(netX, y, netX + cardWidth, y + cardHeight), 8f, 8f, bgPaint)
                textPaint.color = netColorText
                textPaint.textSize = 9f
                canvas.drawText("NET CASHFLOW", netX + 10f, y + 20f, textPaint)
                boldPaint.textSize = 14f
                boldPaint.color = netColorText
                val sign = if (netSavings >= 0) "+" else ""
                canvas.drawText("$sign₹${String.format(Locale.US, "%,.2f", netSavings)}", netX + 10f, y + 44f, boldPaint)

                y += cardHeight + 24f
            }

            // Table Header
            bgPaint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(RectF(40f, y, (pageWidth - 40).toFloat(), y + 26f), 4f, 4f, bgPaint)

            boldPaint.textSize = 10f
            boldPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("DATE", 48f, y + 17f, boldPaint)
            canvas.drawText("TYPE", 130f, y + 17f, boldPaint)
            canvas.drawText("CATEGORY", 200f, y + 17f, boldPaint)
            canvas.drawText("DESCRIPTION / NOTE", 310f, y + 17f, boldPaint)
            val amtHeader = "AMOUNT"
            canvas.drawText(amtHeader, pageWidth - 48f - boldPaint.measureText(amtHeader), y + 17f, boldPaint)

            y += 32f

            // Table Rows
            val startIndex = pageIndex * rowsPerPage
            val endIndex = minOf(startIndex + rowsPerPage, expenses.size)

            for (i in startIndex until endIndex) {
                val expense = expenses[i]
                val isEven = (i % 2 == 0)

                if (isEven) {
                    bgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(RectF(40f, y - 10f, (pageWidth - 40).toFloat(), y + 14f), bgPaint)
                }

                textPaint.textSize = 9.5f
                textPaint.color = Color.rgb(51, 65, 85)
                canvas.drawText(expense.date, 48f, y + 4f, textPaint)

                // Type Badge
                when (expense.transactionType) {
                    "INCOME" -> {
                        textPaint.color = Color.rgb(16, 185, 129)
                        canvas.drawText("INCOME", 130f, y + 4f, textPaint)
                    }
                    "TRANSFER" -> {
                        textPaint.color = Color.rgb(99, 102, 241)
                        canvas.drawText("TRANSFER", 130f, y + 4f, textPaint)
                    }
                    else -> {
                        textPaint.color = Color.rgb(244, 63, 94)
                        canvas.drawText("EXPENSE", 130f, y + 4f, textPaint)
                    }
                }

                textPaint.color = Color.rgb(51, 65, 85)
                val catText = if (expense.category.length > 18) expense.category.take(16) + ".." else expense.category
                canvas.drawText(catText, 200f, y + 4f, textPaint)

                val noteText = if (expense.note.isNotBlank()) {
                    if (expense.note.length > 25) expense.note.take(23) + ".." else expense.note
                } else "-"
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText(noteText, 310f, y + 4f, textPaint)

                // Amount
                val amtValue = expense.amount.toDoubleOrNull() ?: 0.0
                boldPaint.textSize = 10f
                when (expense.transactionType) {
                    "INCOME" -> {
                        boldPaint.color = Color.rgb(16, 185, 129)
                        val text = "+ ₹${String.format(Locale.US, "%,.2f", amtValue)}"
                        canvas.drawText(text, pageWidth - 48f - boldPaint.measureText(text), y + 4f, boldPaint)
                    }
                    "TRANSFER" -> {
                        boldPaint.color = Color.rgb(99, 102, 241)
                        val text = "⇄ ₹${String.format(Locale.US, "%,.2f", amtValue)}"
                        canvas.drawText(text, pageWidth - 48f - boldPaint.measureText(text), y + 4f, boldPaint)
                    }
                    else -> {
                        boldPaint.color = Color.rgb(225, 29, 72)
                        val text = "- ₹${String.format(Locale.US, "%,.2f", amtValue)}"
                        canvas.drawText(text, pageWidth - 48f - boldPaint.measureText(text), y + 4f, boldPaint)
                    }
                }

                y += 24f
            }

            // Footer
            textPaint.textSize = 8f
            textPaint.color = Color.rgb(148, 163, 184)
            val footerText = "Generated locally by Sanchay Personal Finance • Private, Offline, and Zero-Data Collection"
            canvas.drawText(footerText, (pageWidth - textPaint.measureText(footerText)) / 2f, pageHeight - 25f, textPaint)

            pdfDocument.finishPage(page)
        }

        val fileName = "sanchay_statement_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        shareFile(context, file, "application/pdf")
        return file.absolutePath
    }

    fun exportToExcel(context: Context, expenses: List<Expense>): String {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Expenses")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ID")
        header.createCell(1).setCellValue("Type")
        header.createCell(2).setCellValue("Amount")
        header.createCell(3).setCellValue("Category")
        header.createCell(4).setCellValue("Date")
        header.createCell(5).setCellValue("Payment Method")
        header.createCell(6).setCellValue("Note")

        expenses.forEachIndexed { index, expense ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(expense.id.toDouble())
            row.createCell(1).setCellValue(expense.transactionType)
            row.createCell(2).setCellValue(expense.amount)
            row.createCell(3).setCellValue(expense.category)
            row.createCell(4).setCellValue(expense.date)
            row.createCell(5).setCellValue(expense.paymentMethod)
            row.createCell(6).setCellValue(expense.note)
        }

        val fileName = "expenses_${System.currentTimeMillis()}.xlsx"
        val file = File(context.cacheDir, fileName)
        val out = FileOutputStream(file)
        workbook.write(out)
        out.close()
        workbook.close()

        shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        return file.absolutePath
    }

    fun saveToDownloads(context: Context, file: File, mimeType: String): Uri? {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Sanchay")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val uri = resolver.insert(collection, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { inp ->
                        inp.copyTo(out)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        saveToDownloads(context, file, mimeType)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val chooser = Intent.createChooser(intent, "Export ${file.name}").apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
