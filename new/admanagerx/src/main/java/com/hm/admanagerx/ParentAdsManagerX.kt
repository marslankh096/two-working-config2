package com.hm.admanagerx

import android.app.Application
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.rewarded.RewardItem
import com.hm.admanagerx.adsanalysis.AdAnalyticsTracker
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

open class ParentAdsManagerX {

    protected lateinit var application: Application

    protected fun loadFullScreenAdWithTypeCheck(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)?,
        onAdFailed: ((String) -> Unit)?,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
        when (adConfig.adType) {
            INTER_AD -> {
//                if (application.isRussian()) {
//                    loadYandexInterAdX(
//                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
//                    )
//                } else {
                    loadInterAdX(
                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
                    )
//                }
            }

            REWARDED_AD -> {
//                if (application.isRussian()) {
//                    loadYandexRewardedAdX(
//                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
//                    )
//                } else {
                    loadRewardedAdX(
                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
                    )
//                }
            }

            APP_OPEN_AD -> {
//                if (application.isRussian()) {
//                    loadYandexAppOpenAdX(
//                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
//                    )
//                } else {
                    loadAppOpenAdX(
                        lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed, onAdRequestDenied
                    )
//                }
            }
        }
    }

    protected fun loadInScreenAdWithTypeCheck(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        adContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
        when (adConfig.adType) {
            BANNER_AD -> {
//                if (application.isRussian()) {
//                    loadYandexBannerAdX(
//                        lifecycleOwner,
//                        adConfigManager,
//                        adContainer,
//                        onAdLoaded,
//                        onAdFailed,
//                        onAdClicked,
//                        onAdImpression,
//                        onAdRequestDenied
//                    )
//                } else {
                    loadBannerAdX(
                        lifecycleOwner,
                        adConfigManager,
                        adContainer,
                        onAdLoaded,
                        onAdFailed,
                        onAdClicked,
                        onAdImpression,
                        onAdRequestDenied
                    )
//                }
            }

            NATIVE_AD -> {
//                if (application.isRussian()) {
                  /*  if (adConfig.bannerAdSize != null) {
                        loadYandexBannerAdX(
                            lifecycleOwner,
                            adConfigManager,
                            adContainer,
                            onAdLoaded,
                            onAdFailed,
                            onAdClicked,
                            onAdImpression,
                            onAdRequestDenied
                        )
                    } else {
                        loadYandexNativeAdX(
                            lifecycleOwner,
                            adConfigManager,
                            adContainer,
                            onAdLoaded,
                            onAdFailed,
                            onAdClicked,
                            onAdImpression,
                            onAdRequestDenied
                        )
                    }*/
//                } else {
                    loadNativeAdX(
                        lifecycleOwner,
                        adConfigManager,
                        adContainer,
                        onAdLoaded,
                        onAdFailed,
                        onAdClicked,
                        onAdImpression,
                        onAdRequestDenied
                    )
//                }
            }

        }

    }


    protected fun showFullScreenAdWithTypeCheck(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)?,
        onAdShow: (() -> Unit)?,
        onAdClicked: (() -> Unit)?,
        onAdImpression: (() -> Unit)?,
        onUserEarnedRewarded: ((Any) -> Unit)? = null,//only for rewarded ad
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)?,
    ) {

        val adType = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType
        when (adType) {
            INTER_AD -> {
                Log.d("TAG", "------->showFullScreenAdWithTypeCheck: before check")
//                if (application.isRussian()) {
//                    showYandexInterAdX(
//                        activity = activity,
//                        adConfigManager = adConfigManager,
//                        onAdClose = onAdClose,
//                        onAdShow = onAdShow,
//                        onAdClicked = onAdClicked,
//                        onAdImpression = onAdImpression,
//                        onLoadingShowHide = onLoadingShowHide,
//                        funBlock = funBlock)
//                } else {
                    showInterAdX(
                        activity = activity,
                        adConfigManager = adConfigManager,
                        onAdClose = onAdClose,
                        onAdShow = onAdShow,
                        onAdClicked = onAdClicked,
                        onAdImpression = onAdImpression,
                        onLoadingShowHide = onLoadingShowHide,
                        funBlock = funBlock
                    )
               // }
            }

            APP_OPEN_AD -> {
//                if (application.isRussian()) {
//                    showYandexAppOpenAdX(
//                        activity = activity,
//                        adConfigManager = adConfigManager,
//                        onAdClose = onAdClose,
//                        onAdShow = onAdShow,
//                        onAdClicked = onAdClicked,
//                        onAdImpression = onAdImpression,
//                        onLoadingShowHide = onLoadingShowHide,
//                        funBlock = funBlock
//                    )
//                } else {
                    showAppOpenAdX(
                        activity = activity,
                        adConfigManager = adConfigManager,
                        onAdClose = onAdClose,
                        onAdShow = onAdShow,
                        onAdClicked = onAdClicked,
                        onAdImpression = onAdImpression,
                        onLoadingShowHide = onLoadingShowHide,
                        funBlock = funBlock
                    )
//                }
            }

            REWARDED_AD -> {
//                if (application.isRussian()) {
//                    showYandexRewardedAdX(
//                        activity = activity,
//                        adConfigManager = adConfigManager,
//                        onAdClose = onAdClose,
//                        onAdShow = onAdShow,
//                        onAdClicked = onAdClicked,
//                        onAdImpression = onAdImpression,
//                        onUserEarnedRewarded = onUserEarnedRewarded,
//                        onLoadingShowHide = onLoadingShowHide,
//                        funBlock = funBlock
//                    )
//                } else {
                    showRewardedAdX(
                        activity = activity,
                        adConfigManager = adConfigManager,
                        onAdClose = onAdClose,
                        onAdShow = onAdShow,
                        onAdClicked = onAdClicked,
                        onAdImpression = onAdImpression,
                        onUserEarnedRewarded = onUserEarnedRewarded,
                        onLoadingShowHide = onLoadingShowHide,
                        funBlock = funBlock
                    )
//                }
            }
        }
    }
    protected fun isFullScreenAdWithTypeLoaded(adConfigManager: AdConfigManager): Boolean {
        val adType = adConfigManager.adConfig.adType
        return  when (adType) {
            INTER_AD -> interAdList[adConfigManager.name]?.isAdLoaded() ?: false
            REWARDED_AD -> rewardedAdList[adConfigManager.name]?.isAdLoaded() ?: false
            APP_OPEN_AD -> appOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
            else -> false
        }


//        if (application.isRussian()) {
//            when (adType) {
//                INTER_AD -> yandexInterAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                REWARDED_AD -> yandexRewardedAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                APP_OPEN_AD -> yandexAppOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                else -> false
//            }
//        }else{
//            when (adType) {
//                INTER_AD -> interAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                REWARDED_AD -> rewardedAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                APP_OPEN_AD -> appOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                else -> false
//            }
//        }
    }

    protected fun destroyFullScreenAdWithType(adConfigManager: AdConfigManager) {
        val adType = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType
        when (adType) {
            INTER_AD -> {
//                if (application.isRussian()) {
//                    destroyYandexInterAdX(adConfigManager)
//                } else {
                    destroyInterAdX(adConfigManager)
               // }
            }

            APP_OPEN_AD -> {
//                if (application.isRussian()) {
//                    destroyYandexAppOpenAdX(adConfigManager)
//                } else {
                    destroyAppOpenAdX(adConfigManager)
//                }
            }

            REWARDED_AD -> {
//                if (application.isRussian()) {
//                    destroyYandexRewardedAdX(adConfigManager)
//                } else {
                    destroyRewardedAdX(adConfigManager)
//                }
            }
        }
    }

    protected fun reloadFullScreenAdWithType(adConfigManager: AdConfigManager) {
        val adType = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType
        when (adType) {
            INTER_AD -> {
//                if (application.isRussian()) {
//                    reloadYandexInterAdX(adConfigManager)
//                } else {
                    reloadInterAdX(adConfigManager)
//                }
            }

            APP_OPEN_AD -> {
//                if (application.isRussian()) {
//                    reloadYandexAppOpenAdX(adConfigManager)
//                } else {
                    reloadAppOpenAdX(adConfigManager)
             //   }
            }

            REWARDED_AD -> {
//                if (application.isRussian()) {
//                    reloadYandexRewardedAdX(adConfigManager)
//                } else {
                    reloadRewardedAdX(adConfigManager)
             //   }
            }
        }
    }


    /** ----- Inter Ad --------**/
    protected val interAdList: ConcurrentHashMap<String, InterAdLoaderX?> = ConcurrentHashMap()

    /** ----- Yandex Ad --------**/
