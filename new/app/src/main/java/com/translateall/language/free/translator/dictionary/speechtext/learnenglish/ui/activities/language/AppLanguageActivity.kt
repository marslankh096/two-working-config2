package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.language

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.IS_CHECK_OPEN_ONBOARD
import com.hm.admanagerx.getBooleanRemoteConfigValue
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityAppLanguageBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.onboarding.OnboardingActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.ExitDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getDefaultLangCode
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getFlags
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getSelectedLanguageCode
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getSelectedLanguageName
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.languageList
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class AppLanguageActivity : BaseActivity() {
    private lateinit var binding: ActivityAppLanguageBinding
    private var adapter: LanguageAdapter? = null
    private var exitDialog: ExitDialog? = null
    private var isCheckLanguageReload = false

    private var languages = ""
    var isFromSplash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppLanguageBinding.inflate(layoutInflater)
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
    }

    private fun init() {
        isFromSplash = intent.getBooleanExtra("isFromSplash", false)
        setAdapter()
        loadShowNativeAd()
        //Load App Open Ad If Comes Directly from Splash
        loadAppOpenAd()
        exitDialog = ExitDialog.newInstance()
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })
        binding.ivBack.setOnClickListener {
            if (isDoubleClick()) {
                backPressed()
            }
        }
        binding.doneIV.setOnClickListener {
            if (isDoubleClick()) {
                adapter?.let {
                    saveLanguage(it.selectedLanguage)
                }
            }
        }
        exitDialog?.initListener(object : ExitDialog.ExitClickListener {
            override fun onExitClick() {
                (this@AppLanguageActivity.application as TranslateApplication).backFromOnboarding =
                    false
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if ((this.application as TranslateApplication).backFromOnboarding) {
            TinyDB.getInstance(this).putLanguage("")
        }
        if (isPremium())
            binding.adsCV.gone()
    }

    private fun setAdapter() {
        languages = TinyDB.getInstance(this).language
        val languageList = languageList()
        val flagList = getFlags()

        adapter = LanguageAdapter(languageList, flagList) {
            binding.doneIV.visible()
        }

        binding.langRV.layoutManager = LinearLayoutManager(this)
        binding.langRV.adapter = adapter

        if (languages.isEmpty()) {
            binding.doneIV.gone()
        } else {
            binding.doneIV.visible()
            adapter?.selectedLanguage = getSelectedLanguageName(languages)
            adapter?.notifyDataSetChanged()
        }

    }

    private fun saveLanguage(selectedLanguage: String) {
        val langCode = getSelectedLanguageCode(selectedLanguage)
        if (langCode == TinyDB.getInstance(this).language) {
            backPressed()
        } else {
            TinyDB.getInstance(this).putLanguage(langCode)
            launchNextScreen()
        }
    }

    private fun launchMain() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    private fun backPressed() {
        if ((this.application as TranslateApplication).backFromOnboarding) {
            exitDialog?.let {
                if (!it.isVisible)
                    it.show(supportFragmentManager, "lang_exit_dialog")
            }
        } else if (TinyDB.getInstance(this).language.isEmpty()) {
            saveDefaultLangCode()
        } else {
            finish()
        }
    }

    private fun saveDefaultLangCode() {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        }
        // en_Us to split en
        val defaultSystemLang = locale.toString()
        val langCode = if (defaultSystemLang.contains("_")) {
            defaultSystemLang.split("_")[0]
        } else {
            locale.toString()
        }
        TinyDB.getInstance(this).putLanguage(getDefaultLangCode(langCode))
        launchNextScreen()
    }

    private fun launchNextScreen() {
        AdsManagerX.showInterAd(this@AppLanguageActivity,
            AdConfigManager.INTER_AD_LANGUAGE,
            onAdClose = {
                moveNextScreen()
            },
            funBlock = {
                moveNextScreen()
            })
    }

    private fun moveNextScreen() {
        if (TinyDB.getInstance(this).showOnboarding()) {
            val showOnboarding =
                IS_CHECK_OPEN_ONBOARD.getBooleanRemoteConfigValue()
            if (showOnboarding) {
                startActivity(
                    Intent(
                        this@AppLanguageActivity,
                        OnboardingActivity::class.java
                    )
                )
            } else {
                launchMain()
            }

        } else {
            launchMain()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isFromSplash = false
        if (AdsManagerX.isNativeAdLoaded(AdConfigManager.NATIVE_AD_LANGUAGE)) {
            AdsManagerX.destroyNativeAd(AdConfigManager.NATIVE_AD_LANGUAGE)
        }
    }

    private fun loadShowNativeAd() {
        if (!isPremium() && Controller.isOnline(this) && isFromSplash) {
            AdsManagerX.loadNativeAd(
                this@AppLanguageActivity,
                AdConfigManager.NATIVE_AD_LANGUAGE.apply {
                    adConfig.nativeAdLayout =
                        com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R.layout.custom_native_big_layout
                },
                binding.nativeAdView, onAdLoaded = {
                    binding.adsCV.visible()
                }, onAdFailed = {
                    binding.adsCV.gone()
                }
            )
        }
    }

    private fun loadAppOpenAd() {
        if (isFromSplash) {
            AdsManagerX.loadAppOpenAd(
                this,
                AdConfigManager.APP_OPEN.apply {
                    adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                })
        }
    }
}