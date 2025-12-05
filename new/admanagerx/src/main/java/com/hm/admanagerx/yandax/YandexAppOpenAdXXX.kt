//package com.hm.admanagerx.old.yandax
//
//import android.app.Activity
//import android.app.Application
//import android.app.Dialog
//import android.content.Context
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ProcessLifecycleOwner
//import com.hm.admanagerx.old.AdConfig
//import com.hm.admanagerx.old.AdConfigManager
//import com.hm.admanagerx.old.adsanalysis.AdAnalyticsTracker
//import com.hm.admanagerx.old.checkAdShow
//import com.hm.admanagerx.old.checkInterAdShow
//import com.hm.admanagerx.old.getAdId
//import com.hm.admanagerx.old.isAppOpenAdShow
//import com.hm.admanagerx.old.isOnline
//import com.hm.admanagerx.old.isPremium
//import com.hm.admanagerx.old.printIt
//import com.hm.admanagerx.old.sendLogs
//import com.hm.admanagerx.old.showLoadingBeforeAd
//import com.hm.admanagerx.old.utility.HandlerX
//import com.yandex.mobile.ads.appopenad.AppOpenAd
//import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
//import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
//import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
//import com.yandex.mobile.ads.common.AdRequestConfiguration
//import com.yandex.mobile.ads.common.AdRequestError
//import com.yandex.mobile.ads.common.ImpressionData
//import java.util.Date
//
//class YandexAppOpenAdXXX(
//    var application: Application,
//) : Application.ActivityLifecycleCallbacks {
//
//
//    private var TAG = "YandexAppOpenAdX"
//
//    private var currentActivity: Activity? = null
//
//    private lateinit var adConfig: AdConfig
//
//    private var appOpenAd: AppOpenAd? = null
//    private var isLoadingAd = false
//    var isShowingAd = false
//
//    var loadingDialog: Dialog? = null
//
//    private var loadTime: Long = 0
//    private var loadCallback: AppOpenAdLoadListener? = null
//    private lateinit var adConfigManager: AdConfigManager
//    var onAdLoaded: MutableLiveData<Unit>? = null
//    var onAdLoadFailed: MutableLiveData<String>? = null
//    var onAdRequestDenied: MutableLiveData<Unit>? = null
//
//    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(adConfigManager.name) }
//
//
//    fun loadYandexAppOpenAd(
//        adConfigManager: AdConfigManager,
//        onAdLoaded: MutableLiveData<Unit>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//    ) {
//        this.adConfigManager = adConfigManager
//        this.onAdLoaded = onAdLoaded
//        this.onAdLoadFailed = onAdFailed
//        this.onAdRequestDenied = onAdRequestDenied
//
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//        application.registerActivityLifecycleCallbacks(this)
//
//        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
//
//        showAdIfAvailable()
//    }
//
//    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
//        val dateDifference: Long = Date().time - loadTime
//        val numMilliSecondsPerHour: Long = 3600000
//        return dateDifference < numMilliSecondsPerHour * numHours
//    }
//
//    private fun isAdAvailable(): Boolean {
//        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
//    }
//
//
//    private fun showAdIfAvailable(isShowAd: Boolean = false) {
//
//        if (!adConfig.isAdShow || isShowingAd || application.isPremium() || !application.isOnline() || application.checkInterAdShow()) {
//            this.onAdRequestDenied?.value = Unit
//            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
//            return
//        }
//        if (!isAdAvailable()) {
//            loadAd()
//            return
//        }
//
//        if (isShowAd) showAd(currentActivity)
//    }
//
//    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
//        override fun onStart(owner: LifecycleOwner) {
//            super.onStart(owner)
//            "ON_START".printIt()
//            if (application.checkAdShow()) {
//                HandlerX(10) {
//                    showAdIfAvailable(true)
//                }
//            } else {
//                "else".printIt()
//            }
//            application.isAppOpenAdShow(true)
//        }
//    }
//
//    private fun Context.getAppOpenKey(): String {
//        return getAdId(adConfig.adIdYandex)
//    }
//
//    fun loadAd() {
//
//        if (isLoadingAd || isAdAvailable()) {
//            return
//        }
//
//        isLoadingAd = true
//        val adRequestConfiguration =
//            AdRequestConfiguration.Builder(application.getAppOpenKey()).build()
//        try {
//
//            "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad load request".printIt(TAG)
//            application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//
//            adAnalyticsTracker.trackAdRequest()
//            loadCallback = object : AppOpenAdLoadListener {
//                override fun onAdFailedToLoad(error: AdRequestError) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error_${error.description}".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error")
//                    isLoadingAd = false
//                    loadingDialog?.dismiss()
//                    loadingDialog = null
//                    onAdLoadFailed?.value = error.description
//
//                    adAnalyticsTracker.trackAdLoadFailed()
//                }
//
//                override fun onAdLoaded(ad: AppOpenAd) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//
//                    appOpenAd = ad
//                    isLoadingAd = false
//                    loadTime = Date().time
//                    onAdLoaded?.value = Unit
//
//                    adAnalyticsTracker.trackAdLoaded()
//                }
//            }
//
//            val appOpenAdLoader = AppOpenAdLoader(application)
//            appOpenAdLoader.setAdLoadListener(loadCallback)
//            appOpenAdLoader.loadAd(adRequestConfiguration)
//        } catch (e: Exception) {
//
//        }
//
//    }
//
//    fun showAd(
//        activity: Activity?,
//        onAdShow: MutableLiveData<Unit>? = null,
//        onAdClose: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        funBlock: MutableLiveData<Unit>? = null,
//    ) {
//        if (loadingDialog?.isShowing == true) return
//
//        val fullScreenContentCallback: AppOpenAdEventListener =
//            object : AppOpenAdEventListener {
//                override fun onAdClicked() {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad clicked".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//                    onAdClicked?.value = Unit
//
//                    adAnalyticsTracker.trackAdClicked()
//                }
//
//                override fun onAdDismissed() {
//                    isShowingAd = false
//
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad Close".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_close")
//
//                    loadingDialog?.dismiss()
//                    loadingDialog = null
//                    onAdClose?.value = Unit
//                }
//
//                override fun onAdFailedToShow(adError: com.yandex.mobile.ads.common.AdError) {
//                    appOpenAd = null
//                    isShowingAd = false
//                    adError.description.printIt()
//                    loadAd()
//                    loadingDialog?.dismiss()
//
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_show_error_${adError.description}".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error")
//
//                    adAnalyticsTracker.trackAdShowFailed()
//                }
//
//                override fun onAdImpression(impressionData: ImpressionData?) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad impression".printIt(
//                        TAG
//                    )
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                    onAdImpression?.value = Unit
//                    adAnalyticsTracker.trackAdImpression()
//                }
//
//                override fun onAdShown() {
//                    appOpenAd = null
//                    loadAd()
//                    onAdShow?.value = Unit
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad show".printIt(TAG)
//                    application.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
//
//                    adAnalyticsTracker.trackAdShow()
//                }
//
//            }
//        appOpenAd?.setAdEventListener(fullScreenContentCallback)
//
//        activity?.let {
//            if (it !is AppCompatActivity) {
//                funBlock?.value = Unit
//                return
//            }
//
//            isShowingAd = true
//            loadingDialog = adConfig.showLoadingBeforeAd(isAdAvailable(), it,layout = adConfig.fullScreenAdLoadingLayout, onDone = {
//                appOpenAd?.show(it) ?: run { funBlock?.value = Unit }
//            })
//        }
//    }
//
//    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//
//
//    }
//
//    override fun onActivityStarted(activity: Activity) {
//        if (!isShowingAd) {
//            currentActivity = activity
//        }
//    }
//
//    override fun onActivityResumed(activity: Activity) {
//    }
//
//    override fun onActivityPaused(activity: Activity) {
//    }
//
//    override fun onActivityStopped(activity: Activity) {
//    }
//
//    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//
//    }
//
//    override fun onActivityDestroyed(activity: Activity) {
//        if (currentActivity?.isFinishing == true) {
//            loadingDialog = null
//            currentActivity = null
//        }
//    }
//
//    //App open Ad class
//    fun onDestroy() {
//        appOpenAd = null
//
//        currentActivity = null
//        application.unregisterActivityLifecycleCallbacks(this)
//        ProcessLifecycleOwner.get().lifecycle.removeObserver(defaultLifecycleObserver)
//    }
//}