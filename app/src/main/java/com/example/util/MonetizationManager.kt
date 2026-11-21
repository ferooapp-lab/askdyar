package com.example.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.model.BillingProductKind
import com.example.data.model.MonetizationCatalog
import com.example.data.model.StoreProduct
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobIds {
    // Official Google AdMob Sample Unit IDs (replace with your Play Console AdMob IDs before upload)
    const val BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
}

class AdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialShownAt: Long = 0L

    fun preloadAds() {
        loadInterstitial()
        loadRewarded()
    }

    fun loadInterstitial() {
        try {
            val request = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AdMobIds.INTERSTITIAL_UNIT_ID,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        } catch (_: Throwable) {
            interstitialAd = null
        }
    }

    fun loadRewarded() {
        try {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                AdMobIds.REWARDED_UNIT_ID,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        } catch (_: Throwable) {
            rewardedAd = null
        }
    }

    fun showInterstitialIfEligible(
        activity: Activity?,
        shouldShowAds: Boolean,
        cooldownSeconds: Int
    ): Boolean {
        if (!shouldShowAds || activity == null) return false
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownAt < cooldownSeconds * 1000L) return false
        val ad = interstitialAd ?: run {
            loadInterstitial()
            return false
        }
        return try {
            lastInterstitialShownAt = now
            ad.show(activity)
            interstitialAd = null
            loadInterstitial()
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onOfflineFallback: () -> Unit
    ) {
        val ad = rewardedAd
        if (activity != null && ad != null) {
            try {
                ad.show(activity) {
                    onRewardEarned()
                }
                rewardedAd = null
                loadRewarded()
                return
            } catch (_: Throwable) {
                rewardedAd = null
            }
        }
        loadRewarded()
        onOfflineFallback()
    }
}

class PlayBillingController(
    context: Context,
    private val onProductPurchased: (StoreProduct, String) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onSyncExistingEntitlements: (List<StoreProduct>) -> Unit = {},
    private val onBillingStatusMessage: (String) -> Unit = {}
) : PurchasesUpdatedListener {

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private val _liveFormattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val liveFormattedPrices: StateFlow<Map<String, String>> = _liveFormattedPrices.asStateFlow()

    private var billingClient: BillingClient? = null

    init {
        try {
            billingClient = BillingClient.newBuilder(context.applicationContext)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .build()
        } catch (_: Throwable) {
            billingClient = null
        }
    }

    fun startConnection() {
        val client = billingClient ?: return
        if (client.isReady) {
            onConnectionChanged(true)
            queryAllProducts()
            queryActivePurchases()
            return
        }
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    val ready = result.responseCode == BillingClient.BillingResponseCode.OK
                    onConnectionChanged(ready)
                    if (ready) {
                        queryAllProducts()
                        queryActivePurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    onConnectionChanged(false)
                }
            })
        } catch (_: Throwable) {
            onConnectionChanged(false)
        }
    }

    private fun queryAllProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val subsProducts = MonetizationCatalog.vipSubscriptions.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.skuId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        if (subsProducts.isNotEmpty()) {
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(subsProducts).build()
            ) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val updatedPrices = _liveFormattedPrices.value.toMutableMap()
                    details.forEach { pd ->
                        productDetailsMap[pd.productId] = pd
                        val formatted = pd.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.lastOrNull()
                            ?.formattedPrice
                        if (!formatted.isNullOrBlank()) {
                            updatedPrices[pd.productId] = formatted
                        }
                    }
                    _liveFormattedPrices.value = updatedPrices
                }
            }
        }

        val inAppProducts = (MonetizationCatalog.oneTimePacks + MonetizationCatalog.coinPacks).map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.skuId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        if (inAppProducts.isNotEmpty()) {
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(inAppProducts).build()
            ) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val updatedPrices = _liveFormattedPrices.value.toMutableMap()
                    details.forEach { pd ->
                        productDetailsMap[pd.productId] = pd
                        val formatted = pd.oneTimePurchaseOfferDetails?.formattedPrice
                        if (!formatted.isNullOrBlank()) {
                            updatedPrices[pd.productId] = formatted
                        }
                    }
                    _liveFormattedPrices.value = updatedPrices
                }
            }
        }
    }

    fun queryActivePurchases(onFinished: ((Int) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onFinished?.invoke(0)
            return
        }
        val restored = mutableListOf<StoreProduct>()
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { subsResult, subsPurchases ->
            if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                subsPurchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
                    acknowledgeIfNeeded(client, purchase)
                    purchase.products.forEach { sku ->
                        MonetizationCatalog.allProducts.find { it.skuId == sku }?.let { restored.add(it) }
                    }
                }
            }
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
            ) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    inAppPurchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
                        acknowledgeIfNeeded(client, purchase)
                        purchase.products.forEach { sku ->
                            MonetizationCatalog.allProducts.find { it.skuId == sku }?.let { product ->
                                if (product.kind == BillingProductKind.NON_CONSUMABLE) {
                                    restored.add(product)
                                }
                            }
                        }
                    }
                }
                if (restored.isNotEmpty()) {
                    onSyncExistingEntitlements(restored)
                }
                onFinished?.invoke(restored.size)
            }
        }
    }

    private fun acknowledgeIfNeeded(client: BillingClient, purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.acknowledgePurchase(ackParams) { _ -> }
        }
    }

    fun launchPurchaseFlow(activity: Activity?, product: StoreProduct): Boolean {
        val client = billingClient ?: return false
        if (activity == null || !client.isReady) return false
        val details = productDetailsMap[product.skuId] ?: return false

        val pdParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (product.kind == BillingProductKind.SUBSCRIPTION) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (!offerToken.isNullOrBlank()) {
                pdParamsBuilder.setOfferToken(offerToken)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(pdParamsBuilder.build()))
            .build()

        val launchResult = client.launchBillingFlow(activity, flowParams)
        return launchResult.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val client = billingClient ?: return
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        purchase.products.forEach { sku ->
                            val matched = MonetizationCatalog.allProducts.find { it.skuId == sku }
                            if (matched != null) {
                                if (matched.kind == BillingProductKind.CONSUMABLE_COINS) {
                                    val consumeParams = ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                    client.consumeAsync(consumeParams) { _, _ -> }
                                } else {
                                    acknowledgeIfNeeded(client, purchase)
                                }
                                onProductPurchased(matched, "Play Store")
                            }
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        onBillingStatusMessage("Ödeme onayı bekleniyor, tamamlandığında otomatik tanımlanacaktır.")
                    }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryActivePurchases()
                onBillingStatusMessage("Bu paket hesabınızda zaten aktif, ayrıcalıklarınız geri yüklendi.")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onBillingStatusMessage("Satın alma işlemi iptal edildi.")
            }
        }
    }

    fun endConnection() {
        try {
            billingClient?.endConnection()
        } catch (_: Throwable) {
        }
    }
}
