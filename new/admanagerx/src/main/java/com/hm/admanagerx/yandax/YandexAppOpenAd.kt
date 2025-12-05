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
//import com.hm.admanagerx.old.AdsManagerX.isAppLevelAdsInitializationSuccess
//import com.hm.admanagerx.old.checkAdShow
//import com.hm.admanagerx.old.checkInterAdShow
//import com.hm.admanagerx.old.getAdId
//import com.hm.admanagerx.old.isAppOpenAdShow
//import com.hm.admanagerx.old.isInterAdShow
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
//
//class YandexAppOpenAd(var context: Application) {
//
//    private var TAG = "YandexAppOpenAdX"
//
//    private var adRequestLoading: Boolean = false
//    private var mAppOpenAd: AppOpenAd? = null
//
//    var onAdClose: MutableLiveData<Unit>? = null
//    var onAdShow: MutableLiveData<Unit>? = null
//
//    private var onAdLoaded: MutableLiveData<Unit>? = null
//    private var onAdFailed: MutableLiveData<String>? = null
//    private var onAdClicked: MutableLiveData<Unit>? = null
//    private var onAdImpression: MutableLiveData<Unit>? = null
//    private var funBlock: MutableLiveData<Unit>? = null
//    private var loadCallback: AppOpenAdLoadListener? = null
//    private lateinit var adConfig: AdConfig
//    private lateinit var adConfigManager: AdConfigManager
//    private var adSessionCount: Long = 1
//
//    var adShowCount: Long = 0
//
//    private var loadingDialog: Dialog? = null
//
//    fun isAdLoaded() = mAppOpenAd != null
//
//    private fun Context.getAppOpenKey(): String {
//        return getAdId(adConfig.adIdYandex)
//    }
//
//    fun getAdConfigManager() = adConfigManager
//    fun loadYandexAppOpenAd(
//        adConfigManager: AdConfigManager,
//        onAdLoaded: MutableLiveData<Unit>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//    ) {
//
//        this.adConfigManager = adConfigManager
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//        this.onAdLoaded = onAdLoaded
//        this.onAdFailed = onAdFailed
//
//        val checkLoadAdOnCount =
//            adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount
//
//        if (checkAppOpenAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
//            onAdRequestDenied?.value = Unit
//            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(
//                TAG
//            )
//            return
//        }
//        val adRequestConfiguration =
//            AdRequestConfiguration.Builder(context.getAppOpenKey()).build()
//
//
//        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
//        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//        adRequestLoading = true
//
//        try {
//
//            "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad load request".printIt(
//                TAG
//            )
//            context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//
//            loadCallback = object : AppOpenAdLoadListener {
//                override fun onAdFailedToLoad(error: AdRequestError) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error_${error.description}".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error")
//                    mAppOpenAd = null
//                    loadingDialog?.dismiss()
//
//                    onAdFailed?.value = error.description
//                    dismissDialog()
//                }
//
//                override fun onAdLoaded(openAd: AppOpenAd) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//
//                    mAppOpenAd = null
//                    mAppOpenAd = openAd
//                    onAdLoaded?.value = Unit
//                    adRequestLoading = false
//
//                }
//            }
//
//            val appOpenAdLoader = AppOpenAdLoader(context)
//            appOpenAdLoader.setAdLoadListener(loadCallback)
//            appOpenAdLoader.loadAd(adRequestConfiguration)
//        } catch (e: Exception) {
//
//        }
//
//        if (adConfig.isAppOpenAdAppLevel) {
//            setLifecycleObserver()
//            setActivityLifeCycleCallbacks()
//        }
//    }
//
//
//    private var currentActivity: Activity? = null
//    var isShowingAd = false
//
//    private fun setLifecycleObserver() =
//        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
//
//    fun removeLifecycleObserver() =
//        ProcessLifecycleOwner.get().lifecycle.removeObserver(defaultLifecycleObserver)
//
//    private fun setActivityLifeCycleCallbacks() {
//        context.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
//
//            }
//
//            override fun onActivityStarted(activity: Activity) {
//                if (!isShowingAd) {
//                    currentActivity = activity
//                }
//            }
//
//            override fun onActivityResumed(p0: Activity) {
//
//            }
//
//            override fun onActivityPaused(p0: Activity) {
//
//            }
//
//            override fun onActivityStopped(p0: Activity) {
//
//            }
//
//            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
//
//            }
//
//            override fun onActivityDestroyed(p0: Activity) {
//                if (currentActivity?.isFinishing == true) {
//                    loadingDialog = null
//                    currentActivity = null
//                }
//            }
//
//        })
//    }
//
//
//    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
//        override fun onStart(owner: LifecycleOwner) {
//            super.onStart(owner)
//            "ON_START".printIt()
//            if (context.checkAdShow()) {
//                HandlerX(10) {
//                    showYandexAppOpenAd(
//                        currentActivity,
//                        onAdClose,
//                        onAdShow,
//                        onAdClicked,
//                        onAdImpression,
//                        funBlock
//                    )
//                }
//            } else {
//                "else".printIt()
//            }
//
//
//            context.isAppOpenAdShow(true)
//        }
//    }
//
//
//    private fun dismissDialog() {
//        loadingDialog?.dismiss()
//        loadingDialog = null
//    }
//
//
//    fun reloadAd() = loadYandexAppOpenAd(adConfigManager, onAdLoaded, onAdFailed)
//
//    fun showYandexAppOpenAd(
//        activity: Activity?,
//        onAdClose: MutableLiveData<Unit>? = null,
//        onAdShow: MutableLiveData<Unit>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        funBlock: MutableLiveData<Unit>? = null,
//    ) {
//        if (loadingDialog?.isShowing == true) return
//
//        if (loadingDialog?.isShowing == true) return
//        this.onAdClose = onAdClose
//        this.onAdShow = onAdShow
//        this.onAdClicked = onAdClicked
//        this.onAdImpression = onAdImpression
//        this.funBlock = funBlock
//
//        if (checkAppOpenAdShow() || isShowingAd || context.checkInterAdShow()) {
//            funBlock?.value = Unit
//            return
//        }
//        val fullScreenContentCallback: AppOpenAdEventListener =
//            object : AppOpenAdEventListener {
//                override fun onAdClicked() {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad clicked".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//                    onAdClicked?.value = Unit
//
//                }
//
//                override fun onAdDismissed() {
//                    isShowingAd = false
//                    if (!adConfig.isAppOpenAdAppLevel) context.isInterAdShow(false)
//
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad Close".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_close")
//
//                    loadingDialog?.dismiss()
//                    loadingDialog = null
//                    onAdClose?.value = Unit
//                }
//
//                override fun onAdFailedToShow(adError: com.yandex.mobile.ads.common.AdError) {
//                    mAppOpenAd = null
//                    isShowingAd = false
//                    adError.description.printIt()
//
//                    loadingDialog?.dismiss()
//
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_show_error_${adError.description}".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_error")
//
//                }
//
//                override fun onAdImpression(impressionData: ImpressionData?) {
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad impression".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                    onAdImpression?.value = Unit
//                }
//
//                override fun onAdShown() {
//
//                    if (!adConfig.isAppOpenAdAppLevel) context.isInterAdShow(true)
//                    mAppOpenAd = null
//                    onAdShow?.value = Unit
//                    "y_${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad show".printIt(
//                        TAG
//                    )
//                    context.sendLogs("y_${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
//                    if (adConfig.isAdLoadAgain || adConfig.isAppOpenAdAppLevel) reloadAd()
//                }
//
//            }
//        mAppOpenAd?.setAdEventListener(fullScreenContentCallback)
//
//
//        // Remote config Ad click counter logic
//        if (adShowCount < adConfig.fullScreenAdCount) {
//            if (!isAdLoaded() && adConfig.fullScreenAdLoadOnCount > 0L && adShowCount == adConfig.fullScreenAdLoadOnCount) reloadAd()
//            adShowCount++
//            funBlock?.value = Unit
//            return
//        }
//
//        activity?.let {
//            if (it !is AppCompatActivity) {
//                funBlock?.value = Unit
//                return
//            }
//            isShowingAd = true
//            loadingDialog = adConfig.showLoadingBeforeAd(
//                isAdLoaded(), it, adConfig.fullScreenAdLoadingLayout, onDone = {
//                    mAppOpenAd?.show(activity) ?: run { funBlock?.value = Unit }
//                }, onLoadingShowHide = {
//                })
//        } ?: run { funBlock?.value = Unit }
//    }
//
//
//    private fun checkAppOpenAdShow(): Boolean {
//        return context.isPremium() ||
//                !context.isOnline() ||
//                !adConfig.isAdShow ||
//                adRequestLoading ||
//                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L
//    }
//
//    fun destroyAd() {
//        if (adConfig.isAppOpenAdAppLevel) {
//            removeLifecycleObserver()
//        }
//    }
//}