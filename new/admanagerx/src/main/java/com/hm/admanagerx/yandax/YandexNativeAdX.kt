//package com.hm.admanagerx.old.yandax
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.widget.Button
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.core.view.isVisible
//import androidx.lifecycle.MutableLiveData
//import com.hm.admanagerx.old.AdConfig
//import com.hm.admanagerx.old.AdConfigManager
//import com.hm.admanagerx.old.R
//import com.hm.admanagerx.old.adCheckShow
//import com.hm.admanagerx.old.adsanalysis.AdAnalyticsTracker
//import com.hm.admanagerx.old.getAdId
//import com.hm.admanagerx.old.printIt
//import com.hm.admanagerx.old.sendLogs
//import com.yandex.mobile.ads.common.AdRequestError
//import com.yandex.mobile.ads.common.ImpressionData
//import com.yandex.mobile.ads.nativeads.MediaView
//import com.yandex.mobile.ads.nativeads.NativeAd
//import com.yandex.mobile.ads.nativeads.NativeAdEventListener
//import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
//import com.yandex.mobile.ads.nativeads.NativeAdLoader
//import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
//import com.yandex.mobile.ads.nativeads.NativeAdView
//import com.yandex.mobile.ads.nativeads.NativeAdViewBinder
//
//class YandexNativeAdX(private var context: Context) {
//    var mNativeAd: NativeAd? = null
//
//
//    private var TAG = "NativeAdLoaderX"
//
//    private lateinit var mAdConfigManager: AdConfigManager
//    private lateinit var adConfig: AdConfig
//
//    private var onNativeAdLoaded: MutableLiveData<NativeAd>? = null
//    private var onAdFailed: MutableLiveData<String>? = null
//    private var onAdClicked: MutableLiveData<Unit>? = null
//    private var onAdImpression: MutableLiveData<Unit>? = null
//    private var onAdRequestDenied: MutableLiveData<Unit>? = null
//    var isAdImpression = false
//    private var isAdLoading: Boolean = false
//
//    fun isAdLoading() = isAdLoading
//    fun isAdLoaded() = mNativeAd != null
//
//
//    val adAnalyticsTracker: AdAnalyticsTracker by lazy { AdAnalyticsTracker(mAdConfigManager.name) }
//
//    fun getAdConfigManager() = mAdConfigManager
//    fun loadYandexNativeAdX(
//        adConfigManager: AdConfigManager,
//        onAdLoaded: MutableLiveData<NativeAd>? = null,
//        onAdFailed: MutableLiveData<String>? = null,
//        onAdClicked: MutableLiveData<Unit>? = null,
//        onAdImpression: MutableLiveData<Unit>? = null,
//        onAdRequestDenied: MutableLiveData<Unit>? = null,
//        isReloadRequest: Boolean = false,
//    ) {
//
//        mAdConfigManager = adConfigManager
//        this.adConfig = adConfigManager.adConfig.fetchAdConfigFromRemote(adConfigManager.name)
//        this.onNativeAdLoaded = onAdLoaded
//        this.onAdFailed = onAdFailed
//        this.onAdClicked = onAdClicked
//        this.onAdImpression = onAdImpression
//        this.onAdRequestDenied = onAdRequestDenied
//
//        if (context.adCheckShow(adConfig) || isAdLoading()) {
//            onAdRequestDenied?.value = Unit
//            "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad request denied".printIt(TAG)
//            return
//        }
//
//        //if ad already loaded and not impression then trigger onAdLoaded for show native ad and return
//        if (isAdLoaded() && !isReloadRequest) {
//            if (isAdImpression) {
//                onAdLoaded?.value = mNativeAd
//            } else {
//                onAdLoaded?.value = mNativeAd
//                return
//            }
//        }
//        isAdImpression = false
//
//        if (isAdLoading) {
//            return
//        }
//        isAdLoading = true
//
//        "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded request".printIt(TAG)
//        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_request")
//
//        adAnalyticsTracker.trackAdRequest()
//        val nativeAdId = context.getAdId(adConfig.adIdYandex)
//        val adLoadListener = object : NativeAdLoadListener {
//            override fun onAdFailedToLoad(error: AdRequestError) {
//                isAdLoading = false
//
//                "${adConfigManager.name}_${adConfigManager.adConfig.adType} " + error.toString().printIt(TAG)
//                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_failed")
//                this@YandexNativeAdX.onAdFailed?.value = error.description
//
//                adAnalyticsTracker.trackAdLoadFailed()
//            }
//
//            override fun onAdLoaded(nativeAd: NativeAd) {
//                isAdLoading = false
//
//                "${adConfigManager.name}_${adConfigManager.adConfig.adType} Ad loaded".printIt(TAG)
//                context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_loaded")
//                mNativeAd=null
//                mNativeAd = nativeAd
//                //avoid show ad if it is reload request
//                if (!isReloadRequest) this@YandexNativeAdX.onNativeAdLoaded?.value = nativeAd
//
//                adAnalyticsTracker.trackAdLoaded()
//
//                val adEventListener = object : NativeAdEventListener {
//                    override fun onAdClicked() {
//                        "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdClicked".printIt(TAG)
//                        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_clicked")
//
//                        this@YandexNativeAdX.onAdClicked?.value = Unit
//
//                        adAnalyticsTracker.trackAdClicked()
//                    }
//
//                    override fun onImpression(impressionData: ImpressionData?) {
//                        context.sendLogs("${adConfigManager.name}_${adConfigManager.adConfig.adType}_impression")
//                        "${adConfigManager.name}_${adConfigManager.adConfig.adType} onAdImpression".printIt(TAG)
//                        isAdImpression = true
//
//                        this@YandexNativeAdX.onAdImpression?.value = Unit
//                        adAnalyticsTracker.trackAdImpression()
//                    }
//
//                    override fun onLeftApplication() {
//
//                    }
//
//                    override fun onReturnedToApplication() {
//
//                    }
//                }
//                mNativeAd?.setNativeAdEventListener(adEventListener)
//            }
//        }
//        val builder = NativeAdLoader(context)
//        builder.setNativeAdLoadListener(adLoadListener)
//        val nativeAdRequestConfiguration = NativeAdRequestConfiguration.Builder(
//            nativeAdId).build()
//        builder.loadAd(nativeAdRequestConfiguration)
//    }
//
//    fun reloadYandexNativeAd() =
//        loadYandexNativeAdX(mAdConfigManager, onNativeAdLoaded, onAdFailed, onAdClicked, onAdImpression, onAdRequestDenied, true)
//
//    fun showYandexNativeAd(nativeAdContainer: FrameLayout?) {
//
//        if (mNativeAd == null || mAdConfigManager == null || nativeAdContainer == null) return
//
//        val layoutView = LayoutInflater.from(nativeAdContainer.context).inflate(R.layout.ad_unified_splash_yandex, null, false)
//
//        NativeAdView(nativeAdContainer.context).apply {
//            addView(layoutView)
//            val adAgeView = layoutView.findViewById<TextView>(R.id.age)
//            val adBodyView = layoutView.findViewById<TextView>(R.id.body)
//            val adCTAView = layoutView.findViewById<Button>(R.id.callToAction)
//            val adDomainView = layoutView.findViewById<TextView>(R.id.domain)
//            val adFeedbackView = layoutView.findViewById<AppCompatImageView>(R.id.feedback)
//            val adIconView = layoutView.findViewById<ImageView>(R.id.icon)
//            val adMediaView = layoutView.findViewById<MediaView>(R.id.media)
//            val adPriceView = layoutView.findViewById<TextView>(R.id.price)
//            val adSponsoredView = layoutView.findViewById<TextView>(R.id.sponsored)
//            val adTitleView = layoutView.findViewById<TextView>(R.id.title)
//            val adWarningView = layoutView.findViewById<TextView>(R.id.warning)
//
//            val adBinder = NativeAdViewBinder.Builder(layoutView)
//                .setAgeView(adAgeView)
//                .setBodyView(adBodyView)
//                .setCallToActionView(adCTAView)
//                .setDomainView(adDomainView)
//                .setFeedbackView(adFeedbackView)
//                .setIconView(adIconView)
//                .setMediaView(adMediaView)
//                .setPriceView(adPriceView)
//                .setSponsoredView(adSponsoredView)
//                .setTitleView(adTitleView)
//                .setWarningView(adWarningView)
//                .build()
//            mNativeAd?.bindNativeAd(adBinder)
//
//
//        }.let {
//            nativeAdContainer.removeAllViews()
//            nativeAdContainer.addView(it)
//            nativeAdContainer.isVisible = true
//        }
//
//    }
//
//
//    fun destroyNative() {
//        mNativeAd = null
//    }
//}