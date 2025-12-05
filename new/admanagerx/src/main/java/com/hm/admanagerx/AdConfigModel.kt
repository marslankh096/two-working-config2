package com.hm.admanagerx

import android.util.Log
import androidx.annotation.Keep
import com.google.android.gms.ads.AdSize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson

const val BANNER_AD = "banner"
const val NATIVE_AD = "native"
const val INTER_AD = "inter"
const val REWARDED_AD = "rewarded"
const val APP_OPEN_AD = "app_open"

/** Firebase remote config json template */
/*
{
  "adType": "native",
  "adId": "ca-app-pub-3940256099942544/2247696110",
  "isAdShow": true,
  "isShowLoadingBeforeAd": true,
  "beforeAdLoadingTimeInMs": 1500,
  "isAdLoadAgain": false,
  "fullScreenAdCount": 0,
  "fullScreenAdLoadOnCount": 0
  "fullScreenAdSessionCount": 0
}
*/

/**
 * remote (json) ad configuration data class
 */
@Keep
data class RemoteAdConfig(
    var isAdShow: Boolean? = null,// show or hide ad
    var adId: String? = null, // ad id : Any - Int (ad id from local string ref) String (ad id from remote config )
    var adIdYandex: Any?="", // ad id : Any - Int (ad id from local string ref) String (ad id from remote config )
    var adType: String? = null, // change ad type remotely, banner ,  native , inter , rewarded ,  app_open
    var isShowLoadingBeforeAd: Boolean? = null,// show loading view before showing ad
    var beforeAdLoadingTimeInMs: Long? = null, // before ad loading time
    var isAdLoadAgain: Boolean? = null, // if true,send new ad request again after show
    var fullScreenAdCount: Long? = null,// Remote config count for ad control , for inter ad / rewarded ad only , 0 = no limit
    var fullScreenAdLoadOnCount: Long? = null,// LoadAd load on specific count to improve show rate, 0 = no effect , -1 = second last count load ad position  , other value = specific load position
    var fullScreenAdSessionCount: Long? = null,// Total ads show in per session , 0 = no limit

)

/**
 * local ad configuration class
 */
@Keep
data class AdConfig(
    var isAdShow: Boolean = true, // show or hide ad
    var adId: Any = "", // ad id : Any - Int (ad id from local string ref) String (ad id from remote config )
    var adIdYandex: Any="",  // ad id : Any - Int (ad id from local string ref) String (ad id from remote config )
    var adType: String = "",// change ad type remotely, banner ,  native , inter , rewarded ,  app_open
    var isShowLoadingBeforeAd: Boolean = false,// show loading view before showing ad
    var beforeAdLoadingTimeInMs: Long = 1500, // before ad loading time
    var isAdLoadAgain: Boolean = false, // if true,send new ad request again after show
    var fullScreenAdCount: Long = 0,// Remote config count for ad control , for inter ad / rewarded ad only , 0 = no limit
    var fullScreenAdLoadOnCount: Long = 0,// LoadAd load on specific count to improve show rate, 0 = no effect , -1 = second last count load ad position  , other value = specific load position
    var fullScreenAdSessionCount: Long = 0,// Total ads show in per session , 0 = no limit
    var isAppOpenAdAppLevel : Boolean = false,

    var fullScreenAdLoadingLayout: Int = R.layout.ad_loading_view,// Full screen AD loading Layout
    var nativeAdLayout: Int = R.layout.native_add_banner_view, // native ad layout , only for native Ad
    var bannerAdSize: AdSize? = null, // default value is null means its auto get adaptive AdSize
    var bannerAdLoadingLayout: Int = R.layout.banner_ad_loading_view, // before loading layout of banner ad
) {

    fun fetchAdConfigFromRemote(remoteConfigKey: String): AdConfig {
        val configJson = FirebaseRemoteConfig.getInstance().getString(remoteConfigKey)
        Log.d("TAG", "fetchAdConfigFromRemote:$configJson ")
        try {
            Gson().fromJson(configJson, RemoteAdConfig::class.java).let {
                adId = it.adId ?: adId
                adIdYandex = it.adIdYandex ?: adIdYandex
                isAdShow = it.isAdShow ?: isAdShow
                adType = it.adType ?: adType
                isShowLoadingBeforeAd = it.isShowLoadingBeforeAd ?: isShowLoadingBeforeAd
                beforeAdLoadingTimeInMs = it.beforeAdLoadingTimeInMs ?: beforeAdLoadingTimeInMs
                isAdLoadAgain = it.isAdLoadAgain ?: isAdLoadAgain
                fullScreenAdCount = it.fullScreenAdCount ?: fullScreenAdCount
                fullScreenAdLoadOnCount = updateFullscreenAdLoadOnCount(it.fullScreenAdLoadOnCount)
                fullScreenAdSessionCount = it.fullScreenAdSessionCount ?: fullScreenAdSessionCount
            }

        } catch (e: Exception) {
            e.message?.printIt()
//          if configJson is empty or invalid json , update fullScreenAdLoadOnCount base on default values
            fullScreenAdLoadOnCount = updateFullscreenAdLoadOnCount(null)
        }

        return this
    }

    private fun updateFullscreenAdLoadOnCount(remoteFullScreenAdLoadOnCount: Long?): Long {
        var loadOnCount = remoteFullScreenAdLoadOnCount ?: fullScreenAdLoadOnCount
        if (loadOnCount == -1L || loadOnCount > fullScreenAdCount) loadOnCount = fullScreenAdCount - 1
        return loadOnCount
    }

}
