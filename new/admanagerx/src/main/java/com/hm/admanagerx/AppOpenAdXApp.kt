package com.hm.admanagerx

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.hm.admanagerx.AdsManagerX.isAppLevelAdsInitializationSuccess
import com.hm.admanagerx.utility.HandlerX
import com.hm.admanagerx.utility.TinyDB

fun Context.isAppOpenAdShow(showOpenAd: Boolean) =
    TinyDB.getInstance(this).putBoolean("showOpenAd", showOpenAd)

fun Context.checkAdShow() = TinyDB.getInstance(this).getBoolean("showOpenAd")

fun Context.isInterAdShow(isInterAdShow: Boolean) =
    TinyDB.getInstance(this).putBoolean("isInterAdShow", isInterAdShow)

fun Context.checkInterAdShow() = TinyDB.getInstance(this).getBoolean("isInterAdShow")

class AppOpenAdXApp(var context: Application) {

    private var TAG = "AppOpenAdX"

    private var adRequestLoading: Boolean = false
    private var mAppOpenAd: AppOpenAd? = null

    var onAdClose: MutableLiveData<Unit>? = null
    var onAdShow: MutableLiveData<Unit>? = null

    private var onAdLoaded: MutableLiveData<Unit>? = null
    private var onAdFailed: MutableLiveData<String>? = null
    private var onAdClicked: MutableLiveData<Unit>? = null
    private var onAdImpression: MutableLiveData<Unit>? = null
    private var funBlock: MutableLiveData<Unit>? = null

    private lateinit var adConfig: AdConfig
    private lateinit var adConfigManager: AdConfigManager
    private var adSessionCount: Long = 1

    var adShowCount: Long = 0

    private var loadingDialog: Dialog? = null

    fun isAdLoaded() = mAppOpenAd != null

    fun getAdConfigManager() = adConfigManager


    fun loadAppOpenAdX(
        adConfigManager: AdConfigManager,
        onAdLoaded: MutableLiveData<Unit>? = null,
        onAdFailed: MutableLiveData<String>? = null,
        onAdRequestDenied: MutableLiveData<Unit>? = null,
    ) {

        this.adConfigManager = adConfigManager
        this.adConfig = adConfigManager.adConfig
        this.onAdLoaded = onAdLoaded
        this.onAdFailed = onAdFailed

        val checkLoadAdOnCount =
            adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount

        if (checkAppOpenAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
            onAdRequestDenied?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(
                TAG
            )
            return
        }

        val request = AdRequest.Builder().build()
        val appOpenAdId = context.getAdId(adConfig.adId)


        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
        adRequestLoading = true

        AppOpenAd.load(context, appOpenAdId, request, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {

                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")

                mAppOpenAd = null
                mAppOpenAd = ad
                mAppOpenAd?.fullScreenContentCallback = appOpenAdCallback
                onAdLoaded?.value = Unit
                adRequestLoading = false

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                "${adConfigManager.name}_${adConfigManager.adConfig.adType}_error_${loadAdError.message}".printIt(
                    TAG
                )
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_error")
                mAppOpenAd = null
                loadingDialog?.dismiss()
                onAdFailed?.value = loadAdError.message
                dismissDialog()

            }
        })

