package com.hm.admanagerx

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.nativead.MediaView
import com.google.firebase.analytics.FirebaseAnalytics
import com.hm.admanagerx.AdsManagerX.isAppLevelAdsInitializationSuccess
import com.hm.admanagerx.utility.HandlerResumeX
import com.hm.admanagerx.utility.TinyDB
import com.hm.admanagerx.utility.childrenRecursiveSequence
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

const val IS_PREMIUM = "is_premium"
const val IS_RUSSIAN = "is_russian"
var nativeAdFragment: String? = "wa_Images"

fun Context.isPremium() = TinyDB(this).getBoolean(IS_PREMIUM)
fun Context.isRussian() = TinyDB(this).getBoolean(IS_RUSSIAN)

fun String.printIt(tag: String = "-->") = Log.e(tag, this)

fun Context.isOnline(block: () -> Unit) {
    if (isOnline()) {
        block()
    }
}

fun Context.isOnline(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        val hasTransport = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true // Consider VPN as a valid network
            else -> false
        }

        return if (hasTransport) {
            (actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else {
            false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected && nwInfo.isAvailable
    }
}


fun Context.sendLogs(name: String, bundle: Bundle = Bundle()) {
    FirebaseAnalytics.getInstance(this).logEvent(name, bundle)
}

fun Context.initCustomDialog(layout: Int, isBGTransparent: Boolean = false): Dialog {
    val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

    if (isBGTransparent) if (dialog.window != null) dialog.window?.setBackgroundDrawableResource(
        android.R.color.transparent
    )
    dialog.setCancelable(false)
    dialog.setContentView(layout)
    return dialog
}

/**
 * showAdLoadingShimmerView : show loading view for native and banner ad
 */
fun Context.showAdLoadingShimmerView(adContainer: FrameLayout?, adLayout: Int) {
    if (adContainer == null) return
    adContainer.removeAllViews()

    LayoutInflater.from(this).inflate(adLayout, null, false)
        ?.let { layout ->
            adContainer.visibility= View.VISIBLE
            layout.childrenRecursiveSequence().iterator().forEach { view ->
                when (view) {
                    !is ViewGroup -> {
                        view.apply { setBackgroundColor(Color.GRAY) }
                        when (view) {
                            is ImageView -> view.setImageDrawable(null)
                        }
                    }

                    is MediaView -> view.setBackgroundColor(Color.GRAY)
                    is ConstraintLayout -> view.setBackgroundColor(Color.GRAY)
                }
            }

            val shimmerFrameLayout = ShimmerFrameLayout(this).apply {
                addView(layout)
                this.setShimmer(
                    Shimmer.ColorHighlightBuilder().setBaseColor(Color.GRAY)
                        .setHighlightColor(Color.WHITE).build()
                )
                this.stopShimmer()
                this.showShimmer(false)
            }

            adContainer.addView(shimmerFrameLayout)
        }
}


/**
 * showLoadingBeforeAd : use for show loading before FullscreenAds (Inter Ad , Rewarded AD , App open AD )
 */
fun AdConfig.showLoadingBeforeAd(
    isAdLoaded: Boolean,
    activity: AppCompatActivity,
    layout: Int = R.layout.ad_loading_view,
    onDone: () -> Unit,
    onLoadingShowHide: ((Boolean) -> Unit)? = null,
): Dialog? {
    if (isShowLoadingBeforeAd && isAdLoaded && !activity.isFinishing && !activity.isDestroyed) {

        //init fullscreen loading dialog
        var loadingDialog: Dialog?
        loadingDialog = activity.initCustomDialog(layout, true).apply {
            setOnShowListener {  onLoadingShowHide?.invoke(true) }
            setOnDismissListener { onLoadingShowHide?.invoke(false) }
            show()
        }

        var handlerX: HandlerResumeX? = null

        activity.isInterAdShow(true) //for not show app open ad when inter or rewarded ad loading


        // lifecycle callbacks for handle state of Handler (pause , resume , destroy) and null loadDialog for avoid leaks
        val activityLifecycleObserver = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                handlerX?.pause()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                handlerX?.resume()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                activity.isInterAdShow(false) //for not show app open ad when inter or rewarded ad loading
                handlerX?.destroy()
                loadingDialog = null
            }
        }

        //
        activity.lifecycle.removeObserver(activityLifecycleObserver)


        // handler delay for show loading view
        handlerX = HandlerResumeX(beforeAdLoadingTimeInMs) {
            onDone.invoke()
            activity.isInterAdShow(false) // set default when inter or rewarded ad loading complete
            activity.lifecycle.removeObserver(activityLifecycleObserver)
        }.apply { start() }

        activity.lifecycle.addObserver(activityLifecycleObserver)

        return loadingDialog
    } else {
        onDone.invoke()
        return null
    }
}