//    protected val yandexInterAdList: ConcurrentHashMap<String, YandexInterAdx?> =
//        ConcurrentHashMap()


    protected fun loadInterAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {

            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var interAdLoaderX = interAdList[adConfigManager.name]
            if (interAdLoaderX == null) interAdLoaderX = InterAdLoaderX(application)

            interAdLoaderX.loadInterAdX(
                adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() },
            )
            interAdList[adConfigManager.name] = interAdLoaderX
        })

    }

    protected fun showInterAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        Log.d("TAG", "------->showFullScreenAdWithTypeCheck: before show $activity")
        interAdList[adConfigManager.name]?.showAd(activity = activity,
            onAdClose = activity.listenerX {
                onAdClose?.invoke()
            },
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            funBlock = activity.listenerX { funBlock?.invoke() })
            ?: run {
                funBlock?.invoke()
                Log.d("TAG", "------->showFullScreenAdWithTypeCheck: ad null shown")
            }
    }


    protected fun destroyInterAdX(adConfigManager: AdConfigManager) =
        interAdList.remove(adConfigManager.name)

    protected fun reloadInterAdX(adConfigManager: AdConfigManager) =
        interAdList[adConfigManager.name]?.reloadAd()


    /**--------Yandex InterAd Loading-------*/
  /*  protected fun loadYandexInterAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {

            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var yandexInterAdLoaderX = yandexInterAdList[adConfigManager.name]
            if (yandexInterAdLoaderX == null) yandexInterAdLoaderX = YandexInterAdx(application)

            yandexInterAdLoaderX.loadYandexInterAdX(
                adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() },
            )
            yandexInterAdList[adConfigManager.name] = yandexInterAdLoaderX
        })

    }*/

   /* protected fun showYandexInterAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        yandexInterAdList[adConfigManager.name]?.showYandexInterAd(activity = activity,
            onAdClose = activity.listenerX {
                onAdClose?.invoke()
            },
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            funBlock = activity.listenerX { funBlock?.invoke() }) ?: run { funBlock?.invoke() }
    }


    protected fun destroyYandexInterAdX(adConfigManager: AdConfigManager) =
        yandexInterAdList.remove(adConfigManager.name)

    protected fun reloadYandexInterAdX(adConfigManager: AdConfigManager) =
        yandexInterAdList[adConfigManager.name]?.reloadAd()
*/

    /** ----- Native Ad --------**/

    protected val nativeAdList: ConcurrentHashMap<String, NativeAdLoaderX?> = ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
    protected fun loadNativeAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        nativeAdContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {

        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            Log.d("TAG", "loadNativeAdX: $adConfig")
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var nativeAdLoaderX = nativeAdList[adConfigManager.name]

            // clear ad container (FrameLayout) on activity destroy to avoid memory leak
            clearAdContainer(nativeAdContainer, lifecycleOwner)

            if (nativeAdLoaderX == null) nativeAdLoaderX = NativeAdLoaderX(application)

            // Show loading before ad
            if (nativeAdContainer?.childCount == 0) {
                if (!application.adCheckShow(adConfig) && adConfig.isShowLoadingBeforeAd && !nativeAdLoaderX.isAdImpression) {
                    application.showAdLoadingShimmerView(nativeAdContainer, adConfig.nativeAdLayout)
                }
            }

            nativeAdLoaderX.loadNativeAdX(adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX {
                    onAdLoaded?.invoke()
                    nativeAdLoaderX.showNativeAd(nativeAdContainer)
                },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdClicked = lifecycleOwner.listenerX { onAdClicked?.invoke() },
                onAdImpression = lifecycleOwner.listenerX {
                    onAdImpression?.invoke()
                    if (adConfig.isAdLoadAgain) nativeAdLoaderX.reloadAd()
                },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })

            nativeAdList[adConfigManager.name] = nativeAdLoaderX
        })
    }


    protected fun showNativeAdX(adConfigManager: AdConfigManager, nativeAdContainer: FrameLayout?) {
        nativeAdList[adConfigManager.name]?.showNativeAd(nativeAdContainer)
    }


    protected fun destroyNativeAdX(adConfigManager: AdConfigManager) {
        nativeAdList[adConfigManager.name]?.destroyNative()
        nativeAdList.remove(adConfigManager.name)
    }

    /** ----- Yandex Native Ad --------**/

