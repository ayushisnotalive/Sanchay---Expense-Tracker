package com.camuel.sanchay.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = ExpenseDatabase.getDatabase(applicationContext)
        val dao = database.expenseDao()
        val subscriptions = dao.getAllSubscriptions().first()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        subscriptions.forEach { sub ->
            // Logic: If due date is today and hasn't been posted yet
            if (sub.nextDueDate == today && sub.lastAutoPostDate != today) {
                // Auto-post expense
                dao.insert(
                    Expense(
                        amount = sub.amount.toString(),
                        category = sub.category,
                        date = today,
                        paymentMethod = "Auto-Debit",
                        note = "Auto-posted subscription: ${sub.name}",
                        walletId = sub.walletId
                    )
                )
                // Update subscription to mark as posted
                dao.insertSubscription(sub.copy(lastAutoPostDate = today))
                
                showNotification(sub.name, "Successfully auto-posted subscription payment: ${sub.amount}")
            } else if (sub.nextDueDate == today) {
                // Already posted today, just optional notification or skip
            } else {
                // Check if due tomorrow for reminder
                // (Omitted for brevity, but could be added here)
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bill_reminders"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Bill Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
