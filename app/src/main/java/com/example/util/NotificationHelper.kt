package com.example.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CuratedQuotes
import com.example.receiver.DailyQuoteReceiver
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_DAILY_QUOTE = "channel_daily_love_quote"
    const val CHANNEL_REMINDERS = "channel_romantic_reminders"
    const val CHANNEL_ADMIN_BROADCAST = "channel_admin_broadcasts"

    const val EXTRA_OPEN_DAILY_QUOTE = "extra_open_daily_quote"
    const val EXTRA_QUOTE_ID = "extra_daily_quote_id"
    const val EXTRA_QUOTE_TEXT = "extra_daily_quote_text"
    const val EXTRA_QUOTE_AUTHOR = "extra_daily_quote_author"
    const val EXTRA_QUOTE_SECTION = "extra_daily_quote_section"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_QUOTE,
                "Günün Aşk Sözü",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Her gün romantik ve ilham verici aşk sözleri"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Romantik Hatırlatıcılar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yıldönümleri, özel günler ve sevgi görevleri"
                enableVibration(true)
            }

            val broadcastChannel = NotificationChannel(
                CHANNEL_ADMIN_BROADCAST,
                "Duyurular ve Özel Bildirimler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yöneticiden tüm kullanıcılara gönderilen özel duyuru ve mesajlar"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(broadcastChannel)
        }
    }

    fun showBroadcastNotification(
        context: Context,
        broadcastId: Long,
        title: String,
        message: String,
        emoji: String = "📢"
    ) {
        createNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (5000 + broadcastId).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ADMIN_BROADCAST)
            .setSmallIcon(R.drawable.ic_notif_heart)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify((5000 + broadcastId).toInt(), notification)
        } catch (_: SecurityException) {
        }
    }

    fun showDailyQuoteNotification(
        context: Context,
        quoteText: String? = null,
        author: String? = null,
        quoteId: String? = null,
        sectionId: String? = null,
        tonePreference: String = "MIXED"
    ) {
        val chosen = if (quoteText != null && author != null) {
            null
        } else {
            CuratedQuotes.pickMorningQuote(tonePreference)
        }
        val finalId = quoteId ?: chosen?.id ?: "daily_custom"
        val finalText = quoteText ?: chosen?.text ?: CuratedQuotes.quotes.first().text
        val finalAuthor = author ?: chosen?.author ?: "Aşka Dair"
        val finalSection = sectionId ?: chosen?.section?.id ?: "gunaydin"

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.gamferask.OPEN_DAILY_QUOTE_$finalId"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_DAILY_QUOTE, true)
            putExtra(EXTRA_QUOTE_ID, finalId)
            putExtra(EXTRA_QUOTE_TEXT, finalText)
            putExtra(EXTRA_QUOTE_AUTHOR, finalAuthor)
            putExtra(EXTRA_QUOTE_SECTION, finalSection)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val titlePrefix = if (tonePreference == "MOTIVATIONAL" || chosen?.subCategory?.contains("Motivasyon") == true) {
            "🌅 Sabah Motivasyon Sözü"
        } else {
            "💌 Günün Aşk Sözü"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_QUOTE)
            .setSmallIcon(R.drawable.ic_notif_heart)
            .setContentTitle("$titlePrefix ($finalAuthor)")
            .setContentText(finalText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"$finalText\"\n— $finalAuthor"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(1001, notification)
        } catch (_: SecurityException) {
        }
    }

    fun showReminderNotification(context: Context, reminderId: Long, title: String, note: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (2000 + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notif_heart)
            .setContentTitle("💖 $title")
            .setContentText(if (note.isNotBlank()) note else "Bugün özel bir sevgi anı var!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$title\n${if (note.isNotBlank()) note else "Bu özel günü sevgilinle paylaşmayı unutma!"}"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify((2000 + reminderId).toInt(), notification)
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDailyQuoteAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyQuoteReceiver::class.java).apply {
            action = "com.gamferask.ACTION_DAILY_QUOTE"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {
            // Ignore if alarm scheduling is restricted
        }
    }

    fun cancelDailyQuoteAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyQuoteReceiver::class.java).apply {
            action = "com.gamferask.ACTION_DAILY_QUOTE"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleReminder(context: Context, reminderId: Long, title: String, note: String, timestamp: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.gamferask.ACTION_REMINDER"
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_note", note)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (4000 + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (timestamp > System.currentTimeMillis()) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
                    }
                    else -> {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
                    }
                }
            }
        } catch (_: Exception) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.gamferask.ACTION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (4000 + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
