//package com.hm.admanagerx.old.yandax
//
//import android.app.Dialog
//import android.content.Context
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.MutableLiveData
//import com.hm.admanagerx.old.AdConfig
//import com.hm.admanagerx.old.AdConfigManager
//import com.hm.admanagerx.old.AdsManagerX.isAppLevelAdsInitializationSuccess
//import com.hm.admanagerx.old.adsanalysis.AdAnalyticsTracker
//import com.hm.admanagerx.old.getAdId
//import com.hm.admanagerx.old.isAppOpenAdShow
//import com.hm.admanagerx.old.isInterAdShow
//import com.hm.admanagerx.old.isOnline
//import com.hm.admanagerx.old.isPremium
//import com.hm.admanagerx.old.printIt
//import com.hm.admanagerx.old.sendLogs
//import com.hm.admanagerx.old.showLoadingBeforeAd
//import com.yandex.mobile.ads.common.AdRequestConfiguration
//import com.yandex.mobile.ads.common.ImpressionData
//import com.yandex.mobile.ads.common.AdError as YandexAdError
//import com.yandex.mobile.ads.common.AdRequestError as YandexAdRequestError
//import com.yandex.mobile.ads.interstitial.InterstitialAd as YandexInterAd
//import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener as YandexAdEventListener
//import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener as YandexAdLoadListener
//import com.yandex.mobile.ads.interstitial.InterstitialAdLoader as YandexAdLoader
//
//class YandexInterAdx(var context: Context) {
//    private var adRequestLoading: Boolean = false
//    private var mYandexInterAd: YandexInterAd? = null
//
//    private var TAG = "YandexInterAdLoaderX"
//
//    var onAdClose: MutableLiveData<Unit>? = null
//    var onAdShow: MutableLiveData<Unit>? = null
//
//    private var onAdLoaded: MutableLiveData<YandexInterAd>? = null
//    private var onAdFailed: MutableLiveData<String>? = null
//    private var onAdClicked: MutableLiveData<Unit>? = null
//    private var onAdImpression: MutableLiveData<Unit>? = null
//
//    private lateinit var adConfig: AdConfig
//    private lateinit var adConfigManager: AdConfigManager
//    private var adSessionCount: Long = 1
//
//    var adShowCount: Long = 0
//
//    private var loadingDialog: Dialog? = null
//
//    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(adConfigManager.name) }
//
//    fun getAdConfigManager() = adConfigManager
//    fun loadYandexInterAdX(
//        adConfigManager: AdConfigManager,
//        onAdLoaded: MutableLiveData<YandexInterAd>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//    ) {
//        this.adConfigManager = adConfigManager
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//        this.onAdLoaded = onAdLoaded
//        this.onAdFailed = onAdFailed
//
//        val checkLoadAdOnCount =
//            adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount
//        if (checkYandexInterAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
//            onAdRequestDenied?.value = Unit
//            return
//        }
//
//        val interAdId = context.getAdId(adConfig.adIdYandex ?: "")
//        val yandexInterRequest = AdRequestConfiguration.Builder(interAdId).build()
//
//        "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
//        context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//        adAnalyticsTracker.trackAdRequest()
//        adRequestLoading = true
//
//        fun getAdEventListener() = object : YandexAdEventListener {
//            override fun onAdClicked() {
//                onAdClicked?.value = Unit
//                context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(
//                    TAG
//                )
//                adAnalyticsTracker.trackAdClicked()
//            }
//
//            override fun onAdDismissed() {
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad dismissed".printIt(
//                    TAG
//                )
//                mYandexInterAd = null
//                dismissDialog()
//                onAdClose?.value = Unit
//                context.isInterAdShow(false)
//                context.isAppOpenAdShow(true)
//            }
//
//            override fun onAdFailedToShow(adError: YandexAdError) {
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad failed to show ${adError.description}".printIt(
//                    TAG
//                )
//                mYandexInterAd = null
//                adShowCount = 0
//                dismissDialog()
//                adAnalyticsTracker.trackAdShowFailed()
//                context.isInterAdShow(false)
//                context.isAppOpenAdShow(true)
//            }
//
//            override fun onAdImpression(impressionData: ImpressionData?) {
//                context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                onAdImpression?.value = Unit
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdImpression".printIt(
//                    TAG
//                )
//                adAnalyticsTracker.trackAdImpression()
//            }
//
//            override fun onAdShown() {
//                context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad showed".printIt(TAG)
//
//                mYandexInterAd = null
//                adShowCount = 0
//                //this for avoid app open Ad show when inter ad show
//                context.isInterAdShow(true)
//                context.isAppOpenAdShow(false)
//                onAdShow?.value = Unit
//                if (adConfig.isAdLoadAgain) reloadAd()
//
//                if (adConfig.fullScreenAdSessionCount > 0) adSessionCount++
//
//                adAnalyticsTracker.trackAdShow()
//                dismissDialog()
//            }
//        }
//
//        fun getLoadListener() = object : YandexAdLoadListener {
//            override fun onAdFailedToLoad(error: YandexAdRequestError) {
//                context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType}" + error.toString()
//                    .printIt(TAG)
//                mYandexInterAd = null
//                onAdFailed?.value = error.description
//                dismissDialog()
//                adRequestLoading = false
//
//                adAnalyticsTracker.trackAdLoadFailed()
//            }
//
//            override fun onAdLoaded(interstitialAd: YandexInterAd) {
//                interstitialAd.setAdEventListener(getAdEventListener())
//                context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//                "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
//
//                mYandexInterAd = null
//                mYandexInterAd = interstitialAd
//                mYandexInterAd?.setAdEventListener(getAdEventListener())
//                onAdLoaded?.value = interstitialAd
//                adRequestLoading = false
//
//                adAnalyticsTracker.trackAdLoaded()
//            }
//        }
//
//        YandexAdLoader(context).apply {
//            setAdLoadListener(getLoadListener())
//            loadAd(yandexInterRequest)
//        }
//    }
//
//    private fun dismissDialog() {
//        loadingDialog?.dismiss()
//        loadingDialog = null
//    }
//
//    fun reloadAd() = loadYandexInterAdX(adConfigManager, onAdLoaded, onAdFailed)
//
//     fun isAdLoaded() = mYandexInterAd != null
//
//
//    fun showYandexInterAd(
//        activity: AppCompatActivity,
//        onAdClose: MutableLiveData<Unit>? = null,
//        onAdShow: MutableLiveData<Unit>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        funBlock: MutableLiveData<Unit>? = null,
//    ) {
//
//        if (loadingDialog?.isShowing == true) return
//        this.onAdClose = onAdClose
//        this.onAdShow = onAdShow
//        this.onAdClicked = onAdClicked
//        this.onAdImpression = onAdImpression
//
//        if (checkYandexInterAdShow()) {
//            funBlock?.value = Unit
//            return
//        }
//
//        // Remote config Ad click counter logic
//        if (adShowCount < adConfig.fullScreenAdCount) {
//            if (!isAdLoaded() && adConfig.fullScreenAdLoadOnCount > 0L && adShowCount == adConfig.fullScreenAdLoadOnCount) reloadAd()
//            adShowCount++
//            funBlock?.value = Unit
//            return
//        }
//
//        loadingDialog = adConfig.showLoadingBeforeAd(
//            isAdLoaded(), activity, adConfig.fullScreenAdLoadingLayout
//        , onDone = {
//            mYandexInterAd?.show(activity) ?: run {
//                funBlock?.value = Unit
//            }
//        }, onLoadingShowHide = {})
//    }
//
//    private fun checkYandexInterAdShow(): Boolean {
//        return context.isPremium() ||
//                !context.isOnline() ||
//                !adConfig.isAdShow ||
//                adRequestLoading ||
//                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L
//    }
//}