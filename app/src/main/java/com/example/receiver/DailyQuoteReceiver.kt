package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class DailyQuoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.createNotificationChannels(context)
        val prefs = context.getSharedPreferences("romantic_prefs", Context.MODE_PRIVATE)
        val tone = prefs.getString("daily_notif_tone", "MIXED") ?: "MIXED"
        NotificationHelper.showDailyQuoteNotification(
            context = context,
            tonePreference = tone
        )
    }
}
