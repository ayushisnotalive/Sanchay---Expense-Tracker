package com.camuel.sanchay.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.camuel.sanchay.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SanchayBackupPayload(
    val version: Int = 1,
    val exportDate: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val investments: List<Investment> = emptyList()
)

object BackupManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createFullBackup(context: Context, database: ExpenseDatabase): String = withContext(Dispatchers.IO) {
        val dao = database.expenseDao()
        val payload = SanchayBackupPayload(
            expenses = dao.getAllExpenses().first(),
            categories = dao.getAllCategories().first(),
            wallets = dao.getAllWallets().first(),
            subscriptions = dao.getAllSubscriptions().first(),
            savingsGoals = dao.getAllSavingsGoals().first(),
            debts = dao.getAllDebts().first(),
            investments = dao.getAllInvestments().first()
        )

        val jsonString = gson.toJson(payload)
        val fileName = "sanchay_backup_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonString)

        shareBackupFile(context, file)
        file.absolutePath
    }

    private fun shareBackupFile(context: Context, file: File) {
        ReportGenerator.saveToDownloads(context, file, "application/json")

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            clipData = android.content.ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Export Sanchay Backup").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    suspend fun restoreFullBackup(context: Context, uri: Uri, database: ExpenseDatabase): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val payload = gson.fromJson(reader, SanchayBackupPayload::class.java)
                        ?: throw IllegalArgumentException("Invalid backup file format")

                    val dao = database.expenseDao()
                    var count = 0

                    payload.wallets.forEach { wallet ->
                        dao.insertWallet(wallet)
                        count++
                    }

                    payload.categories.forEach { category ->
                        dao.insertCategory(category)
                        count++
                    }

                    payload.expenses.forEach { expense ->
                        dao.insert(expense)
                        count++
                    }

                    payload.subscriptions.forEach { subscription ->
                        dao.insertSubscription(subscription)
                        count++
                    }

                    payload.savingsGoals.forEach { goal ->
                        dao.insertSavingsGoal(goal)
                        count++
                    }

                    payload.debts.forEach { debt ->
                        dao.insertDebt(debt)
                        count++
                    }

                    payload.investments.forEach { investment ->
                        dao.insertInvestment(investment)
                        count++
                    }

                    count
                }
            } ?: throw IllegalStateException("Could not open input stream for backup file")
        }
    }
}
