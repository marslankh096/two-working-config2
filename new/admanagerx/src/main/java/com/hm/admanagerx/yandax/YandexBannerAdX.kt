//package com.hm.admanagerx.old.yandax
//
//import android.content.Context
//import android.util.Log
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import androidx.core.view.isVisible
//import androidx.lifecycle.MutableLiveData
//import com.google.android.gms.ads.AdSize
//import com.hm.admanagerx.old.AdConfig
//import com.hm.admanagerx.old.AdConfigManager
//import com.hm.admanagerx.old.adCheckShow
//import com.hm.admanagerx.old.adsanalysis.AdAnalyticsTracker
//import com.hm.admanagerx.old.getAdId
//import com.hm.admanagerx.old.getYandexInlineAdSize
//import com.hm.admanagerx.old.getYandexStickyAdSize
//import com.hm.admanagerx.old.printIt
//import com.hm.admanagerx.old.sendLogs
//import com.yandex.mobile.ads.banner.BannerAdEventListener
//import com.yandex.mobile.ads.banner.BannerAdSize
//import com.yandex.mobile.ads.banner.BannerAdView
//import com.yandex.mobile.ads.common.AdRequest
//import com.yandex.mobile.ads.common.AdRequestError
//import com.yandex.mobile.ads.common.ImpressionData
//
//class YandexBannerAdX(private val context: Context) {
//
//    private var TAG = "YandexBannerAdsLoaderX"
//
//    private lateinit var mAdConfigManager: AdConfigManager
//    private lateinit var adConfig: AdConfig
//
//    var adView: BannerAdView? = null
//
//    var isAdLoaded: Boolean = false
//    var isAdImpression: Boolean = false
//
//    var onAdLoaded: MutableLiveData<Any>? = null
//    var onAdFailed: MutableLiveData<String>? = null
//    var onAdClicked: MutableLiveData<Unit>? = null
//    var onAdImpression: MutableLiveData<Unit>? = null
//    var onAdRequestDenied: MutableLiveData<Unit>? = null
//    private var isAdLoading: Boolean = false
//    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(mAdConfigManager.name) }
//    fun getAdConfigManager() = mAdConfigManager
//    fun loadYandexBannerAdX(
//        adConfigManager: AdConfigManager,
//        adSize: BannerAdSize?,
//        onAdLoaded: MutableLiveData<Any>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//    ) {
//
//        this.mAdConfigManager = adConfigManager
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//
//        this.onAdLoaded = onAdLoaded
//        this.onAdFailed = onAdFailed
//        this.onAdClicked = onAdClicked
//        this.onAdImpression = onAdImpression
//        this.onAdRequestDenied = onAdRequestDenied
//
//        if (context.adCheckShow(adConfig) || isAdLoading()) {
//            onAdRequestDenied?.value = Unit
//            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(
//                TAG
//            )
//            return
//        }
//
//        //if ad already loaded and not impression then trigger onAdLoaded for show native ad and return
//        if (isAdLoaded) {
//            if (isAdImpression) {
//                onAdLoaded?.value = adView
//            } else {
//                onAdLoaded?.value = adView
//                return
//            }
//        }
//
//        if (isAdLoading) {
//            return
//        }
//        isAdLoading = true
//        isAdImpression = false
//        isAdLoaded = false
//
//        "y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
//        context.sendLogs("y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//
//        adAnalyticsTracker.trackAdRequest()
//
//
//        adView = BannerAdView(context)
//        val adUnit = context.getAdId(adConfig.adIdYandex)
//        Log.d(TAG, "loadYandexBannerAdX: $adUnit")
//        if (adSize != null) {
//            adView?.setAdSize(adSize)
//        }
//        adView?.setAdUnitId(adUnit)
//        adView?.setBannerAdEventListener(object : BannerAdEventListener {
//            override fun onAdClicked() {
//                this@YandexBannerAdX.onAdClicked?.value = Unit
//                "y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(
//                    TAG
//                )
//                context.sendLogs("y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//                adAnalyticsTracker.trackAdClicked()
//            }
//
//            override fun onAdFailedToLoad(error: AdRequestError) {
//                isAdLoaded = false
//                isAdLoading = false
//                this@YandexBannerAdX.onAdFailed?.value = error.description
//                "y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_Ad failed ${error.description}".printIt(
//                    TAG
//                )
//                context.sendLogs("y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
//                adAnalyticsTracker.trackAdLoadFailed()
//            }
//
//            override fun onAdLoaded() {
//                isAdLoaded = true
//                isAdLoading = false
//
//                this@YandexBannerAdX.onAdLoaded?.value = adView
//                "y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
//                context.sendLogs("y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//                adAnalyticsTracker.trackAdLoaded()
//            }
//
//            override fun onImpression(impressionData: ImpressionData?) {
//                this@YandexBannerAdX.onAdImpression?.value = Unit
//                "y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad Impression".printIt(
//                    TAG
//                )
//                context.sendLogs("y_${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                isAdImpression = true
//                adAnalyticsTracker.trackAdImpression()
//            }
//
//            override fun onLeftApplication() {
//
//            }
//
//            override fun onReturnedToApplication() {
//
//            }
//        })
//
//        adView?.loadAd(AdRequest.Builder().build())
//    }
//
//    fun isAdLoading() = isAdLoading
//
//    fun destroyAd() = adView?.destroy()
//
//    fun showYandexBannerAd(bannerAdContainer: FrameLayout?) {
//
//        if (context.adCheckShow(adConfig) || isAdLoading()) return
//
//        if (adView == null) return
//
//        val params = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        //when ad is loaded already
//        bannerAdContainer?.removeAllViews()
//        (adView?.parent as? ViewGroup)?.removeAllViews()
//
//        Log.d(TAG, "showYandexBannerAd: $adView")
//        bannerAdContainer?.addView(adView, params)
//        bannerAdContainer?.isVisible = true
//        adAnalyticsTracker.trackAdShow()
//    }
//
//
//}