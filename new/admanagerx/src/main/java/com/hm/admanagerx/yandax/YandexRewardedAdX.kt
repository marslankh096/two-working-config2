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
//import com.hm.admanagerx.old.isInterAdShow
//import com.hm.admanagerx.old.isOnline
//import com.hm.admanagerx.old.isPremium
//import com.hm.admanagerx.old.printIt
//import com.hm.admanagerx.old.sendLogs
//import com.hm.admanagerx.old.showLoadingBeforeAd
//import com.yandex.mobile.ads.common.AdError
//import com.yandex.mobile.ads.common.AdRequestConfiguration
//import com.yandex.mobile.ads.common.AdRequestError
//import com.yandex.mobile.ads.common.ImpressionData
//import com.yandex.mobile.ads.rewarded.Reward
//import com.yandex.mobile.ads.rewarded.RewardedAd
//import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
//import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
//import com.yandex.mobile.ads.rewarded.RewardedAdLoader
//
//class YandexRewardedAdX(private val context: Context) {
//
//    private var adRequestLoading: Boolean = false
//    var rewardedAdLoader: RewardedAdLoader? = null
//    private var rewardedAd: RewardedAd? = null
//    private var TAG = "RewardedAdLoaderX"
//
//    var onAdClose: MutableLiveData<Unit>? = null
//    var onAdShow: MutableLiveData<Unit>? = null
//
//    var loadingDialog: Dialog? = null
//
//
//    private var onAdLoaded: MutableLiveData<RewardedAd>? = null
//    private var onAdFailed: MutableLiveData<String>? = null
//    private var onAdClicked: MutableLiveData<Unit>? = null
//    private var onAdImpression: MutableLiveData<Unit>? = null
//
//
//    var adShowCount: Long = 1
//    var adSessionCount: Long = 1
//
//    private lateinit var adConfigManager: AdConfigManager
//
//    private lateinit var adConfig: AdConfig
//
//    fun isAdLoaded() = rewardedAdLoader != null
//
//    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(adConfigManager.name) }
//
//    fun getAdConfigManager() = adConfigManager
//
//    fun loadRewardedAdX(
//        adConfigManager: AdConfigManager,
//        onAdLoaded: MutableLiveData<RewardedAd>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//    ) {
//
//
//        this.adConfigManager = adConfigManager
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//        this.onAdLoaded = onAdLoaded
//        this.onAdFailed = onAdFailed
//
//        val checkLoadAdOnCount =
//            adConfig.fullScreenAdLoadOnCount > 0L && adShowCount != adConfig.fullScreenAdLoadOnCount
//
//        if (checkRewardedAdShow() || isAdLoaded() || !isAppLevelAdsInitializationSuccess || checkLoadAdOnCount) {
//            onAdRequestDenied?.value = Unit
//            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(
//                TAG
//            )
//            return
//        }
//
//        val rewardedAdId = context.getAdId(adConfig.adIdYandex)
//
//        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
//        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//
//        adAnalyticsTracker.trackAdRequest()
//
//        adRequestLoading = true
//
//        rewardedAdLoader =RewardedAdLoader(context).apply {
//            setAdLoadListener(object : RewardedAdLoadListener {
//                override fun onAdLoaded(loadedRewardedAd: RewardedAd) {
//
//                    context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
//                    rewardedAd = null
//                    rewardedAd = loadedRewardedAd
//                    onAdLoaded?.value = loadedRewardedAd
//                    adRequestLoading = false
//
//                    adAnalyticsTracker.trackAdLoaded()
//                }
//
//                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType}_${adRequestError.description}".printIt(TAG)
//
//                    context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
//
//                    rewardedAd = null
//                    onAdFailed?.value = adRequestError.description
//                    dialogDismiss()
//                    adRequestLoading = false
//                    adAnalyticsTracker.trackAdLoadFailed()
//                }
//            })
//        }
//
//        val adRequestConfiguration = AdRequestConfiguration.Builder(rewardedAdId).build()
//        rewardedAdLoader?.loadAd(adRequestConfiguration)
//
//
//    }
//
//    fun showAd(
//        activity: AppCompatActivity,
//        onAdClose: MutableLiveData<Unit>? = null,
//        onAdShow: MutableLiveData<Unit>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        onUserEarnedRewarded: MutableLiveData<Reward>? = null,
//        funBlock: MutableLiveData<Unit>? = null,
//    ) {
//        if (loadingDialog?.isShowing == true) return
//
//        this.onAdClose = onAdClose
//        this.onAdShow = onAdShow
//        this.onAdClicked = onAdClicked
//        this.onAdImpression = onAdImpression
//
//        if (checkRewardedAdShow()) {
//            funBlock?.value = Unit
//            return
//        }
//
//        // Remote config Ad click counter logic
//        if (adShowCount <= adConfig.fullScreenAdCount) {
//            if (!isAdLoaded() && adConfig.fullScreenAdLoadOnCount > 0L && adShowCount == adConfig.fullScreenAdLoadOnCount) reloadAd()
//            adShowCount++
//            funBlock?.value = Unit
//            return
//        }
//
//        loadingDialog =
//            adConfig.showLoadingBeforeAd(
//                isAdLoaded(),
//                activity,
//                adConfig.fullScreenAdLoadingLayout,
//                onDone = {
//                    rewardedAd?.show(activity) ?: run {
//                        dialogDismiss()
//                        funBlock?.value = Unit
//                    }
//                })
//
//        rewardedAd?.apply {
//            setAdEventListener(object : RewardedAdEventListener {
//                override fun onAdShown() {
//                    context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_show")
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad showed".printIt(
//                        TAG
//                    )
//
//                    rewardedAd = null
//                    adShowCount = 1
//                    context.isInterAdShow(true) //this for avoid app open Ad show when rewarded ad show
//                    onAdShow?.value = Unit
//                    if (adConfig.isAdLoadAgain) reloadAd()
//
//                    if (adConfig.fullScreenAdSessionCount > 0) adSessionCount++
//
//                    adAnalyticsTracker.trackAdShow()
//                }
//
//                override fun onAdFailedToShow(adError: AdError) {
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad failed to show ${adError.description}".printIt(
//                        TAG
//                    )
//                    rewardedAd = null
//                    dialogDismiss()
//
//                    adAnalyticsTracker.trackAdShowFailed()
//                }
//
//                override fun onAdDismissed() {
//
//                    rewardedAd?.setAdEventListener(null)
//                    rewardedAd = null
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad dismissed".printIt(
//                        TAG
//                    )
//
//                    onAdClose?.value = Unit
//                    dialogDismiss()
//                    context.isInterAdShow(false) //this for avoid app open Ad show when rewarded ad show
//                }
//
//                override fun onAdClicked() {
//                    onAdClicked?.value = Unit
//                    context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(
//                        TAG
//                    )
//                    adAnalyticsTracker.trackAdClicked()
//                }
//
//                override fun onAdImpression(impressionData: ImpressionData?) {
//                    context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                    onAdImpression?.value = Unit
//                    "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdImpression".printIt(
//                        TAG
//                    )
//                    adAnalyticsTracker.trackAdImpression()
//                }
//
//                override fun onRewarded(reward: Reward) {
//                    onUserEarnedRewarded?.value = reward
//                }
//            })
//        }
//    }
//
//    private fun dialogDismiss() {
//        loadingDialog?.dismiss()
//        loadingDialog = null
//    }
//
//    private fun checkRewardedAdShow(): Boolean {
//        return context.isPremium() ||
//                !context.isOnline() ||
//                !adConfig.isAdShow ||
//                adRequestLoading ||
//                adSessionCount > adConfig.fullScreenAdSessionCount && adConfig.fullScreenAdSessionCount != 0L
//
//    }
//
//    fun reloadAd() =
//        loadRewardedAdX(adConfigManager, onAdLoaded, onAdFailed)
//
//     fun destroyRewardedAd() {
//        rewardedAdLoader?.setAdLoadListener(null)
//        rewardedAdLoader = null
//        rewardedAd?.setAdEventListener(null)
//        rewardedAd = null
//
//    }
//}