//    protected val yandexNativeAdList: ConcurrentHashMap<String, YandexNativeAdX?> =
//        ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
   /* protected fun loadYandexNativeAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        nativeAdContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {

        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var nativeAdLoaderX = yandexNativeAdList[adConfigManager.name]

            // clear ad container (FrameLayout) on activity destroy to avoid memory leak
            clearAdContainer(nativeAdContainer, lifecycleOwner)

            if (nativeAdLoaderX == null) nativeAdLoaderX = YandexNativeAdX(application)

            // Show loading before ad
            if (!application.adCheckShow(adConfig) && nativeAdContainer != null && adConfig.isShowLoadingBeforeAd && !nativeAdLoaderX.isAdImpression) application.showAdLoadingShimmerView(
                nativeAdContainer,
                adConfig.nativeAdLayout
            )

            nativeAdLoaderX.loadYandexNativeAdX(adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX {
                    onAdLoaded?.invoke()
                    nativeAdLoaderX.showYandexNativeAd(nativeAdContainer)
                },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdClicked = lifecycleOwner.listenerX { onAdClicked?.invoke() },
                onAdImpression = lifecycleOwner.listenerX {
                    onAdImpression?.invoke()
                    if (adConfig.isAdLoadAgain) nativeAdLoaderX.reloadYandexNativeAd()
                },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })

            yandexNativeAdList[adConfigManager.name] = nativeAdLoaderX
        })
    }


    protected fun showYandexNativeAdX(
        adConfigManager: AdConfigManager, nativeAdContainer: FrameLayout?
    ) {
        yandexNativeAdList[adConfigManager.name]?.showYandexNativeAd(nativeAdContainer)
    }


    protected fun destroyYandexNativeAdX(adConfigManager: AdConfigManager) {
        yandexNativeAdList[adConfigManager.name]?.destroyNative()
        yandexNativeAdList.remove(adConfigManager.name)

    }*/


    /** ----- Banner Ad --------**/

    protected val bannerAdList: ConcurrentHashMap<String, BannerAdsLoaderX?> = ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
    protected fun loadBannerAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        bannerAdContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,

        ) {

        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }
            // clear ad container (FrameLayout) on activity destroy to avoid memory leak
            clearAdContainer(bannerAdContainer, lifecycleOwner)
            // Show loading before ad
            if (!application.adCheckShow(adConfig) && bannerAdContainer != null && adConfig.isShowLoadingBeforeAd) application.showAdLoadingShimmerView(
                bannerAdContainer, adConfig.bannerAdLoadingLayout
            )

            var bannerAdsLoaderX = bannerAdList[adConfigManager.name]

            if (bannerAdsLoaderX == null) bannerAdsLoaderX = BannerAdsLoaderX(application)

            var bannerAdSize = adConfig.bannerAdSize
            if (bannerAdSize == null) bannerAdSize =
                application.getAdaptiveAdSize(bannerAdContainer?.width ?: 0)

            bannerAdsLoaderX.loadBannerAdX(adConfigManager = adConfigManager,
                adSize = bannerAdSize as AdSize,
                onAdLoaded = lifecycleOwner.listenerX {
                    bannerAdsLoaderX.showBannerAd(bannerAdContainer)
                    onAdLoaded?.invoke()
                },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdClicked = lifecycleOwner.listenerX { onAdClicked?.invoke() },
                onAdImpression = lifecycleOwner.listenerX { onAdImpression?.invoke() },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })
            bannerAdList[adConfigManager.name] = bannerAdsLoaderX
        })
    }

    protected fun showBannerAdX(adConfigManager: AdConfigManager, bannerAdContainer: FrameLayout?) {
        bannerAdList[adConfigManager.name]?.showBannerAd(bannerAdContainer)
    }


    protected fun destroyBannerX(adConfigManager: AdConfigManager) {

        bannerAdList[adConfigManager.name]?.destroyAd()
        bannerAdList.remove(adConfigManager.name)

    }


    /** -----Yandex Banner Ad --------**/

