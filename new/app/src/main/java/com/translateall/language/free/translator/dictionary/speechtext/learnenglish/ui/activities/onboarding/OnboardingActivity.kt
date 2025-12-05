package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.onboarding

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.IS_CHECK_OPEN_PLANS
import com.hm.admanagerx.getBooleanRemoteConfigValue
import com.hm.admanagerx.getLongRemoteConfigValue
import com.hm.admanagerx.utility.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityOnboardingBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.ExitDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchInApp
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchMain
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.hide
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class OnboardingActivity : BaseActivity() {
    //ext_ars
    private var moveTo_aopars = false
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private var exitDialog: ExitDialog? = null
    //private var hadConnection = true

    //    var loadNativeAd1 = false
//    var loadNativeAd2 = false
//    var loadNativeAd3 = false
    private var pagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⭐ Skip onboarding if already completed  //ars_ext
        if (TinyDB.getInstance(this).getBoolean("onboarding_done", false)) {
            launchMain(true)
            finish()
            return
        }
        // ✅ Android 15+ edge-to-edge setup (only once, here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
                // Apply padding to your view using KTX extension
                view.updatePadding(
                    left = systemBarInsets.left,
                    top = systemBarInsets.top,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )
                // Consume the insets if you've handled them
                WindowInsetsCompat.CONSUMED
            }
        }
        loadShowNativeAd()
        //Load App Open Ad If Comes Directly from Splash
        loadAppOpenAd()
        init()
        setListeners()
    }
    override fun onResume() {
        super.onResume()
//ext_ars
        //  letStartars()
        /*if (isPremium()) {
            binding.firstNativeAdViewLayout.gone()
            binding.secondNativeAdViewLayout.gone()
            binding.thirdNativeAdViewLayout.gone()
        }*/
    }
    //ext_ars
    private fun letStartars() {
        if (moveTo_aopars)
            return
        moveTo_aopars = true
        val showOnboardingars = IS_CHECK_OPEN_PLANS.getLongRemoteConfigValue()
        Log.d("RemoteConfig", "showOnboardingars = $showOnboardingars")
        if (showOnboardingars == 0L) {
            Log.d("Paywall", "0L → Paywall OFF (Kabhi nahi dikhani)")
            launchInApp()
        } else if (showOnboardingars == -1L) {
            Log.d("Paywall", "-1L → Paywall ALWAYS (Har dafa onboarding ke baad)")
            launchInApp()
        } else if (showOnboardingars == -2L) {
            Log.d("Paywall", "-2L → Paywall 24 HOURS gap")
            launchInApp()
        }
        if (showOnboardingars !in listOf(0L, -1L, -2L))
            launchMain()
    }
    private fun init() {
        //ext_ars
//        val showOnboardingars = IS_CHECK_OPEN_PLANS.getLongRemoteConfigValue()
//        Log.d("RemoteConfig", "showOnboardingars = $showOnboardingars")
        onboardingAdapter = OnboardingAdapter(this)
        binding.onboardingViewPager.adapter = onboardingAdapter
        binding.dotsIndicator.attachTo(binding.onboardingViewPager)
        exitDialog = ExitDialog.newInstance()
        /*if (Controller.isOnline(this)) {
            showThirdNativeAd(true)
            showFirstNativeAd(false)
            showSecondNativeAd(false)
        } else {
            hadConnection = false
            binding.firstNativeAdViewLayout.gone()
            binding.secondNativeAdViewLayout.gone()
            binding.thirdNativeAdViewLayout.gone()
        }*/
    }

    private fun setListeners() {
        binding.onboardingViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                pagePosition = position
                if (pagePosition == 2) {
                    binding.skipTV.gone()
                    //showFirstNativeAd()
                    binding.nextTV.text = getString(R.string.lets_start)
                    binding.titleTV.text = getString(R.string.conversation)
                    binding.detailTV.text = getString(R.string.conversation_detail)
                    /*if (!isPremium() && hadConnection) {
                        binding.firstNativeAdViewLayout.visibility = GONE
                        binding.secondNativeAdViewLayout.visibility = GONE
                        binding.thirdNativeAdViewLayout.visibility = VISIBLE
                    }*/
                } else if (pagePosition == 1 && Controller.isOnline(this@OnboardingActivity)) {
                    binding.skipTV.visible()
                    //showSecondNativeAd()
                    binding.nextTV.text = getString(R.string.next)
                    binding.titleTV.text = getString(R.string.camera_translation)
                    binding.detailTV.text = getString(R.string.camera_detail)
                    /*if (!isPremium()) {
                        binding.firstNativeAdViewLayout.visibility = GONE
                        binding.secondNativeAdViewLayout.visibility = VISIBLE
                        binding.thirdNativeAdViewLayout.visibility = GONE
                    }*/
                } else {
                    binding.skipTV.visible()
                    //showThirdNativeAd()
                    binding.nextTV.text = getString(R.string.next)
                    binding.titleTV.text = getString(R.string.text_translation)
                    binding.detailTV.text = getString(R.string.text_detail)
                    /*    if (!isPremium() && hadConnection) {
                            binding.firstNativeAdViewLayout.visibility = VISIBLE
                            binding.secondNativeAdViewLayout.visibility = GONE
                            binding.thirdNativeAdViewLayout.visibility = GONE
                        }*/
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        binding.nextTV.setOnClickListener {
            if (pagePosition < 2) {
                binding.onboardingViewPager.currentItem = pagePosition + 1
            } else {
                //ars
                val tinyDBcheck = TinyDB.getInstance(this)
                // ⭐ If user is here again → directly skip to Main
                if (tinyDBcheck.getBoolean("onboarding_done", false)) {
                    Log.d("Onboarding", "Button pressed → second time → Go Main")
                    launchMain(true)
                    finish()
                    return@setOnClickListener
                }
                //ext_ars
                TinyDB.getInstance(this).putBoolean("onboarding_done", true)
                //  TinyDB.getInstance(this).setOnboarding(false) // 👈 Save completion
              letStartars()
            }
        }

        binding.skipTV.setOnClickListener {
            binding.onboardingViewPager.currentItem = 2
        }

        exitDialog?.initListener(object : ExitDialog.ExitClickListener {
            override fun onExitClick() {
                finish()
            }
        })

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })
    }

    private fun backPressed() {
        when (pagePosition) {
            0 -> {
                launchMain(true)
            }

            1 -> {
                binding.onboardingViewPager.currentItem = 0
            }

            2 -> {
                binding.onboardingViewPager.currentItem = 1
            }
        }
    }

    var firstTime = true

    /*    private fun showFirstNativeAd(showLayout: Boolean = true) {

            if (fetchRemoteBoolean(RemoteConfigValues.IS_SHOW_ONBOARD_NATIVE_1)) {
                if (showLayout)
                    binding.firstNativeAdViewLayout.visible()

                if (loadNativeAd1.not()) {

                    Log.d("hello", "loadNativeAd: IS_SHOW_ONBOARD_NATIVE_1")
                    loadNativeAd(
                        ADUnitPlacements.NATIVE_AD,
                        AMCallback = {
                            loadNativeAd1 = true
                            Log.d("hello", "showNativeAd: 179")
                            bindNativeAM(
                                binding.firstNativeAdView,
                                CustomNativeMediumLayoutBinding.inflate(LayoutInflater.from(this)),
                                it,
                                remoteConfigValue = null,
                                viewBound = {}
                            )

                        }, checkIsLoading = false,
                        onError = { binding.firstNativeAdViewLayout.gone() }
                    )
                }
            } else {
                binding.firstNativeAdViewLayout.gone()
            }
        }

        private fun showSecondNativeAd(showLayout: Boolean = true) {

            if (fetchRemoteBoolean(RemoteConfigValues.IS_SHOW_ONBOARD_NATIVE_2)) {
                if (showLayout)
                    binding.secondNativeAdViewLayout.visible()
                if (loadNativeAd2.not()) {
                    Log.d("hello", "loadNativeAd: IS_SHOW_ONBOARD_NATIVE_2")
                    loadNativeAd(
                        ADUnitPlacements.NATIVE_AD,
                        AMCallback = {
                            loadNativeAd2 = true
                            Log.d("hello", "showNativeAd: 205")
                            bindNativeAM(
                                binding.secondNativeAdView,
                                CustomNativeMediumLayoutBinding.inflate(LayoutInflater.from(this)),
                                it,
                                remoteConfigValue = null,
                                viewBound = {}
                            )

                        }, checkIsLoading = false,
                        onError = { binding.secondNativeAdViewLayout.gone() }
                    )
                } else {
                    binding.secondNativeAdViewLayout.gone()
                }
            }
        }

        private fun showThirdNativeAd(showLayout: Boolean = true) {

            if (fetchRemoteBoolean(RemoteConfigValues.IS_SHOW_ONBOARD_NATIVE_3)) {

                if (loadNativeAd3.not()) {
                    Log.d("hello", "loadNativeAd: IS_SHOW_ONBOARD_NATIVE_3")

                    if (firstTime) {
                        firstTime = false

                        nativeAd?.let {
                            Log.d("hello", "showNativeAd: 232")
                            bindNativeAM(
                                binding.thirdNativeAdView,
                                CustomNativeMediumLayoutBinding.inflate(LayoutInflater.from(this)),
                                nativeAd,
                                remoteConfigValue = null,
                                viewBound = {
                                    loadNativeAd3 = true
                                    Log.d("hello", "loadNativeAd: 236")
                                    loadNativeAd()
                                },
                                onPremium = { binding.thirdNativeAdViewLayout.gone() }
                            )
                        } ?: run {
                            binding.thirdNativeAdViewLayout.visible()
                            loadNativeAd(
                                ADUnitPlacements.NATIVE_AD,
                                AMCallback = {
                                    loadNativeAd3 = true
                                    Log.d("hello", "showNativeAd: 250")
                                    bindNativeAM(
                                        binding.thirdNativeAdView,
                                        CustomNativeMediumLayoutBinding.inflate(LayoutInflater.from(this)),
                                        it,
                                        remoteConfigValue = null,
                                        viewBound = {}
                                    )
                                },
                                checkIsLoading = false,
                                onError = { binding.thirdNativeAdViewLayout.gone() }
                            )
                        }
                    } else {
                        Log.d("hello", "loadNativeAd: 260")
                        loadNativeAd(
                            ADUnitPlacements.NATIVE_AD,
                            AMCallback = {
                                loadNativeAd3 = true
                                Log.d("hello", "showNativeAd: 268")
                                if (showLayout)
                                    binding.thirdNativeAdViewLayout.visible()
                                bindNativeAM(
                                    binding.thirdNativeAdView,
                                    CustomNativeMediumLayoutBinding.inflate(LayoutInflater.from(this)),
                                    it,
                                    remoteConfigValue = null,
                                    viewBound = {}
                                )

                            },
                            checkIsLoading = false,
                            onError = { binding.thirdNativeAdViewLayout.gone() }
                        )
                    }
                } else {
                    binding.thirdNativeAdViewLayout.gone()
                }
            }
        }*/

    override fun onDestroy() {
        super.onDestroy()
        if (AdsManagerX.isNativeAdLoaded(AdConfigManager.NATIVE_AD_ON_BOARDING)) {
            AdsManagerX.destroyNativeAd(AdConfigManager.NATIVE_AD_ON_BOARDING)
        }
    }

    private fun loadShowNativeAd() {
        if (isPremium().not() && isOnline(this@OnboardingActivity)) {
            AdsManagerX.loadNativeAd(
                this@OnboardingActivity,
                AdConfigManager.NATIVE_AD_ON_BOARDING.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_medium_layout
                }, binding.nativeAdContainer, onAdFailed = {
                    binding.nativeAdContainer.hide()
                }
            )
        } else {
            binding.nativeAdContainer.hide()
        }
    }

    private fun loadAppOpenAd() {
        if (intent.getBooleanExtra("isFromSplash", false)) {
            AdsManagerX.loadAppOpenAd(
                this,
                AdConfigManager.APP_OPEN.apply {
                    adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                })
        }
    }
}