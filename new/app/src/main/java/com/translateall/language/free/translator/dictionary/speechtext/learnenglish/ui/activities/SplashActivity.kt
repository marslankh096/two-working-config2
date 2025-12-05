package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsConsentManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.IS_CHECK_OPEN_FIRST_LANGUAGE
import com.hm.admanagerx.IS_CHECK_OPEN_ONBOARD
import com.hm.admanagerx.getBooleanRemoteConfigValue
import com.hm.admanagerx.isAppOpenAdShow
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.SubscriptionServiceListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.PhrasesDbHelper
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivitySplashBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.LIFE_TIME_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.MONTHLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.WEEKLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.YEARLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.IS_PREMIUM
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.cmpLog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchLangFromSplash
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchMain
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchOnboardingFromSplash
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var moveToMain = false

    private var iapConnector: IapConnector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Android 15+ edge-to-edge setup (only once, here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                // Apply padding to your view using KTX extension
                view.updatePadding(
                    left = systemBarInsets.left,
                    top = 0,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )

                // Consume the insets if you've handled them
                WindowInsetsCompat.CONSUMED
            }
        }
// To prevent launching another instance of app on clicking app icon
        settingIAP()
        initConsentDialog()
    }
    private fun initConsentDialog() {
        //To Check old user
        val showLanguageScreen = TinyDB.getInstance(this).language.isEmpty()

        splashAdConfigManager =
            if (showLanguageScreen && !com.hm.admanagerx.utility.TinyDB.getInstance(this)
                    .getBoolean("is_user_first_time")
            ) {
                //save New user
                com.hm.admanagerx.utility.TinyDB.getInstance(this@SplashActivity)
                    .putBoolean("is_user_first_time", true)
                AdConfigManager.INTER_AD_SPLASH_FIRST_OPEN
            } else {
                AdConfigManager.INTER_AD_SPLASH_SECOND_OPEN
            }
        splashAdConfigManager.apply {
            adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
        }
        AdsConsentManager.getInstance(this@SplashActivity).init(this,
            consentCompleteListener = {
                cmpLog("consentCompletedListener")
                initMediationConsent()
                initAdsManager()
            })
    }
    private fun initMediationConsent() {
//        AppLovinPrivacySettings.setHasUserConsent(true, this)
//        AppLovinPrivacySettings.setDoNotSell(false, this)
//        VunglePrivacySettings.setGDPRStatus(true, "1.0.0")
//        VunglePrivacySettings.setCCPAStatus(true)
//        val sdk = MBridgeSDKFactory.getMBridgeSDK()
//        sdk.setConsentStatus(this, MBridgeConstans.IS_SWITCH_ON)
//        sdk.setDoNotTrackStatus(false)
    }
    private fun settingIAP() {
        iapConnector = IapConnector(
            context = this,
            subscriptionKeys = listOf(
                YEARLY_SUBS_ID,
                MONTHLY_SUBS_ID,
                LIFE_TIME_SUBS_ID,
                WEEKLY_SUBS_ID
            )
        )
        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionNotPurchased() {
                TinyDB.getInstance(this@SplashActivity).putBoolean(IS_PREMIUM, false)
            }
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(IS_PREMIUM, true)
                    }
                    MONTHLY_SUBS_ID -> {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(IS_PREMIUM, true)
                    }
                    LIFE_TIME_SUBS_ID -> {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(IS_PREMIUM, true)
                    }
                    WEEKLY_SUBS_ID -> {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(IS_PREMIUM, true)
                    }
                }
            }
            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {}
            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {}
            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {
            }
        })
    }
    private fun inItPhraseDb() {
        try {
            PhrasesDbHelper(applicationContext).createDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun checkAndLaunch() {
        if (moveToMain)
            return

        moveToMain = true

        val showOnboarding = IS_CHECK_OPEN_ONBOARD.getBooleanRemoteConfigValue()

        if (TinyDB.getInstance(this).language.isEmpty()) {
            if (IS_CHECK_OPEN_FIRST_LANGUAGE.getBooleanRemoteConfigValue()) {
                if (!isSplashAdShown) {
                    AdsManagerX.loadInterAd(
                        this@SplashActivity,
                        AdConfigManager.INTER_AD_LANGUAGE.apply {
                            adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                        }
                    )
                }
                launchLangFromSplash()
            } else {
                if (showOnboarding) {
                    if (TinyDB.getInstance(this).showOnboarding()) {
                        launchOnboardingFromSplash()
                    } else {
                        launchMain(true, isSplashToHome = true)
                    }
                } else {
                    launchMain(true, isSplashToHome = true)
                }
            }
        } else if (showOnboarding) {
            if (TinyDB.getInstance(this).showOnboarding()) {
                launchOnboardingFromSplash()
            } else {
                launchMain(true, isSplashToHome = true)
            }
        } else {
            launchMain(true, isSplashToHome = true)
        }
    }

    override fun onResume() {
        super.onResume()
        isAppOpenAdShow(false)
        animator?.resume()
        if (isAdClosed) {
            animator?.removeAllUpdateListeners()
            animator?.removeAllListeners()
            animator = null
            isAdClosed = false
            letStart("onResume")
            isInterAdLoaded = false
        }
    }

    override fun onPause() {
        super.onPause()
        animator?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        iapConnector?.closeAllConnection()
        animator?.removeAllUpdateListeners()
        animator?.removeAllListeners()
        animator = null
        isAdClosed = false
        isInterAdLoaded = false
        /*  if (AdsManagerX.isInterAdLoaded(splashAdConfigManager)){
               AdsManagerX.destroyInterAd(splashAdConfigManager)
          }*/
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    private var splashAdConfigManager: AdConfigManager = AdConfigManager.INTER_AD_SPLASH_FIRST_OPEN
    var isSplashAdShown = false
    private var isInterAdLoaded: Boolean = false
    private var MAX_PROGRESS = 5000L
    private var animator: ValueAnimator? = null
    private var isAdShown = false
    private var isAdClosed: Boolean = false

    private fun initAdsManager() {
        AdsManagerX.init(
            application = application as TranslateApplication,
            isAppLevelAdsInitialization = true,
            onAdsSdkInitializeComplete = {
                cmpLog("AdsManagerX Initialized")
                AdsManagerX.loadFirebaseRemoteConfig {
                    cmpLog("Firebase Initialized")
                    startSplashTimer()
                }
            })
    }

    private fun startSplashTimer() {
        cmpLog("startSplashTimer")
        MAX_PROGRESS = if (!isPremium() && Controller.isOnline(this)) {
            loadSplashInterAD(splashAdConfigManager)
            12000L
        } else {
            3000L
        }
        cmpLog("startSplashTimer MAX_PROGRESS=$MAX_PROGRESS")
        animator = ValueAnimator.ofInt(0, MAX_PROGRESS.toInt())?.apply {
            duration = MAX_PROGRESS
            addUpdateListener {
                if (!isDestroyed && !isAdShown && isInterAdLoaded) {
                    showIntersAd(splashAdConfigManager)
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isDestroyed && !isAdShown) {
                        letStart("onAnimationEnd")
                    }
                }
            })
            start()
        }
    }

    private fun letStart(isInvokeFrom: String) {
        cmpLog("letStart isInvokeFrom=$isInvokeFrom  isInterAdLoaded=$isInterAdLoaded ")
        inItPhraseDb()
        checkAndLaunch()
    }

    private fun loadSplashInterAD(adConfig: AdConfigManager) {
        binding.splashMain.tvAds.visible()
        AdsManagerX.loadInterAd(this, adConfig, onAdLoaded = {
            isInterAdLoaded = true
        }, onAdFailed = {
            isInterAdLoaded = true
        })
    }

    private fun showIntersAd(interAdSplash: AdConfigManager) {
        isAdShown = true
        AdsManagerX.showInterAd(this,
            interAdSplash,
            onAdShow = {
                binding.splashMain.tvAds.gone()
                isAdClosed = true
                isAdShown = true
                isSplashAdShown = true
            },
            onAdClose = {
                isAdClosed = true
            },
            funBlock = {
                letStart("funBlock")
            })
    }
}