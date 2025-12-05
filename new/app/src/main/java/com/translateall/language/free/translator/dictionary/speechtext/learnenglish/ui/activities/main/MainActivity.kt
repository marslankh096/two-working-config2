package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.isAppOpenAdShow
import com.hm.admanagerx.isOnline
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.SubscriptionServiceListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityMainBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services.NotificationReceiver
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.LIFE_TIME_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.MONTHLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.WEEKLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.YEARLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.adapter.MainAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.dictionary.DictionaryFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.files.FilesFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.home.HomeFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase.PhraseFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.ExitDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.NoConnectionDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.CLIP_BOARD_SERVICE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.IS_PREMIUM
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.canPostNotification
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getClipboardClass
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getFirstBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isMyServiceRunning
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchClipboardService
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.putBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Objects

@Suppress("DEPRECATION")
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: MainAdapter
    private var exitDialog: ExitDialog? = null
    private var selectedTab: Int = 0

    private var iapConnector: IapConnector? = null
    var isShowPayWallBackFromPremiumScreen = false
    override fun onResume() {
        super.onResume()
        if (selectedTab == 0 && !(application as TranslateApplication).noConnectionDialogShown) {
            if (!Controller.isOnline(this)) {
                (application as TranslateApplication).shouldReloadAds = true
                showNoInternetDialog()
            }
        }
        if ((application as TranslateApplication).shouldReloadAds) {
            if (Controller.isOnline(this)) {
                (application as TranslateApplication).shouldReloadAds = false
                init()
                setListeners()
            }
        }
        isActivityPaused = false
        if (isShowPayWallBackFromPremiumScreen && !isPremium()) {
            isShowPayWallBackFromPremiumScreen = false
            binding.layoutPremium.layoutPremiumMain.visible()
        }

        if (isPremium()) {
            AdsManagerX.destroyAllAds()
            binding.nativeAdContainer.gone()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        init()
        setListeners()
        loadMainBannerAd()
        //Load App Open Ad If Comes Directly from Splash
        loadAppOpenAd()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleNotification()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showPermissionDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPermissionDialog() {
        val dialog = Dialog(this, R.style.exitDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        Objects.requireNonNull<Window>(dialog.window)
            .setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dlg_notification_permission)
        dialog.setCancelable(true)

        dialog.findViewById<TextView>(R.id.settingsTV)?.setOnClickListener {
            if (isDoubleClick()) {
                isAppOpenAdShow(false)
                val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(settingsIntent)
                dialog.dismiss()
            }
        }

        val window: Window? = dialog.window
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        val wlp = window!!.attributes
        wlp.gravity = Gravity.BOTTOM
        wlp.dimAmount = 0.7f
        window.attributes = wlp
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleNotification() {
        val delay: Long = 4 * 60 * 60 * 1000
//        val delay: Long = (1 * 60 * 60 * 1000) / 2
        val triggerTime: Long = System.currentTimeMillis()
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
            .putExtra("requestCode", "scheduled")
        val checkIntent: PendingIntent? = PendingIntent.getBroadcast(
            this,
            2341,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_NO_CREATE
        )
        if (checkIntent == null) {
            Log.e("none", "scheduleNotification: no alarm was set")
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                2341,
                notificationIntent,
                if (Build.VERSION.SDK_INT >= 31) 201326592 else 134217728 // PendingIntent.FLAG_UPDATE_CURRENT
//                if (Build.VERSION.SDK_INT >= 31) 201326592 else 134217728
            )
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                delay,
                pendingIntent
            )
        } else {
            Log.e("none", "scheduleNotification: alam was already set")
        }

    }

    private fun init() {
        val fragments = mapOf(
            "Home" to HomeFragment(),
            "Dictionary" to DictionaryFragment(),
            "Files" to FilesFragment(),
            "Phrase" to PhraseFragment()
        )
        pagerAdapter = MainAdapter(this@MainActivity, fragments.map { it.value })
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.offscreenPageLimit = 2

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("none", "onPageSelected: $position")
                if (selectedTab == 1 && position != 1) {
                    resetDictionarySelection()
                }
                selectedTab = binding.viewPager.currentItem
                showSelectedTab()
            }
        })

        selectedTab = binding.viewPager.currentItem

        if (canPostNotification()) {
            scheduleNotification()
        } else {
            askNotificationPermission()
        }

        exitDialog = ExitDialog.newInstance()
        AdsManagerX.loadNativeAd(
            this,
            AdConfigManager.NATIVE_AD_EXIT_DIALOG, null
        )
        checkForClipboardService()
        settingUpWeeklySubscription()
    }

    private fun setListeners() {

        exitDialog?.initListener(object : ExitDialog.ExitClickListener {
            override fun onExitClick() {
                (application as TranslateApplication).noConnectionDialogShown = false
                (application as TranslateApplication).homePremiumShown = false
                (application as TranslateApplication).conversationPremiumShown = false
                finish()
            }
        })

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isDoubleClick()) {
                        backPressed()
                    }
                }
            })

        binding.layoutHome.setOnClickListener {
            if (isDoubleClick()) {
                selectTab(0)
            }
        }

        binding.layoutDictionary.setOnClickListener {
            if (isDoubleClick()) {
                selectTab(1)
            }
        }

        binding.layoutFiles.setOnClickListener {
            if (isDoubleClick()) {
                selectTab(2)
            }
        }

        binding.layoutPhrase.setOnClickListener {
            if (isDoubleClick()) {
                selectTab(3)
            }
        }

        binding.layoutPremium.premiumButton.setOnClickListener {
            if (isDoubleClick()) {
                if (isOnline()) {
                    iapConnector?.subscribe(this, WEEKLY_SUBS_ID)
                } else {
                    showToast(getString(R.string.internet_toast),0)
                }
            }
        }

        binding.layoutPremium.ivClosePremium.setOnClickListener {
            binding.layoutPremium.layoutPremiumMain.gone()
        }

        binding.layoutPremium.layoutPremiumMain.setOnClickListener {}

    }

    fun activatePremium() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.layoutPremium.layoutPremiumMain.gone()
            AdsManagerX.destroyAllAds()
            binding.nativeAdContainer.gone()
            TinyDB.getInstance(this@MainActivity)
                .putBoolean(IS_PREMIUM, true)
            delay(500)
            init()
            setListeners()
        }
    }

    private fun selectTab(index: Int) {
        if (selectedTab != index) {
            binding.viewPager.setCurrentItem(index, false)
            selectedTab = binding.viewPager.currentItem
            showSelectedTab()
        }
    }

    private fun showSelectedTab() {
        binding.homeIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_home
            )
        )
        binding.dictionaryIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_dictionary
            )
        )
        binding.filesIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_files
            )
        )
        binding.phraseIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_phrase
            )
        )
        binding.homeTV.setTextColor(Color.parseColor("#ABABAB"))
        binding.dictionaryTV.setTextColor(Color.parseColor("#ABABAB"))
        binding.filesTV.setTextColor(Color.parseColor("#ABABAB"))
        binding.phraseTV.setTextColor(Color.parseColor("#ABABAB"))

        when (selectedTab) {
            0 -> {
                binding.homeIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_home_selected
                    )
                )
                binding.homeTV.setTextColor(Color.parseColor("#4086F5"))
            }

            1 -> {
                binding.dictionaryIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_dictionary_selected
                    )
                )
                binding.dictionaryTV.setTextColor(Color.parseColor("#4086F5"))
            }

            2 -> {
                binding.filesIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_files_selected
                    )
                )
                binding.filesTV.setTextColor(Color.parseColor("#4086F5"))
            }

            3 -> {
                binding.phraseIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_phrase_selected
                    )
                )
                binding.phraseTV.setTextColor(Color.parseColor("#4086F5"))
            }
        }
    }

    private fun settingUpWeeklySubscription() {

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

            override fun onSubscriptionNotPurchased() {}

            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        TinyDB.getInstance(this@MainActivity).putBoolean(IS_PREMIUM, true)
                    }

                    MONTHLY_SUBS_ID -> {
                        TinyDB.getInstance(this@MainActivity).putBoolean(IS_PREMIUM, true)
                    }

                    LIFE_TIME_SUBS_ID -> {
                        TinyDB.getInstance(this@MainActivity).putBoolean(IS_PREMIUM, true)
                    }

                    WEEKLY_SUBS_ID -> {
                        TinyDB.getInstance(this@MainActivity).putBoolean(IS_PREMIUM, true)
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        activatePremium()
                    }

                    MONTHLY_SUBS_ID -> {
                        activatePremium()
                    }

                    LIFE_TIME_SUBS_ID -> {
                        activatePremium()
                    }

                    WEEKLY_SUBS_ID -> {
                        activatePremium()
                    }
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {

                iapKeyPrices.forEach {
                    when (it.key) {

                        WEEKLY_SUBS_ID -> {
                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))

                            binding.layoutPremium.tvPremiumDetail.text =
                                String.format(
                                    getString(R.string.just_1_s_weekly_cancel_anytime),
                                    priceTxt
                                )
                        }

                        YEARLY_SUBS_ID -> {}

                        MONTHLY_SUBS_ID -> {}

                        LIFE_TIME_SUBS_ID -> {
//                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
//                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))
//
//                            binding.layoutPremium.tvPremiumDetail.text =  String.format(getString(R.string.just_lifetime),priceTxt)
                        }
                    }
                }
            }

            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {
            }
        })
    }

    private fun checkForClipboardService() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            kotlin.runCatching {
                if (getFirstBoolean(CLIP_BOARD_SERVICE) && !isMyServiceRunning(getClipboardClass())) {
                    if ("xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(this)) {
                                putBoolean(CLIP_BOARD_SERVICE, false)
                            } else {
                                launchClipboardService()
                            }
                        } else {
                            launchClipboardService()
                        }
                    } else {
                        launchClipboardService()

                    }
                }
            }
        }

    }

    private fun showNoInternetDialog() {
        val dialog = NoConnectionDialog.newInstance()
        dialog.initListener(object : NoConnectionDialog.NoConnectionListener {
            override fun turnOnMobileData() {
                startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
            }

            override fun turnOnWifi() {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }

        })
        dialog.let {
            if (!it.isVisible && !it.isAdded) {
                (application as TranslateApplication).noConnectionDialogShown = true
                it.show(supportFragmentManager, "no_internet_dialog")
            }
        }
    }

    private fun backPressed() {
        if (binding.layoutPremium.layoutPremiumMain.visibility != VISIBLE) {
            if (selectedTab != 0) {
                selectTab(0)
            } else {
                exitDialog?.let {
                    if (!it.isVisible && !it.isAdded)
                        it.show(supportFragmentManager, "exit_dialog")
                }
            }
        }
    }

    fun showPremiumDialog() {
        if (!isPremium() && !(application as TranslateApplication).homePremiumShown) {
            (application as TranslateApplication).homePremiumShown = true
            binding.layoutPremium.layoutPremiumMain.visible()
        }
    }

    private fun resetDictionarySelection() {
        (pagerAdapter.getFragment(1) as DictionaryFragment).resetSelectedView()
    }

    private var isDialogVisible = false
    fun showLoadingDialog() {
        if (!isDialogVisible) {
            isDialogVisible = true
            loadingDialog?.let {
                if (!it.isShowing) {
                    it.show()
                }
            }
        }
    }

    fun hideLoadingDialog() {
        if (isDialogVisible) {
            isDialogVisible = false
            loadingDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isShowPayWallBackFromPremiumScreen = false
        mAdHandler.removeCallbacks(mAdRunnable)
        AdsManagerX.destroyAllAds()
    }

    /* private fun loadMainNativeBannerAd() {
         val updatedRemoteAdConfig = AdConfigManager.NATIVE_AD_MAIN.let {
             it.adConfig.fetchAdConfigFromRemote(it.name)
         }
         showTimeLog("Main variant -- ${updatedRemoteAdConfig.adType}")
         updatedRemoteAdConfig.apply {
             if (isPremium().not() && isOnline(this@MainActivity)) {
                 if (adType == NATIVE_AD) {
                     loadFirstNativeAd()
                 } else if (adType == BANNER_AD) {
                     loadMainBannerAd()
                 }
             }
         }
     }*/
    private fun loadMainBannerAd() {
        if (isPremium().not() && isOnline(this@MainActivity)) {
            AdsManagerX.loadBannerAd(this,
                AdConfigManager.BANNER_AD_MAIN,
                binding.nativeAdContainer,
                onAdLoaded = {}, onAdFailed = {
                    loadMainNativeAd()
                })
        }
    }

    private fun loadMainNativeAd() {
        showTimeLog("requesting first ad main")
        if (isPremium().not() && isOnline(this@MainActivity)) {
            AdsManagerX.loadNativeAd(this,
                AdConfigManager.NATIVE_AD_MAIN.apply {
                    adConfig.nativeAdLayout =
                        R.layout.native_banner_new
                },
                binding.nativeAdContainer,
                onAdImpression = {
                    showTimeLog("ad shown")
                    startAdTimer()
                }, onAdFailed = {
                    mAdHandler.removeCallbacks(mAdRunnable)
                    binding.nativeAdContainer.gone()
                })
        }else{
            binding.nativeAdContainer.gone()
        }

    }


    private var mAdHandler = Handler(Looper.getMainLooper())
    private var interval: Long = 0
    private var isRefreshImpressionShown = false
    private val mAdRunnable = object : Runnable {
        override fun run() {
            reloadAdAfterInterval()
            mAdHandler.postDelayed(this, interval)
        }
    }

    private fun startAdTimer() {
        isRefreshImpressionShown = true
        Handler(Looper.getMainLooper()).postDelayed({ startAdRefreshTimer() }, 15000)
    }

    private fun startAdRefreshTimer() {
        interval = 15000L
        mAdHandler.post(mAdRunnable)
    }

    private fun reloadAdAfterInterval() {
        if (isRefreshImpressionShown) {
            isRefreshImpressionShown = false
            loadNativeAdForReload()
        } else {
            showTimeLog("previous ad impression not shown so not requesting next ad ")
        }
    }

    private fun loadNativeAdForReload() {
        if (isActivityPaused.not()) {
            showTimeLog("requesting refresh ad")
            if (isPremium().not() && isOnline(this@MainActivity)) {
                AdsManagerX.loadNativeAd(this,
                    AdConfigManager.NATIVE_AD_MAIN.apply {
                        adConfig.nativeAdLayout =
                            R.layout.native_banner_new
                    },
                    binding.nativeAdContainer,
                    onAdImpression = {
                        isRefreshImpressionShown = true
                        showTimeLog("refresh ad shown")
                    },
                    onAdFailed = {
                        showTimeLog("refresh ad failed.Stopping next requests")
                        mAdHandler.removeCallbacks(mAdRunnable)
                    })
            }
        } else {
            isRefreshImpressionShown = true
            showTimeLog("activity paused so not requesting refresh ad")
        }
    }

    private var isActivityPaused = true
    override fun onPause() {
        super.onPause()
        isActivityPaused = true
        exitDialog?.let {
            if (it.isVisible && it.isAdded) {
                it.dismiss()
            }
        }
    }

    private fun showTimeLog(from: String) {
        val df = SimpleDateFormat("hh:mm:ss a", Locale.US)
        Log.e("AdsLogsMain", "Main $from at ${df.format(System.currentTimeMillis())}")
    }

    private fun loadAppOpenAd() {
        if (intent.getBooleanExtra("splashToMain", false)) {
            AdsManagerX.loadAppOpenAd(
                this@MainActivity,
                AdConfigManager.APP_OPEN.apply {
                    adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                })
        }
    }
}