        if (adConfig.isAppOpenAdAppLevel) {
            setLifecycleObserver()
            setActivityLifeCycleCallbacks()
        }
    }


    private var currentActivity: Activity? = null
    var isShowingAd = false

    private fun setLifecycleObserver() =
        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)

    fun removeLifecycleObserver() =
        ProcessLifecycleOwner.get().lifecycle.removeObserver(defaultLifecycleObserver)

    private fun setActivityLifeCycleCallbacks() {
        context.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                if (!isShowingAd) {
                    currentActivity = activity
                }
            }

            override fun onActivityResumed(p0: Activity) {

            }

            override fun onActivityPaused(p0: Activity) {

            }

            override fun onActivityStopped(p0: Activity) {

            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

            }

            override fun onActivityDestroyed(p0: Activity) {
                loadingDialog = null
                currentActivity = null
            }

        })
    }


    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            "ON_START ${context.checkAdShow()}".printIt()
            if (context.checkAdShow()) {
                HandlerX(10) {
                    "ON_START HandlerX =${context.checkAdShow()}".printIt()
                    if (currentActivity?.isFinishing == false || currentActivity?.isDestroyed == false) {
                        showAd(
                            currentActivity,
                            onAdClose,
                            onAdShow,
                            onAdClicked,
                            onAdImpression,
                            funBlock
                        )
                    }
                }
            } else {
                "else".printIt()
            }
            context.isAppOpenAdShow(true)
        }
    }

    val appOpenAdCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {

            if (!adConfig.isAppOpenAdAppLevel) context.isInterAdShow(false)

            isShowingAd = false
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad dismissed".printIt(TAG)
            onAdClose?.value = Unit
            dismissDialog()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {

            isShowingAd = false

            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad failed to show ${adError.message}".printIt(
                TAG
            )
            mAppOpenAd = null
            adShowCount = 0
            dismissDialog()
        }

        override fun onAdShowedFullScreenContent() {
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad showed".printIt(TAG)

            if (!adConfig.isAppOpenAdAppLevel) context.isInterAdShow(true)

            isShowingAd = true

            mAppOpenAd = null
            adShowCount = 0
            onAdShow?.value = Unit
            if (adConfig.isAdLoadAgain || adConfig.isAppOpenAdAppLevel) reloadAd()

            if (adConfig.fullScreenAdSessionCount > 0) adSessionCount++

            HandlerX(500) {
                dismissDialog()
            }
        }

        override fun onAdImpression() {
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad impression".printIt(TAG)
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
            onAdImpression?.value = Unit
        }

        override fun onAdClicked() {
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad clicked".printIt(TAG)
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
            onAdClicked?.value = Unit

        }
    }

    private fun dismissDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }


    fun reloadAd() = loadAppOpenAdX(adConfigManager, onAdLoaded, onAdFailed)

    fun showAd(
        activity: Activity?,
        onAdClose: MutableLiveData<Unit>? = null,
        onAdShow: MutableLiveData<Unit>? = null,
        onAdClicked: MutableLiveData<Unit>? = null,
        onAdImpression: MutableLiveData<Unit>? = null,
        funBlock: MutableLiveData<Unit>? = null,
    ) {

        if (loadingDialog?.isShowing == true) return
        this.onAdClose = onAdClose
        this.onAdShow = onAdShow
        this.onAdClicked = onAdClicked
        this.onAdImpression = onAdImpression
        this.funBlock = funBlock

        if (checkAppOpenAdShow() || isShowingAd || context.checkInterAdShow()) {
            funBlock?.value = Unit
            return
        }

        // Remote config Ad click counter logic
        if (adShowCount < adConfig.fullScreenAdCount) {
            if (!isAdLoaded() && adConfig.fullScreenAdLoadOnCount > 0L && adShowCount == adConfig.fullScreenAdLoadOnCount) reloadAd()
            adShowCount++
            funBlock?.value = Unit
            return
        }
        activity?.let {
            if (it !is AppCompatActivity) {
                funBlock?.value = Unit
                return
            }
            isShowingAd = true
            loadingDialog = adConfig.showLoadingBeforeAd(
                isAdLoaded(), it, adConfig.fullScreenAdLoadingLayout, onDone = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        mAppOpenAd?.setImmersiveMode(true)
                    }
                    mAppOpenAd?.show(activity) ?: run { funBlock?.value = Unit }
                }, onLoadingShowHide = {})
        } ?: run { funBlock?.value = Unit }
    }


    private fun checkAppOpenAdShow(): Boolean {
        return context.isPremium() ||
                !context.isOnline() ||
                !adConfig.isAdShow ||
                adRequestLoading ||
                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L
    }

    fun destroyAd() {
        if (adConfig.isAppOpenAdAppLevel) {
            removeLifecycleObserver()
        }
    }
}