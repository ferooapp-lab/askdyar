package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val reminderId = intent?.getLongExtra("reminder_id", 0L) ?: 0L
        val title = intent?.getStringExtra("reminder_title") ?: "Romantik Hatırlatıcı"
        val note = intent?.getStringExtra("reminder_note") ?: ""

        NotificationHelper.createNotificationChannels(context)
        NotificationHelper.showReminderNotification(context, reminderId, title, note)
    }
}
