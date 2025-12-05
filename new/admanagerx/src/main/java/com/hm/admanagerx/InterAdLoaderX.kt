package com.hm.admanagerx

import android.app.Dialog
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hm.admanagerx.AdsManagerX.isAppLevelAdsInitializationSuccess
import com.hm.admanagerx.adsanalysis.AdAnalyticsTracker
import com.hm.admanagerx.utility.HandlerX

class InterAdLoaderX(var context: Context) {

    private var adRequestLoading: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null

    private var TAG = "InterAdLoaderX"

    var onAdClose: MutableLiveData<Unit>? = null
    var onAdShow: MutableLiveData<Unit>? = null

    private var onAdLoaded: MutableLiveData<InterstitialAd>? = null
    private var onAdFailed: MutableLiveData<String>? = null
    private var onAdClicked: MutableLiveData<Unit>? = null
    private var onAdImpression: MutableLiveData<Unit>? = null

    private lateinit var adConfig: AdConfig
    private lateinit var adConfigManager: AdConfigManager
    var adSessionCount: Long = 1

    var adShowCount: Long = 0

    private var loadingDialog: Dialog? = null

    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(adConfigManager.name) }

    fun getAdConfigManager() = adConfigManager
    fun loadInterAdX(
        adConfigManager: AdConfigManager,
        onAdLoaded: MutableLiveData<InterstitialAd>? = null,
        onAdFailed: MutableLiveData<String>? = null,
        onAdRequestDenied: MutableLiveData<Unit>? = null,
    ) {
        this.adConfigManager = adConfigManager
        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
        this.onAdLoaded = onAdLoaded
        this.onAdFailed = onAdFailed

        val checkLoadAdOnCount =
            adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount

        if (checkInterAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
            onAdRequestDenied?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(
                TAG
            )
            return
        }

        val adRequest = AdRequest.Builder().build()
        val interAdId = context.getAdId(adConfig.adId)

        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
        adAnalyticsTracker.trackAdRequest()
        adRequestLoading = true

        InterstitialAd.load(context, interAdId, adRequest, object : InterstitialAdLoadCallback() {

            override fun onAdLoaded(ad: InterstitialAd) {
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)

                mInterstitialAd = null
                mInterstitialAd = ad
                mInterstitialAd?.fullScreenContentCallback = interAdCallback
                onAdLoaded?.value = ad
                adRequestLoading = false

                adAnalyticsTracker.trackAdLoaded()

            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
                "${adConfigManager.name}_${adConfigManager.adConfig.adType}" + adError.toString()
                    .printIt(TAG)
                mInterstitialAd = null
                onAdFailed?.value = adError.message
                dismissDialog()
                adRequestLoading = false

                adAnalyticsTracker.trackAdLoadFailed()
            }
        })
    }


    val interAdCallback = object : FullScreenContentCallback() {

        override fun onAdShowedFullScreenContent() {
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad showed".printIt(TAG)

            mInterstitialAd = null
            adShowCount = 0
            //this for avoid app open Ad show when inter ad show
            context.isInterAdShow(true)
            context.isAppOpenAdShow(false)
            onAdShow?.value = Unit
            if (adConfig.isAdLoadAgain) reloadAd()

            if (adConfig.fullScreenAdSessionCount > 0) adSessionCount++

            adAnalyticsTracker.trackAdShow()

            HandlerX(500) {
                dismissDialog()
            }
        }

        override fun onAdDismissedFullScreenContent() {

            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad dismissed".printIt(TAG)
            dismissDialog()
            onAdClose?.value = Unit

            context.isInterAdShow(false)
            context.isAppOpenAdShow(true)
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {

            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad failed to show ${adError.message}".printIt(
                TAG
            )
            mInterstitialAd = null
            adShowCount = 0
            dismissDialog()
            adAnalyticsTracker.trackAdShowFailed()
            context.isAppOpenAdShow(true)
            context.isInterAdShow(false)
        }

        override fun onAdClicked() {
            super.onAdClicked()
            onAdClicked?.value = Unit
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(TAG)
            adAnalyticsTracker.trackAdClicked()
        }

        override fun onAdImpression() {
            super.onAdImpression()
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
            onAdImpression?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdImpression".printIt(TAG)
            adAnalyticsTracker.trackAdImpression()
        }

    }

    private fun dismissDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    fun reloadAd() = loadInterAdX(adConfigManager, onAdLoaded, onAdFailed)

    fun isAdLoaded() = mInterstitialAd != null

    fun showAd(
        activity: AppCompatActivity,
        onAdClose: MutableLiveData<Unit>? = null,
        onAdShow: MutableLiveData<Unit>? = null,
        onAdClicked: MutableLiveData<Unit>? = null,
        onAdImpression: MutableLiveData<Unit>? = null,
        funBlock: MutableLiveData<Unit>? = null
    ) {

        if (loadingDialog?.isShowing == true) return
        this.onAdClose = onAdClose
        this.onAdShow = onAdShow
        this.onAdClicked = onAdClicked
        this.onAdImpression = onAdImpression

        if (checkInterAdShow()) {
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

        loadingDialog = adConfig.showLoadingBeforeAd(
            isAdLoaded(), activity, adConfig.fullScreenAdLoadingLayout, onDone = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    mInterstitialAd?.setImmersiveMode(true)
                }
                mInterstitialAd?.show(activity) ?: run {
                    funBlock?.value = Unit
                }
            }, onLoadingShowHide = {

            })
    }

    private fun checkInterAdShow(): Boolean {
        return context.isPremium() ||
                !context.isOnline() ||
                !adConfig.isAdShow ||
                adRequestLoading ||
                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L
    }

}

