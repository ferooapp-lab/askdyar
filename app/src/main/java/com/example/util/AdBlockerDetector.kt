package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class AdBlockerCheckResult(
    val isOnline: Boolean = false,
    val adBlockerDetected: Boolean = false,
    val signalReason: String = ""
)

object AdBlockerEvaluator {
    private val knownAdBlockDnsKeywords = listOf(
        "adguard",
        "nextdns",
        "pihole",
        "rethinkdns",
        "controld",
        "blokada",
        "adblock",
        "ahadns",
        "cleanbrowsing",
        "mullvad",
        "dns.sb"
    )

    private val sinkholeIps = setOf(
        "0.0.0.0",
        "127.0.0.1",
        "::",
        "::1",
        "0:0:0:0:0:0:0:0",
        "0:0:0:0:0:0:0:1"
    )

    fun isAdBlockPrivateDnsHost(privateDnsServerName: String?): Boolean {
        val host = privateDnsServerName?.trim()?.lowercase().orEmpty()
        if (host.isBlank()) return false
        return knownAdBlockDnsKeywords.any { keyword -> host.contains(keyword) }
    }

    fun isSinkholeIp(hostAddress: String?): Boolean {
        val ip = hostAddress?.trim()?.lowercase().orEmpty()
        if (ip.isBlank()) return false
        if (sinkholeIps.contains(ip)) return true
        return ip.startsWith("127.") || ip == "0.0.0.0"
    }

    fun evaluateHostResolution(
        generalDnsResolved: Boolean,
        generalIp: String?,
        adHosts: List<Pair<String, String?>>
    ): AdBlockerCheckResult {
        if (!generalDnsResolved || isSinkholeIp(generalIp)) {
            // Network/DNS is offline or captive — never flag an ad-blocker falsely
            return AdBlockerCheckResult(isOnline = false, adBlockerDetected = false, signalReason = "")
        }

        val blockedHost = adHosts.firstOrNull { (_, ip) ->
            ip == null || isSinkholeIp(ip)
        }
        return if (blockedHost != null) {
            AdBlockerCheckResult(
                isOnline = true,
                adBlockerDetected = true,
                signalReason = "Reklam sunucusu (${blockedHost.first}) cihazdaki DNS veya reklam engelleyici tarafından engelleniyor."
            )
        } else {
            AdBlockerCheckResult(isOnline = true, adBlockerDetected = false, signalReason = "")
        }
    }
}

object AdBlockerDetector {
    private val adServingHosts = listOf(
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com"
    )

    suspend fun detect(context: Context): AdBlockerCheckResult = withContext(Dispatchers.IO) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return@withContext AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)

            val activeNetwork = cm.activeNetwork
                ?: return@withContext AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)
            val caps = cm.getNetworkCapabilities(activeNetwork)
                ?: return@withContext AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)

            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (!hasInternet || !isValidated) {
                return@withContext AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)
            }

            // 1. Check Android Private DNS (DNS-over-TLS) for known ad-blocking providers
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val linkProps = cm.getLinkProperties(activeNetwork)
                if (linkProps?.isPrivateDnsActive == true &&
                    AdBlockerEvaluator.isAdBlockPrivateDnsHost(linkProps.privateDnsServerName)
                ) {
                    return@withContext AdBlockerCheckResult(
                        isOnline = true,
                        adBlockerDetected = true,
                        signalReason = "Özel DNS (${linkProps.privateDnsServerName}) üzerinden reklam engelleme aktif görünüyor."
                    )
                }
            }

            // 2. Compare general DNS resolution against AdMob ad host DNS resolution
            val generalAddr = try {
                InetAddress.getByName("connectivitycheck.gstatic.com")
            } catch (_: Throwable) {
                null
            }
            val generalOk = generalAddr != null &&
                !generalAddr.isAnyLocalAddress &&
                !generalAddr.isLoopbackAddress

            if (!generalOk) {
                return@withContext AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)
            }

            val resolvedAdHosts = adServingHosts.map { host ->
                val ip = try {
                    val addr = InetAddress.getByName(host)
                    if (addr.isAnyLocalAddress || addr.isLoopbackAddress) {
                        "0.0.0.0"
                    } else {
                        addr.hostAddress
                    }
                } catch (_: Throwable) {
                    null
                }
                host to ip
            }

            AdBlockerEvaluator.evaluateHostResolution(
                generalDnsResolved = true,
                generalIp = generalAddr?.hostAddress,
                adHosts = resolvedAdHosts
            )
        } catch (_: Throwable) {
            AdBlockerCheckResult(isOnline = false, adBlockerDetected = false)
        }
    }
}