//    protected val yandexBannerAdList: ConcurrentHashMap<String, YandexBannerAdX?> =
//        ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
//    protected fun loadYandexBannerAdX(
//        lifecycleOwner: LifecycleOwner,
//        adConfigManager: AdConfigManager,
//        bannerAdContainer: FrameLayout?,
//        onAdLoaded: (() -> Unit)? = null,
//        onAdFailed: ((String) -> Unit)? = null,
//        onAdClicked: (() -> Unit)? = null,
//        onAdImpression: (() -> Unit)? = null,
//        onAdRequestDenied: (() -> Unit)? = null,
//
//        ) {
//
//        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
//            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//            if (!adConfig.isAdShow) {
//                onAdRequestDenied?.invoke()
//                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
//                return@listenerX
//            }
//            // clear ad container (FrameLayout) on activity destroy to avoid memory leak
//            clearAdContainer(bannerAdContainer, lifecycleOwner)
//            // Show loading before ad
//            if (!application.adCheckShow(adConfig) && bannerAdContainer != null && adConfig.isShowLoadingBeforeAd) application.showAdLoadingShimmerView(
//                bannerAdContainer, adConfig.bannerAdLoadingLayout
//            )
//
//            var bannerAdsLoaderX = yandexBannerAdList[adConfigManager.name]
//
//            if (bannerAdsLoaderX == null) bannerAdsLoaderX = YandexBannerAdX(application)
//
//            val bannerAdSizeExisting = adConfig.bannerAdSize
//
//            val bannerAdSize = bannerAdContainer?.let {
//                if (bannerAdSizeExisting != null) {
//                    if (bannerAdSizeExisting == AdSize.LARGE_BANNER) {
//                        bannerAdContainer?.getYandexStickyAdSize()
//                    } else {
//                        bannerAdContainer?.getYandexInlineAdSize()
//                    }
//                } else {
//                    bannerAdContainer?.getYandexInlineAdSize()
//                }
//            } ?: run {
//                BannerAdSize.stickySize(application, 350)
//            }
//
//            bannerAdsLoaderX.loadYandexBannerAdX(adConfigManager = adConfigManager,
//                adSize = bannerAdSize,
//                onAdLoaded = lifecycleOwner.listenerX {
//                    bannerAdContainer?.let {
//                        bannerAdsLoaderX.showYandexBannerAd(bannerAdContainer)
//                    }
//                    onAdLoaded?.invoke()
//                },
//                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
//                onAdClicked = lifecycleOwner.listenerX { onAdClicked?.invoke() },
//                onAdImpression = lifecycleOwner.listenerX { onAdImpression?.invoke() },
//                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })
//            yandexBannerAdList[adConfigManager.name] = bannerAdsLoaderX
//        })
//    }

