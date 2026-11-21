package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.ui.MainScreen
import com.example.util.NotificationHelper
import com.example.util.configureAppCheck
import com.google.android.gms.ads.MobileAds

data class DailyQuoteDeepLink(
    val quoteId: String,
    val text: String,
    val author: String,
    val sectionId: String
)

class MainActivity : ComponentActivity() {
    private val pendingDeepLinkState = mutableStateOf<DailyQuoteDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with launch intent before any UI or Firebase operation
        configureAppCheck(this, intent)

        try {
            MobileAds.initialize(this) {}
        } catch (_: Throwable) {
        }

        parseDailyQuoteDeepLink(intent)

        enableEdgeToEdge()
        setContent {
            MainScreen(
                deepLinkQuote = pendingDeepLinkState.value,
                onDeepLinkConsumed = { pendingDeepLinkState.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseDailyQuoteDeepLink(intent)
    }

    private fun parseDailyQuoteDeepLink(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_DAILY_QUOTE, false) == true) {
            val quoteId = intent.getStringExtra(NotificationHelper.EXTRA_QUOTE_ID).orEmpty()
            val text = intent.getStringExtra(NotificationHelper.EXTRA_QUOTE_TEXT).orEmpty()
            val author = intent.getStringExtra(NotificationHelper.EXTRA_QUOTE_AUTHOR).orEmpty()
            val sectionId = intent.getStringExtra(NotificationHelper.EXTRA_QUOTE_SECTION).orEmpty()
            pendingDeepLinkState.value = DailyQuoteDeepLink(
                quoteId = quoteId,
                text = text,
                author = author,
                sectionId = sectionId
            )
            intent.removeExtra(NotificationHelper.EXTRA_OPEN_DAILY_QUOTE)
        }
    }
}
