package com.hm.admanagerx

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.NoSuchAlgorithmException

fun Context.getAdmobHashId(): String {
    try {
        // Create MD5 Hash
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val digest = java.security.MessageDigest.getInstance("MD5")
        digest.update(androidId.toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuilder()
        for (i in messageDigest.indices) {
            var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
            while (h.length < 2)
                h = "0$h"
            hexString.append(h)
        }
        return hexString.toString().uppercase()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""

}
object AdsManagerX : ParentAdsManagerX() {

    var isAppLevelAdsInitializationSuccess = false

    /**
     * isAppLevelAdsInitialization (Optional : because ANR reported on MobileAds.initialize())
     * onInitializeComplete : callback when Google Mobile Ads SDK or mediation partner SDKs initialization complete
     */
    fun init(
        application: Application,
        isAppLevelAdsInitialization: Boolean = false,
        onAdsSdkInitializeComplete: (() -> Unit)? = null,
    ): AdsManagerX {
        this.application = application
        isAppLevelAdsInitializationSuccess = false
        if (isAppLevelAdsInitialization) {

            if (BuildConfig.DEBUG) {
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(application.getAdmobHashId())
                    ).build()
                MobileAds.setRequestConfiguration(configuration);
            }

            CoroutineScope(Dispatchers.IO).launch {

//                val isRussian = TinyDB.getInstance(application).getBoolean(IS_RUSSIAN, false)
//                if (isRussian) {
//                    com.yandex.mobile.ads.common.MobileAds.initialize(application) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            isAppLevelAdsInitializationSuccess = true
//                            onAdsSdkInitializeComplete?.invoke()
//                            loadAdsInQueueAfterInit()
//                        }
//                    }
//                } else {
                   /* if(BuildConfig.DEBUG){
                        MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(listOf("BAD739319F4B122F1CD39026EC367EB7")).build())
                    }*/


                    MobileAds.initialize(application) {
                        Log.d("TAG", "init:app-onCreate ")
                        CoroutineScope(Dispatchers.Main).launch {
                            isAppLevelAdsInitializationSuccess = true
                            onAdsSdkInitializeComplete?.invoke()
                            loadAdsInQueueAfterInit()
                        }
                    }
               // }
            }
        } else {
            isAppLevelAdsInitializationSuccess = true
        }
        return this
    }

    fun initX(application: Application) {
        this.application = application
    }

    fun loadFirebaseRemoteConfig(onComplete: ((Boolean) -> Unit)? = null) =
        initFirebaseRemoteConfig(onComplete)

    /** ----- Inter Ad --------**/

    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
    fun loadInterAd(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {
        adConfigManager.adConfig.adType = INTER_AD
        loadFullScreenAdWithTypeCheck(
            lifecycleOwner,
            adConfigManager,
            onAdLoaded,
            onAdFailed,
            onAdRequestDenied
        )
    }


    fun showInterAd(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onUserEarnedRewarded: ((Any) -> Unit)? = null,//only for rewarded ad
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        val newAdConfigManager =
            interAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
        showFullScreenAdWithTypeCheck(
            activity = activity,
            adConfigManager = newAdConfigManager,
            onAdShow = onAdShow,
            onAdClose = onAdClose,
            onAdClicked = onAdClicked,
            onAdImpression = onAdImpression,
            onUserEarnedRewarded = onUserEarnedRewarded,
            onLoadingShowHide = onLoadingShowHide,
            funBlock = funBlock
        )
    }

    fun isInterAdLoaded(adConfigManager: AdConfigManager): Boolean {
        val newAdConfigManager =
            interAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
        return isFullScreenAdWithTypeLoaded(newAdConfigManager)
    }
  fun isAppOpenAdShowing(adConfigManager: AdConfigManager): Boolean {
        val newAdConfigManager =
            appOpenAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
        return appOpenAdList[newAdConfigManager.name]?.isShowingAd ?: false
    }

    fun getInterAd(adConfigManager: AdConfigManager) = interAdList[adConfigManager.name]


    fun destroyInterAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = INTER_AD
        destroyFullScreenAdWithType(adConfigManager)
    }


    fun reloadInterAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = INTER_AD
        reloadFullScreenAdWithType(adConfigManager)
    }


    /** ----- Native Ad --------**/

    fun loadNativeAd(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        bannerAdContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,
    ) {

//        adConfigManager.adConfig.adType = NATIVE_AD
        loadInScreenAdWithTypeCheck(
            lifecycleOwner,
            adConfigManager,
            bannerAdContainer,
            onAdLoaded,
            onAdFailed,
            onAdClicked,
            onAdImpression,
            onAdRequestDenied
        )

    }

    fun isNativeAdLoaded(adConfigManager: AdConfigManager): Boolean {
        val newAdConfigManager =
            bannerAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
//        return if (application.isRussian()) {
//            if (adConfigManager.adConfig.bannerAdSize != null) {
//                when (newAdConfigManager.adConfig.adType) {
//                    NATIVE_AD -> yandexBannerAdList[adConfigManager.name]?.isAdLoaded ?: false
//                    BANNER_AD -> yandexBannerAdList[adConfigManager.name]?.isAdLoaded ?: false
//                    else -> false
//                }
//            } else {
//                when (newAdConfigManager.adConfig.adType) {
//                    NATIVE_AD -> yandexNativeAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                    BANNER_AD -> yandexBannerAdList[adConfigManager.name]?.isAdLoaded ?: false
//                    else -> false
//                }
//            }
//
//        } else {
//            when (newAdConfigManager.adConfig.adType) {
//                NATIVE_AD -> nativeAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                BANNER_AD -> bannerAdList[adConfigManager.name]?.isAdLoaded ?: false
//                else -> false
//            }
//        }

        return when (newAdConfigManager.adConfig.adType) {
            NATIVE_AD -> nativeAdList[adConfigManager.name]?.isAdLoaded() ?: false
            BANNER_AD -> bannerAdList[adConfigManager.name]?.isAdLoaded ?: false
            else -> false
        }
    }

    fun getNativeAd(adConfigManager: AdConfigManager) = nativeAdList[adConfigManager.name]

    fun showNativeAd(adConfigManager: AdConfigManager, nativeAdContainer: FrameLayout?) {
        adConfigManager.adConfig.adType = NATIVE_AD
        when (adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType) {
            NATIVE_AD -> {
//                if (application.isRussian()) {
//                    if (adConfigManager.adConfig.bannerAdSize != null) {
//                        showYandexBannerAdX(adConfigManager, nativeAdContainer)
//                    } else {
//                        showYandexNativeAdX(adConfigManager, nativeAdContainer)
//                    }
//                } else {
                    showNativeAdX(adConfigManager, nativeAdContainer)
//                }
            }

            BANNER_AD -> {
//                if (application.isRussian()) {
//                    showYandexBannerAdX(adConfigManager, nativeAdContainer)
//                } else {
                    showBannerAdX(adConfigManager, nativeAdContainer)
//                }

            }
        }
    }


    fun reloadNativeAd(adConfigManager: AdConfigManager) {
        nativeAdList[adConfigManager.name]?.reloadAd()
    }


    fun destroyNativeAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = NATIVE_AD
        when (adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType) {
            NATIVE_AD ->
//                if (application.isRussian()) {
//                    if (adConfigManager.adConfig.bannerAdSize != null) {
//                        destroyYandexBannerX(adConfigManager)
//                    } else {
//                        destroyYandexNativeAdX(adConfigManager)
//                    }
//                } else {
                    destroyNativeAdX(adConfigManager)
//                }

            BANNER_AD -> {
//                if (application.isRussian()) {
//                    destroyYandexBannerX(adConfigManager)
//                } else {
                    destroyBannerX(adConfigManager)
//                }
            }
        }
    }


    /** ----- Banner Ad --------**/


    fun loadBannerAd(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        bannerAdContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,

        ) {
        adConfigManager.adConfig.adType = BANNER_AD

        loadInScreenAdWithTypeCheck(
            lifecycleOwner,
            adConfigManager,
            bannerAdContainer,
            onAdLoaded,
            onAdFailed,
            onAdClicked,
            onAdImpression,
            onAdRequestDenied
        )
    }


    fun getBannerAd(adConfigManager: AdConfigManager) = bannerAdList[adConfigManager.name]

    fun showBannerAd(adConfigManager: AdConfigManager, nativeAdContainer: FrameLayout?) {
        adConfigManager.adConfig.adType = BANNER_AD
        when (adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType) {
            NATIVE_AD -> {
//                if (application.isRussian()) {
//                    if (adConfigManager.adConfig.bannerAdSize != null) {
//                        showYandexBannerAdX(adConfigManager, nativeAdContainer)
//                    } else {
//                        showYandexNativeAdX(adConfigManager, nativeAdContainer)
//                    }
//                } else {
                    showNativeAdX(adConfigManager, nativeAdContainer)
//                }

            }

            BANNER_AD -> {
//                if (application.isRussian()) {
//                    showYandexBannerAdX(adConfigManager, nativeAdContainer)
//                } else {
                    showBannerAdX(adConfigManager, nativeAdContainer)
//                }

            }
        }
    }


    fun destroyBannerAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = BANNER_AD
        when (adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name).adType) {
            NATIVE_AD -> destroyNativeAdX(adConfigManager)
            BANNER_AD -> destroyBannerX(adConfigManager)
        }
    }

    //get YandexBanner