//    protected fun showYandexBannerAdX(
//        adConfigManager: AdConfigManager, bannerAdContainer: FrameLayout?
//    ) {
//        yandexBannerAdList[adConfigManager.name]?.showYandexBannerAd(
//            bannerAdContainer
//        )
//    }


//    protected fun destroyYandexBannerX(adConfigManager: AdConfigManager) {
//
//        yandexBannerAdList[adConfigManager.name]?.destroyAd()
//        yandexBannerAdList.remove(adConfigManager.name)
//
//    }

    /** ----- Rewarded Ad --------**/

    protected val rewardedAdList: ConcurrentHashMap<String, RewardedAdLoaderX?> =
        ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
    protected fun loadRewardedAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var rewardedAdLoaderX = rewardedAdList[adConfigManager.name]

            if (rewardedAdLoaderX == null) rewardedAdLoaderX = RewardedAdLoaderX(application)

            rewardedAdLoaderX.loadRewardedAdX(
                adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() },
            )
            rewardedAdList[adConfigManager.name] = rewardedAdLoaderX
        })
    }

    protected fun showRewardedAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onUserEarnedRewarded: ((RewardItem) -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        rewardedAdList[adConfigManager.name]?.showAd(activity = activity,
            onAdClose = activity.listenerX { onAdClose?.invoke() },
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            onUserEarnedRewarded = activity.listenerX { onUserEarnedRewarded?.invoke(it) },
            funBlock = activity.listenerX { funBlock?.invoke() }) ?: run { funBlock?.invoke() }
    }


    protected fun destroyRewardedAdX(adConfigManager: AdConfigManager) =
        rewardedAdList.remove(adConfigManager.name)


    protected fun reloadRewardedAdX(adConfigManager: AdConfigManager) =
        rewardedAdList[adConfigManager.name]?.reloadAd()


    /** ----- Yandex Rewarded Ad --------**/

