package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.CuratedQuotes

class LoveQuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_NEXT_QUOTE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, LoveQuoteWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId, forceNext = true)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_NEXT_QUOTE = "com.gamferask.ACTION_WIDGET_NEXT_QUOTE"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            forceNext: Boolean = false
        ) {
            val prefs = context.getSharedPreferences("widget_love_prefs", Context.MODE_PRIVATE)
            val quotes = CuratedQuotes.quotes
            if (quotes.isEmpty()) return

            var currentIndex = prefs.getInt("last_widget_quote_index", 0)
            if (forceNext) {
                currentIndex = (currentIndex + 1) % quotes.size
                prefs.edit().putInt("last_widget_quote_index", currentIndex).apply()
            }
            val quote = quotes[currentIndex % quotes.size]

            val views = RemoteViews(context.packageName, R.layout.widget_love_quote)
            views.setTextViewText(R.id.widget_quote_text, "\"${quote.text}\"")
            views.setTextViewText(R.id.widget_author_text, "— ${quote.author}")

            // Tap root view to open the app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Tap refresh button to fetch next quote
            val refreshIntent = Intent(context, LoveQuoteWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_NEXT_QUOTE
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