//    fun getYandexBannerAd(adConfigManager: AdConfigManager) =
//        yandexBannerAdList[adConfigManager.name]
    /** ----- Rewarded Ad --------**/

    /**
     * lifecycleOwner : use for set listenerX (LiveData Observer) callback or  get Activity / Fragment lifecycle methods
     */
    fun loadRewardedAd(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null,
        onAdRequestDenied: (() -> Unit)? = null,

        ) {
        adConfigManager.adConfig.adType = REWARDED_AD
        loadFullScreenAdWithTypeCheck(
            lifecycleOwner,
            adConfigManager,
            onAdLoaded,
            onAdFailed,
            onAdRequestDenied
        )
    }


    fun showRewardedAd(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onUserEarnedRewarded: ((Any) -> Unit)? = null,//only for rewarded ad
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        adConfigManager.adConfig.adType = REWARDED_AD
        showFullScreenAdWithTypeCheck(
            activity,
            adConfigManager,
            onAdClose,
            onAdShow,
            onAdClicked,
            onAdImpression,
            onUserEarnedRewarded,
            onLoadingShowHide,
            funBlock
        )
    }

    fun getRewardedAd(adConfigManager: AdConfigManager) = rewardedAdList[adConfigManager.name]

    fun destroyRewardedAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = REWARDED_AD
        destroyFullScreenAdWithType(adConfigManager)
    }


    fun reloadRewardedAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = REWARDED_AD
        reloadFullScreenAdWithType(adConfigManager)
    }


    /** ----- AppOpen Ad --------**/

    fun loadAppOpenAd(
        lifecycleOwner: LifecycleOwner,
        adConfigManager: AdConfigManager,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        adConfigManager.adConfig.adType = APP_OPEN_AD
        loadFullScreenAdWithTypeCheck(lifecycleOwner, adConfigManager, onAdLoaded, onAdFailed)
    }


    fun showAppOpenAd(
        activity: AppCompatActivity,
        adConfigManager: AdConfigManager,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onAdClicked: (() -> Unit)? = null,
        onAdImpression: (() -> Unit)? = null,
        onUserEarnedRewarded: ((Any) -> Unit)? = null,//only for rewarded ad
        onLoadingShowHide: ((Boolean) -> Unit)? = null,
        funBlock: (() -> Unit)? = null,
    ) {
        adConfigManager.adConfig.adType = APP_OPEN_AD
        showFullScreenAdWithTypeCheck(
            activity,
            adConfigManager,
            onAdClose,
            onAdShow,
            onAdClicked,
            onAdImpression,
            onUserEarnedRewarded,
            onLoadingShowHide,
            funBlock
        )
    }


    fun getAppOpenAd(adConfigManager: AdConfigManager) = appOpenAdList[adConfigManager.name]

    fun destroyAppOpenAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = APP_OPEN_AD
        destroyFullScreenAdWithType(adConfigManager)
    }


    fun reloadAppOpenAd(adConfigManager: AdConfigManager) {
        adConfigManager.adConfig.adType = APP_OPEN_AD
        reloadFullScreenAdWithType(adConfigManager)
    }

    fun isOpenAdLoaded(adConfigManager: AdConfigManager): Boolean {
        val newAdConfigManager = appOpenAdList[adConfigManager.name]?.getAdConfigManager() ?: adConfigManager
        return  when (newAdConfigManager.adConfig.adType) {
            APP_OPEN_AD -> appOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
            else -> false
        }
//        return if (application.isRussian()) {
//            when (newAdConfigManager.adConfig.adType) {
//                APP_OPEN_AD -> yandexAppOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                else -> false
//            }
//        } else {
//            when (newAdConfigManager.adConfig.adType) {
//                APP_OPEN_AD -> appOpenAdList[adConfigManager.name]?.isAdLoaded() ?: false
//                else -> false
//            }
//        }
    }

}