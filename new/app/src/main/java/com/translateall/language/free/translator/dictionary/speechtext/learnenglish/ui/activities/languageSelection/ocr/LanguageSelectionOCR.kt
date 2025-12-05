package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.ocr

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityLanguageSelectionBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.adapter.LanguageViewPagerAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.generic.LanguageSelectionFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.viewModel.LanguageViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_LIST_TYPE_ALL
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_LIST_TYPE_OCR
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_SOURCE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_TARGET
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_CODE_OCR
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_NAME_OCR
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.fetchAllLanguages
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.langsDataOCr
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.hide
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.show

class LanguageSelectionOCR : BaseActivity() {
    private lateinit var binding: ActivityLanguageSelectionBinding
    private var langType: String? = null
    private var languagePagerAdapter: LanguageViewPagerAdapter? = null

    //var bannerAd: AdView? = null

    private val viewModel: LanguageViewModel by lazy {
        ViewModelProviders.of(this).get(LanguageViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
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

        langType = intent.getStringExtra("language_type")
        getLanguagesData()
        Handler().postDelayed({
            setViewPager()
            setLangTabs()
            getSelectedLanguageData()

            setClickListeners()
            setFilter()
        }, 50)
        loadShowNativeAd()
    }

    private fun setFilter() {
        binding.etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim() != "") {
                    filterLanguage(s.toString())
                } else {
                    getCurrentLanguageFragment()?.setUnFilteredList()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun filterLanguage(s: String) {
        getCurrentLanguageFragment()?.filterLanguage(s)

    }

    private fun getCurrentLanguageFragment(): LanguageSelectionFragment? {
        return languagePagerAdapter?.getRegisteredFragment(binding.viewPagerLangs.currentItem) as LanguageSelectionFragment?
    }

    private fun setClickListeners() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })
        binding.layoutLanguagesSrc.setOnClickListener {
            langType = LANGUAGE_TYPE_SOURCE
            binding.viewPagerLangs.currentItem = 0
        }
        binding.layoutLanguagesTarget.setOnClickListener {
            langType = LANGUAGE_TYPE_TARGET
            binding.viewPagerLangs.currentItem = 1
        }
    }

    private fun getSelectedLanguageData() {
        AppUtils.onSelectLanguage = { langModel, position ->
            setDataAndFinish(langModel, position)
        }
    }

    private fun setDataAndFinish(langModel: LanguageModel, position: Int) {
        val intent = Intent()
        val name = langModel.languageName
        val languages: MutableList<String> = ArrayList()
        val langsList = if (langType == LANGUAGE_TYPE_SOURCE) {
            langsDataOCr()
        } else {
            fetchAllLanguages()
        }
        for (language in langsList) {
            languages.add(language.languageName)
        }

        val selectedId = languages.indexOf(name)

        intent.putExtra("language_type", langType)
        intent.putExtra("language_model", langModel)
        intent.putExtra("language_position", selectedId)
        setResult(Activity.RESULT_OK, intent)

        finish()

    }

    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getFragment(source: String, list: String): Fragment {
        return LanguageSelectionFragment.newInstance(source, list, "ocr")
    }


    private fun setViewPager() {
        languagePagerAdapter = LanguageViewPagerAdapter(supportFragmentManager)
        languagePagerAdapter?.addFragment(getFragment(LANGUAGE_TYPE_SOURCE, LANGUAGE_LIST_TYPE_OCR))
        languagePagerAdapter?.addFragment(getFragment(LANGUAGE_TYPE_TARGET, LANGUAGE_LIST_TYPE_ALL))

        binding.viewPagerLangs.adapter = languagePagerAdapter
        binding.viewPagerLangs.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {

            }

            override fun onPageSelected(position: Int) {
                langType = if (position == 0) {
                    LANGUAGE_TYPE_SOURCE
                } else {
                    LANGUAGE_TYPE_TARGET
                }
                setLangTabs()

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        if (langType == LANGUAGE_TYPE_SOURCE) {
            binding.viewPagerLangs.currentItem = 0
        } else {
            binding.viewPagerLangs.currentItem = 1

        }
    }

    private fun setLangTabs() {
        if (langType == LANGUAGE_TYPE_SOURCE) {
            setSourceLanguageTab()
        } else {
            setTargetLanguageTab()
        }
    }

    private fun setSourceLanguageTab() {
        binding.layoutLanguagesSrc.background = resources.getDrawable(R.drawable.bg_lang_selected)
        binding.tvSrcNameSelection.setTextColor(resources.getColor(R.color.white))
        binding.layoutLanguagesTarget.background =
            resources.getDrawable(R.drawable.bg_lang_unselected)
        binding.tvTargetNameSelection.setTextColor(resources.getColor(R.color.color_lang_un_selected))
    }

    private fun setTargetLanguageTab() {
        binding.layoutLanguagesTarget.background =
            resources.getDrawable(R.drawable.bg_lang_selected)
        binding.tvTargetNameSelection.setTextColor(resources.getColor(R.color.white))
        binding.layoutLanguagesSrc.background = resources.getDrawable(R.drawable.bg_lang_unselected)
        binding.tvSrcNameSelection.setTextColor(resources.getColor(R.color.color_lang_un_selected))
    }

    private fun getLanguagesData() {


        var srcLangCode = viewModel.getLangData(SOURCE_LANG_CODE_OCR)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(SOURCE_LANG_CODE_OCR, srcLangCode)
        }
        var srcLangName = viewModel.getLangData(SOURCE_LANG_NAME_OCR)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(SOURCE_LANG_NAME_OCR, srcLangName)
        }
        var targetLangCode = viewModel.getLangData(Constants.TARGET_LANG_CODE)
        if (targetLangCode == "") {
            targetLangCode = "fr"
            viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
        }

        var targetLangName = viewModel.getLangData(Constants.TARGET_LANG_NAME)
        if (targetLangName == "") {
            targetLangName = "French"
            viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
        }

        binding.tvSrcNameSelection.text = srcLangName
        binding.tvTargetNameSelection.text = targetLangName

    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(this)
    }

    override fun finish() {
        super.finish()
//        overridePendingTransition(R.anim.transit_none, R.anim.transit_top_bottom)

    }

    private fun loadShowNativeAd() {
        if (isPremium().not() && isOnline(this@LanguageSelectionOCR)) {
            binding.bannerContainerParent.show()
            AdsManagerX.loadNativeAd(
                this@LanguageSelectionOCR,
                AdConfigManager.NATIVE_AD_LANGUAGE_SELECTION.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_seventy
                }, binding.bannerContainer, onAdFailed = {
                    binding.bannerContainerParent.hide()
                }
            )
        } else {
            binding.bannerContainerParent.hide()
        }
    }
}