//    protected val yandexRewardedAdList: ConcurrentHashMap<String, YandexRewardedAdX?> =
//        ConcurrentHashMap()


    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
 /*   protected fun loadYandexRewardedAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var rewardedAdLoaderX = yandexRewardedAdList[adConfigManager.name]

            if (rewardedAdLoaderX == null) rewardedAdLoaderX = YandexRewardedAdX(application)

            rewardedAdLoaderX.loadRewardedAdX(
                adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() },
            )
            yandexRewardedAdList[adConfigManager.name] = rewardedAdLoaderX
        })
    }*/

   /* protected fun showYandexRewardedAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onUserEarnedRewarded: ((Reward) -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        yandexRewardedAdList[adConfigManager.name]?.showAd(activity = activity,
            onAdClose = activity.listenerX { onAdClose?.invoke() },
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            onUserEarnedRewarded = activity.listenerX { onUserEarnedRewarded?.invoke(it) },
            funBlock = activity.listenerX { funBlock?.invoke() }) ?: run { funBlock?.invoke() }
    }
*/

  /*  protected fun destroyYandexRewardedAdX(adConfigManager: AdConfigManager) {
        yandexRewardedAdList[adConfigManager.name]?.destroyRewardedAd()
        yandexRewardedAdList.remove(adConfigManager.name)
    }


    protected fun reloadYandexRewardedAdX(adConfigManager: AdConfigManager) {
        yandexRewardedAdList[adConfigManager.name]?.reloadAd()
    }*/

    protected val appOpenAdList: ConcurrentHashMap<String, AppOpenAdXApp?> = ConcurrentHashMap()
  /*  protected val yandexAppOpenAdList: ConcurrentHashMap<String, YandexAppOpenAd?> =
        ConcurrentHashMap()
*/
    /** ----- AppOpen Ad --------**/


    protected fun loadAppOpenAdX(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {

            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
            if (!adConfig.isAdShow) {
                onAdRequestDenied?.invoke()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
                return@listenerX
            }

            var appOpenAdX = appOpenAdList[adConfigManager.name]

            if (appOpenAdX == null) appOpenAdX = AppOpenAdXApp(application)

            appOpenAdList[adConfigManager.name] = appOpenAdX
            appOpenAdX.loadAppOpenAdX(adConfigManager = adConfigManager,
                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })
        })

    }


    protected fun showAppOpenAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdShow: (() -> Unit)? = null,
        onAdClose: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        appOpenAdList[adConfigManager.name]?.showAd(
            activity,
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClose = activity.listenerX {
                onAdClose?.invoke()
                                           },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            funBlock = activity.listenerX { funBlock?.invoke() },
        ) ?: run { funBlock?.invoke() }
    }


    protected fun destroyAppOpenAdX(adConfigManager: AdConfigManager) {
        val newAdConfigManager =
            appOpenAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
        destroyFullScreenAdWithType(newAdConfigManager)
    }


    protected fun reloadAppOpenAdX(adConfigManager: AdConfigManager) {
        //TODO pending functionality
    }

    /** ----- Yandex AppOpen Ad --------**/


//    protected fun loadYandexAppOpenAdX(
//        lifecycleOwner: LifecycleOwner,
//        adConfigManager: AdConfigManager,
//        onAdLoaded: (() -> Unit)? = null,
//        onAdFailed: ((String) -> Unit)? = null,
//        onAdRequestDenied: (() -> Unit)? = null,
//    ) {
//        checkMobileAdsInit(adConfigManager, lifecycleOwner.listenerX {
//
//            val adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//            if (!adConfig.isAdShow) {
//                onAdRequestDenied?.invoke()
//                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt()
//                return@listenerX
//            }
//
//            var yandexAppOpenAdX = yandexAppOpenAdList[adConfigManager.name]
//
//            if (yandexAppOpenAdX == null) yandexAppOpenAdX = YandexAppOpenAd(application)
//
//            yandexAppOpenAdList[adConfigManager.name] = yandexAppOpenAdX
//            yandexAppOpenAdX.loadYandexAppOpenAd(adConfigManager = adConfigManager,
//                onAdLoaded = lifecycleOwner.listenerX { onAdLoaded?.invoke() },
//                onAdFailed = lifecycleOwner.listenerX { onAdFailed?.invoke(it) },
//                onAdRequestDenied = lifecycleOwner.listenerX { onAdRequestDenied?.invoke() })
//        })
//
//    }


