package com.camuel.sanchay.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.camuel.sanchay.ExpenseDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SmartNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = ExpenseDatabase.getDatabase(applicationContext)
            val dao = database.expenseDao()
            val prefs = applicationContext.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)

            val budget = prefs.getFloat("monthly_budget", 1000f)
            val currency = prefs.getString("currency", "$") ?: "$"

            // Filter expenses for current month
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val expenses = dao.getAllExpenses().first()
            val thisMonthExpenses = expenses.filter { it.date in monthStart..today }
            val totalSpent = thisMonthExpenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

            if (budget > 0) {
                val ratio = totalSpent / budget
                if (ratio >= 1.0) {
                    showNotification(
                        title = "⚠️ Monthly Budget Exceeded",
                        message = "You have spent $currency${String.format(Locale.US, "%.2f", totalSpent)} of your $currency${budget.toInt()} budget (${String.format(Locale.US, "%.0f", ratio * 100)}%)."
                    )
                } else if (ratio >= 0.8) {
                    showNotification(
                        title = "📊 Budget Alert: 80% Reached",
                        message = "You have spent $currency${String.format(Locale.US, "%.2f", totalSpent)} (${String.format(Locale.US, "%.0f", ratio * 100)}% of your monthly budget)."
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "budget_alerts"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Budget & Spending Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifies when monthly budget thresholds are reached"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2001, notification)
    }
}
