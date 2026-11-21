package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != "android.intent.action.QUICKBOOT_POWERON") {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationHelper.createNotificationChannels(context)
                val prefs = context.getSharedPreferences("romantic_prefs", Context.MODE_PRIVATE)
                val dailyEnabled = prefs.getBoolean("daily_notif_enabled", true)
                if (dailyEnabled) {
                    val hour = prefs.getInt("daily_notif_hour", 8)
                    val minute = prefs.getInt("daily_notif_minute", 30)
                    NotificationHelper.scheduleDailyQuoteAlarm(context, hour, minute)
                }

                val now = System.currentTimeMillis()
                val rawCache = prefs.getString("alarm_schedule_cache_json", null)
                if (!rawCache.isNullOrBlank()) {
                    val arr = JSONArray(rawCache)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val targetTs = obj.optLong("targetTimestamp", 0L)
                        if (targetTs > now) {
                            NotificationHelper.scheduleReminder(
                                context = context,
                                reminderId = obj.optLong("id", i.toLong() + 1L),
                                title = obj.optString("title", "Hatırlatıcı"),
                                note = obj.optString("note", ""),
                                timestamp = targetTs
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