fun Context.getAdId(adId: Any): String {
    return if (adId is Int) getString(adId) else if (adId is String) adId else ""
}

fun Context.adCheckShow(adConfig: AdConfig): Boolean {
    return isPremium() ||
            !isOnline() ||
            !adConfig.isAdShow
}

/**
 * getAdaptiveAdSize : get adaptive banner AdSize
 */
fun Context.getAdaptiveAdSize(width: Int): AdSize {
    val outMetrics = Resources.getSystem().displayMetrics

    val density = outMetrics.density

    var adWidthPixels = width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}


/**
 * clearAdContainer : use for clear all views for ad container of native and banner ad
 */
fun clearAdContainer(adContainer: FrameLayout?, lifecycleOwner: LifecycleOwner) {
    var activityLifecycleObserver: DefaultLifecycleObserver? = null
    activityLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            adContainer?.removeAllViews()
            activityLifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
        }
    }
    lifecycleOwner.lifecycle.removeObserver(activityLifecycleObserver)
    lifecycleOwner.lifecycle.addObserver(activityLifecycleObserver)
}


/**
 * listenerX generic livedata observer use as callbacks for avoid memory leaks
 */
fun <T> LifecycleOwner.listenerX(listener: (T) -> Unit): MutableLiveData<T> {
    return MutableLiveData<T>().apply {
        observe(this@listenerX) {
            listener.invoke(it)
        }
    }
}


/**
 * adLoadFunBlockList : use for load ad after init
 */
var adLoadFunBlockList: ConcurrentHashMap<String, MutableLiveData<Unit>> = ConcurrentHashMap()

/**
 * check  MobileAds.initialize(application) initialized if not init then add in Queue for load ad after init
 */
fun checkMobileAdsInit(adConfigManager: AdConfigManager, onAdLoadFunBlock: MutableLiveData<Unit>) {
    if (isAppLevelAdsInitializationSuccess) {
        onAdLoadFunBlock.value = Unit
    } else {
        adLoadFunBlockList[adConfigManager.name] = onAdLoadFunBlock
    }
}

suspend fun loadAdsInQueueAfterInit() {
    adLoadFunBlockList.forEach {
        if (!it.value.hasActiveObservers()) return@forEach
        it.value.value = Unit
        delay(2500)
    }
    adLoadFunBlockList.clear()
}


private fun ViewGroup.getAdWidth(): Int {
    val outMetrics = Resources.getSystem().displayMetrics
    val density = outMetrics.density
    var adWidthPixels = width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }
    val adWidth = (adWidthPixels / density).toInt()
    return adWidth
}

// fun ViewGroup.getYandexStickyAdSize() = BannerAdSize.stickySize(context, getAdWidth())
//fun ViewGroup.getYandexInlineAdSize() =
//    BannerAdSize.inlineSize(context, getAdWidth(), 60)


sealed class AdCheckResult {
    object PremiumUser : AdCheckResult() {
        override fun toString() = "Ad request denied: User is premium."
    }

    object Offline : AdCheckResult() {
        override fun toString() = "Ad request denied: User is offline."
    }

    object AdsDisabled : AdCheckResult() {
        override fun toString() = "Ad request denied: Ad are disabled in configuration."
    }

    object AdLoading : AdCheckResult() {
        override fun toString() = "Ad request denied: An ad is currently loading."
    }

    object AdSessionLimitReached : AdCheckResult() {
        override fun toString() = "Ad request denied: Ad session limit reached."
    }

    object AdAlreadyLoaded : AdCheckResult() {
        override fun toString() = "Ad request denied: Ad is already loaded."
    }

    object AdsInitializationFailed : AdCheckResult() {
        override fun toString() = "Ad request denied: App level ads initialization failed."
    }

    data class AdLoadOnCount(val count: Long) : AdCheckResult() {
        override fun toString() = "Ad request denied: Ad load count enable. count is $count"
    }


    object ReadyToGo : AdCheckResult() {
        override fun toString() = "Ad request denied: Ad Ready to Go."
    }
}