/*    protected fun showYandexAppOpenAdX(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdShow: (() -> Unit)? = null,
        onAdClose: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        yandexAppOpenAdList[adConfigManager.name]?.showYandexAppOpenAd(
            activity,
            onAdShow = activity.listenerX { onAdShow?.invoke() },
            onAdClose = activity.listenerX { onAdClose?.invoke() },
            onAdImpression = activity.listenerX { onAdImpression?.invoke() },
            onAdClicked = activity.listenerX { onAdClicked?.invoke() },
            funBlock = activity.listenerX { funBlock?.invoke() },
        ) ?: run { funBlock?.invoke() }
    }


    protected fun destroyYandexAppOpenAdX(adConfigManager: AdConfigManager) {
        yandexAppOpenAdList.remove(adConfigManager.name)
    }*/


    protected fun reloadYandexAppOpenAdX(adConfigManager: AdConfigManager) {
        //TODO pending functionality
    }


    /** ----- Ad destroy functions--------**/
    fun destroyAllAds() {
        destroyAllNativeAds()
        destroyAllBannerAds()
        destroyAllInterAds()
        destroyAllRewardedAds()
        destroyAllAppOpenAd()
    }

    fun destroyAllRewardedAds() {
        rewardedAdList.clear()
        //yandexRewardedAdList.clear()
    }

    fun destroyAllInterAds() {
        interAdList.clear()
      //  yandexInterAdList.clear()
    }

    fun destroyAllBannerAds() {
        bannerAdList.apply {
            forEach { it.value?.destroyAd() }
            clear()
        }
//        yandexBannerAdList.apply {
//            forEach { it.value?.destroyAd() }
//            clear()
//        }
    }

    fun destroyAllAppOpenAd() {
        appOpenAdList.apply {
            forEach { it.value?.destroyAd() }
            clear()
        }
//        yandexAppOpenAdList.apply {
//            forEach { it.value?.destroyAd() }
//            clear()
//        }
    }

    fun destroyAllNativeAds() {
        nativeAdList.apply {
            forEach { it.value?.destroyNative() }
            clear()
        }
//        yandexNativeAdList.apply {
//            forEach { it.value?.destroyNative() }
//            clear()
//        }
    }

    /** ----- Ad analysis functions--------**/


    fun bannerAdAnalysis(adConfigManager: AdConfigManager): AdAnalyticsTracker? =
        bannerAdList[adConfigManager.name]?.adAnalyticsTracker

    fun nativeAdAnalysis(adConfigManager: AdConfigManager): AdAnalyticsTracker? =
        nativeAdList[adConfigManager.name]?.adAnalyticsTracker

    fun interAdAnalysis(adConfigManager: AdConfigManager): AdAnalyticsTracker? =
        interAdList[adConfigManager.name]?.adAnalyticsTracker

    fun rewardedAdAnalysis(adConfigManager: AdConfigManager): AdAnalyticsTracker? =
        rewardedAdList[adConfigManager.name]?.adAnalyticsTracker

    /*fun appOpenAdAnalysis(adConfigManager: AdConfigManager): AdAnalyticsTracker? =
        appOpenAdList[adConfigManager.name]?.adAnalyticsTracker*/

    protected val adsAnalyticsTrackerList: ArrayList<AdAnalyticsTracker> = ArrayList()

    fun getAllAdsAnalysis() = adsAnalyticsTrackerList.apply {
        adsAnalyticsTrackerList.clear()
        addAll(bannerAdList.mapNotNull { it.value?.adAnalyticsTracker })
        addAll(nativeAdList.mapNotNull { it.value?.adAnalyticsTracker })
        addAll(interAdList.mapNotNull { it.value?.adAnalyticsTracker })
        addAll(rewardedAdList.mapNotNull { it.value?.adAnalyticsTracker })
        /*  addAll(appOpenAdList.mapNotNull { it.value?.adAnalyticsTracker })*/
    }

    fun printAdsAnalyticsTrackerReport() = getAllAdsAnalysis().forEach { it.toString().printIt() }


}