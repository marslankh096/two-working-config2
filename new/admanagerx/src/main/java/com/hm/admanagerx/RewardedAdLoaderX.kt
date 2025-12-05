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
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hm.admanagerx.AdsManagerX.isAppLevelAdsInitializationSuccess
import com.hm.admanagerx.adsanalysis.AdAnalyticsTracker
import com.hm.admanagerx.utility.HandlerX

class RewardedAdLoaderX(var context: Context) {

    private var adRequestLoading: Boolean = false
    var rewardedAd: RewardedAd? = null

    private var TAG = "RewardedAdLoaderX"

    var onAdClose: MutableLiveData<Unit>? = null
    var onAdShow: MutableLiveData<Unit>? = null

    var loadingDialog: Dialog? = null


    private var onAdLoaded: MutableLiveData<RewardedAd>? = null
    private var onAdFailed: MutableLiveData<String>? = null
    private var onAdClicked: MutableLiveData<Unit>? = null
    private var onAdImpression: MutableLiveData<Unit>? = null


    var adShowCount: Long = 1
    var adSessionCount: Long = 1

    private lateinit var adConfigManager: AdConfigManager

    private lateinit var adConfig: AdConfig

    fun getAdConfigManager() = adConfigManager

    fun isAdLoaded() = rewardedAd != null

    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(adConfigManager.name) }

    fun loadRewardedAdX(
        adConfigManager: AdConfigManager,
        onAdLoaded: MutableLiveData<RewardedAd>? = null,
        onAdFailed: MutableLiveData<String>? = null,
        onAdRequestDenied: MutableLiveData<Unit>? = null,
    ) {

        this.adConfigManager = adConfigManager
        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
        this.onAdLoaded = onAdLoaded
        this.onAdFailed = onAdFailed

        val checkLoadAdOnCount = adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount

        if (checkRewardedAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
            onAdRequestDenied?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(TAG)
            return
        }
        val adRequest = AdRequest.Builder().build()

        val rewardedAdId = context.getAdId(adConfig.adId)

        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")

        adAnalyticsTracker.trackAdRequest()

        adRequestLoading = true

        RewardedAd.load(context, rewardedAdId, adRequest, object : RewardedAdLoadCallback() {

            override fun onAdLoaded(ad: RewardedAd) {
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")

                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)

                rewardedAd = null
                rewardedAd = ad
                rewardedAd?.fullScreenContentCallback = rewardedCallback
                onAdLoaded?.value = ad
                adRequestLoading = false

                adAnalyticsTracker.trackAdLoaded()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {

                "${adConfigManager.name}_${adConfigManager.adConfig.adType}_${adError.message}".printIt(TAG)

                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")

                rewardedAd = null
                onAdFailed?.value = adError.message
                dialogDismiss()
                adRequestLoading = false
                adAnalyticsTracker.trackAdLoadFailed()
            }
        })
    }


    private fun checkRewardedAdShow(): Boolean {
        return context.isPremium() ||
                !context.isOnline() ||
                !adConfig.isAdShow ||
                adRequestLoading ||
                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L

    }


    val rewardedCallback = object : FullScreenContentCallback() {

        override fun onAdShowedFullScreenContent() {
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad showed".printIt(TAG)

            rewardedAd = null
            adShowCount = 1
            context.isInterAdShow(true) //this for avoid app open Ad show when rewarded ad show
            onAdShow?.value = Unit
            if (adConfig.isAdLoadAgain) reloadAd()

            if (adConfig.fullScreenAdSessionCount > 0) adSessionCount++

            adAnalyticsTracker.trackAdShow()

            HandlerX(500) {
                dialogDismiss()
            }
        }

        override fun onAdDismissedFullScreenContent() {

            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad dismissed".printIt(TAG)

            onAdClose?.value = Unit
            dialogDismiss()
            context.isInterAdShow(false) //this for avoid app open Ad show when rewarded ad show

        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad failed to show ${adError.message}".printIt(TAG)
            rewardedAd = null
            dialogDismiss()

            adAnalyticsTracker.trackAdShowFailed()
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

    private fun dialogDismiss() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    fun reloadAd() =
        loadRewardedAdX(adConfigManager, onAdLoaded, onAdFailed)

    fun showAd(
        activity: AppCompatActivity,
        onAdClose: MutableLiveData<Unit>? = null,
        onAdShow: MutableLiveData<Unit>? = null,
        onAdClicked: MutableLiveData<Unit>? = null,
        onAdImpression: MutableLiveData<Unit>? = null,
        onUserEarnedRewarded: MutableLiveData<RewardItem>? = null,
        funBlock: MutableLiveData<Unit>? = null,
    ) {
        if (loadingDialog?.isShowing == true) return

        this.onAdClose = onAdClose
        this.onAdShow = onAdShow
        this.onAdClicked = onAdClicked
        this.onAdImpression = onAdImpression

        if (checkRewardedAdShow()) {
            funBlock?.value = Unit
            return
        }

        // Remote config Ad click counter logic
        if (adShowCount <= adConfig.fullScreenAdCount) {
            if (!isAdLoaded() && adConfig.fullScreenAdLoadOnCount > 0L && adShowCount == adConfig.fullScreenAdLoadOnCount) reloadAd()
            adShowCount++
            funBlock?.value = Unit
            return
        }

        loadingDialog =
            adConfig.showLoadingBeforeAd(
                isAdLoaded(),
                activity,
                adConfig.fullScreenAdLoadingLayout,
                onDone = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        rewardedAd?.setImmersiveMode(true)
                    }
                    rewardedAd?.show(activity) { onUserEarnedRewarded?.value = it } ?: run {
                        funBlock?.value = Unit
                    }
                })
    }


}

