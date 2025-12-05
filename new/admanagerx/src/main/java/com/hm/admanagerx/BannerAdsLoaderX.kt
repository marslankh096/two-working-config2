package com.hm.admanagerx

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.hm.admanagerx.adsanalysis.AdAnalyticsTracker


class BannerAdsLoaderX(private var context: Context) {

    private var TAG = "BannerAdsLoaderX"

    private lateinit var mAdConfigManager: AdConfigManager
    private lateinit var adConfig: AdConfig

    var adView: AdView? = null

    var isAdLoaded: Boolean = false
    var isAdImpression: Boolean = false

    var onAdLoaded: MutableLiveData<AdView>? = null
    var onAdFailed: MutableLiveData<String>? = null
    var onAdClicked: MutableLiveData<Unit>? = null
    var onAdImpression: MutableLiveData<Unit>? = null
    var onAdRequestDenied: MutableLiveData<Unit>? = null
    var adSize: AdSize? = null

    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(mAdConfigManager.name) }
    fun getAdConfigManager() = mAdConfigManager
    fun loadBannerAdX(
        adConfigManager: AdConfigManager,
        adSize: AdSize?,
        onAdLoaded: MutableLiveData<AdView>? = null,
        onAdFailed: MutableLiveData<String>? = null,
        onAdClicked: MutableLiveData<Unit>? = null,
        onAdImpression: MutableLiveData<Unit>? = null,
        onAdRequestDenied: MutableLiveData<Unit>? = null,
    ) {

        this.mAdConfigManager = adConfigManager
        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)

        this.onAdLoaded = onAdLoaded
        this.onAdFailed = onAdFailed
        this.onAdClicked = onAdClicked
        this.onAdImpression = onAdImpression
        this.onAdRequestDenied = onAdRequestDenied

        this.adSize = adSize

        if (context.adCheckShow(adConfig) || isAdLoading()) {
            onAdRequestDenied?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(TAG)
            return
        }

        //if ad already loaded and not impression then trigger onAdLoaded for show native ad and return
        if (isAdLoaded) {
            if (isAdImpression) {
                onAdLoaded?.value = adView
            } else {
                onAdLoaded?.value = adView
                return
            }
        }

        //TODO check this

        /*   //if ad already loaded trigger onAdLoaded and return
           if (isAdLoaded) {
               onAdLoaded?.value = adView
               return
           }*/

        isAdImpression = false
        isAdLoaded = false

        "${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
        context.sendLogs("${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_request")

        adAnalyticsTracker.trackAdRequest()

        adView = AdView(context)
        adView?.setAdSize(this.adSize ?: AdSize.BANNER)
        adView?.adUnitId = context.getAdId(adConfig.adId)

        adView?.loadAd(AdRequest.Builder().build())

        adView?.adListener = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                isAdLoaded = true
                this@BannerAdsLoaderX.onAdLoaded?.value = adView
                "${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
                context.sendLogs("${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
                adAnalyticsTracker.trackAdLoaded()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                isAdLoaded = false
                this@BannerAdsLoaderX.onAdFailed?.value = p0.message
                "${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_Ad failed ${p0.message}".printIt(TAG)
                context.sendLogs("${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
                adAnalyticsTracker.trackAdLoadFailed()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                this@BannerAdsLoaderX.onAdClicked?.value = Unit
                "${mAdConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(TAG)
                context.sendLogs("${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
                adAnalyticsTracker.trackAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                this@BannerAdsLoaderX.onAdImpression?.value = Unit
                "${mAdConfigManager.name}_${adConfigManager.adConfig.adType} Ad Impression".printIt(TAG)
                context.sendLogs("${mAdConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
                isAdImpression = true
                adAnalyticsTracker.trackAdImpression()
            }
        }


    }

    fun isAdLoading() = adView?.isLoading ?: false

    fun destroyAd() = adView?.destroy()

    fun showBannerAd(bannerAdContainer: FrameLayout?) {

        if (context.adCheckShow(adConfig) || isAdLoading()) return

        if (adView == null) return

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //when ad is loaded already
        bannerAdContainer?.removeAllViews()
        adView?.parent?.let { (it as? ViewGroup)?.removeView(adView) }
        bannerAdContainer?.addView(adView, params)
        bannerAdContainer?.isVisible = true
        adAnalyticsTracker.trackAdShow()
    }
}








