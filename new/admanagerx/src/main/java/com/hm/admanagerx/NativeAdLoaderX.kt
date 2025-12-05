package com.hm.admanagerx

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.hm.admanagerx.adsanalysis.AdAnalyticsTracker


class NativeAdLoaderX(private var context: Context) {

    var mNativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null


    private var TAG = "NativeAdLoaderX"

    private lateinit var mAdConfigManager: AdConfigManager
    private lateinit var adConfig: AdConfig

    private var onAdLoaded: MutableLiveData<NativeAd>? = null
    private var onAdFailed: MutableLiveData<String>? = null
    private var onAdClicked: MutableLiveData<Unit>? = null
    private var onAdImpression: MutableLiveData<Unit>? = null
    private var onAdRequestDenied: MutableLiveData<Unit>? = null
    var isAdImpression = false

    fun isAdLoading() = adLoader?.isLoading ?: false
    fun isAdLoaded() = mNativeAd != null


    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(mAdConfigManager.name) }
    fun getAdConfigManager() = mAdConfigManager

    fun loadNativeAdX(
        adConfigManager: AdConfigManager,
        onAdLoaded: MutableLiveData<NativeAd>? = null,
        onAdFailed: MutableLiveData<String>? = null,
        onAdClicked: MutableLiveData<Unit>? = null,
        onAdImpression: MutableLiveData<Unit>? = null,
        onAdRequestDenied: MutableLiveData<Unit>? = null,
        isReloadRequest: Boolean = false,
    ) {

        mAdConfigManager = adConfigManager
        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
        this.onAdLoaded = onAdLoaded
        this.onAdFailed = onAdFailed
        this.onAdClicked = onAdClicked
        this.onAdImpression = onAdImpression
        this.onAdRequestDenied = onAdRequestDenied

        if (context.adCheckShow(adConfig) || isAdLoading()) {
            onAdRequestDenied?.value = Unit
            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(TAG)
            return
        }

        //if ad already loaded and not impression then trigger onAdLoaded for show native ad and return
        if (isAdLoaded() && !isReloadRequest) {
            if (isAdImpression) {
                onAdLoaded?.value = mNativeAd
            } else {
                onAdLoaded?.value = mNativeAd
                return
            }
        }
        isAdImpression = false


        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")

        adAnalyticsTracker.trackAdRequest()
        val nativeAdId = context.getAdId(adConfig.adId)

        adLoader = AdLoader.Builder(context, nativeAdId).forNativeAd { ad: NativeAd ->

            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
            context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
            mNativeAd?.destroy()
            mNativeAd = ad
            //avoid show ad if it is reload request
            if (!isReloadRequest) this.onAdLoaded?.value = ad

            adAnalyticsTracker.trackAdLoaded()

        }.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(adError: LoadAdError) {

                "${adConfigManager.name}_${adConfigManager.adConfig.adType} " + adError.toString().printIt(TAG)
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
                this@NativeAdLoaderX.onAdFailed?.value = adError.message

                adAnalyticsTracker.trackAdLoadFailed()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(TAG)
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")

                this@NativeAdLoaderX.onAdClicked?.value = Unit

                adAnalyticsTracker.trackAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
                "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdImpression".printIt(TAG)
                isAdImpression = true

                this@NativeAdLoaderX.onAdImpression?.value = Unit
                adAnalyticsTracker.trackAdImpression()
            }

        }).build()

        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun reloadAd() =
        loadNativeAdX(mAdConfigManager, onAdLoaded, onAdFailed, onAdClicked, onAdImpression, onAdRequestDenied, true)

    fun showNativeAd(nativeAdContainer: FrameLayout?) {

        if (mNativeAd == null || mAdConfigManager == null || nativeAdContainer == null) return

        val layoutView = LayoutInflater.from(nativeAdContainer.context).inflate(adConfig.nativeAdLayout, null, false)

        NativeAdView(nativeAdContainer.context).apply {
            addView(layoutView)
            val ad_media = layoutView.findViewById<MediaView>(R.id.ad_media)
            val ad_headline = layoutView.findViewById<TextView>(R.id.ad_headline)
            val ad_body = layoutView.findViewById<TextView>(R.id.ad_body)
            val ad_call_to_action = layoutView.findViewById<TextView>(R.id.ad_call_to_action)
            val ad_icon = layoutView.findViewById<ImageView>(R.id.ad_icon)

            ad_headline?.text = mNativeAd?.headline
            mNativeAd?.mediaContent?.let { ad_media?.mediaContent = it }

            if (mNativeAd?.body == null) {
                ad_body?.visibility = View.INVISIBLE
            } else {
                ad_body?.visibility = View.VISIBLE
                ad_body?.text = mNativeAd?.body
            }


            if (mNativeAd?.callToAction == null) {
                ad_call_to_action?.visibility = View.INVISIBLE
            } else {
                ad_call_to_action?.visibility = View.VISIBLE
                ad_call_to_action?.text = mNativeAd?.callToAction
            }

            if (mNativeAd?.icon == null) {
                ad_icon?.visibility = View.GONE
            } else {
                ad_icon?.setImageDrawable(mNativeAd?.icon?.drawable)
                ad_icon?.visibility = View.VISIBLE
            }

            mediaView = ad_media
            headlineView = ad_headline
            bodyView = ad_body
            callToActionView = ad_call_to_action
            iconView = ad_icon

            setNativeAd(mNativeAd!!)
        }.let {
            nativeAdContainer.removeAllViews()
            nativeAdContainer.addView(it)
            nativeAdContainer.isVisible = true
        }
    }


    fun destroyNative() {
        mNativeAd?.destroy()
        mNativeAd = null
    }
}







