package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inputscreen

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProviders
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.IS_CHECK_OPEN_PLANS
import com.hm.admanagerx.IS_SHOW_TRANSLATE_INTER_AD
import com.hm.admanagerx.getBooleanRemoteConfigValue
import com.hm.admanagerx.getLongRemoteConfigValue
import com.hm.admanagerx.isAppOpenAdShow
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityInputBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.viewmodel.MainViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.onLanguageChange
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.onSwitchLanguages
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getLocale
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isSpeakerVisible
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchFullScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchLanguageScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.runBlocking

class InputActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityInputBinding
    private var srcLangCode: String = ""
    private var srcLangName: String = ""
    private var targetLangCode: String = ""
    private var targetLangName: String = ""

    private var favorite = false
    private var translationUtils: TranslationUtils? = null
    private var translationModel: TranslationHistory? = null
    private var myDataBase: MyDataBase? = null
    private var mTts: TextToSpeech? = null

    private var srcLangPosition: Int = Constants.DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = Constants.DEFAULT_TAR_LANG_POSITION

    //var bannerAd: AdView? = null

    private var fromDocs = false
    private var isSwitched = false

    private var isVoiceResultInterAdShown = false
    private var isFromMic = false
    private var isMicInterAdRequested = false

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)


        myDataBase = MyDataBase.getInstance(this)

        getLanguagesData()
        if (intent.hasExtra("from_docs")) {
            fromDocs = intent.getBooleanExtra("from_docs", false)
        }
        var hasData = intent.getStringExtra("has_data")
        isVoiceResultInterAdShown = intent.getBooleanExtra("isVoiceResultInterShow", false)
        if (hasData == null) {
            hasData = "no"
        }
        mTts = TextToSpeech(this, this, "com.google.android.tts")
        activateEditText(binding.etInput)
        setEditTextChangeListener()
        setClickListeners()
        if (hasData == "yes") {
            val inputWord = intent.getStringExtra(Constants.INPUT_TYPE_KEY)!!
            binding.etInput.setText(inputWord)
            if (fromDocs)
                binding.layoutActionGo.performClick()
            if (intent.hasExtra("from_mic")) {
                isFromMic = true
                checkAndTranslate()
                adConfig = AdConfigManager.BANNER_AD_VOICE_RESULT
            }
        } else if (intent.hasExtra("history_model")) {
            showData(intent.getParcelableExtra("history_model") as TranslationHistory?)
        }
        if (!isFromMic) {
            loadInterTranslateActionButton()
        }
        loadShowNativeAd()
    }

    private fun loadInterTranslateActionButton() {
        AdsManagerX.loadInterAd(this, AdConfigManager.INTER_AD_TRANSLATE_BUTTON)
    }

    private fun showData(translation: TranslationHistory?) {
        translation?.let {
            translationModel = it
            val inputWord = it.inputWord
            val translatedWord = it.translatedWord
            val inputLangCode = it.srcCode
            val inputLangPosition = viewModel.getLanguagePositionFromList(inputLangCode)
            val inputLanguageName = it.srcLang
            val translatedLangCode = it.trCode
            val translatedLanguageName = it.targetLang
            val translatedLangPosition = viewModel.getLanguagePositionFromList(translatedLangCode)
            favorite = it.isFavorite
            if (favorite)
                binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_history_fill)
            else
                binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_detail_unfill)
            binding.etInput.setText(inputWord)
            binding.etInput.setSelection(getInputWord().length)
            srcLangName = inputLanguageName
            srcLangCode = inputLangCode
            srcLangPosition = inputLangPosition

            viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
            viewModel.setLangData(SOURCE_LANG_CODE, srcLangCode)
            viewModel.putLangPosition(SOURCE_LANG_POSITION, srcLangPosition)
            binding.tvLangSrc.text = srcLangName


            targetLangName = translatedLanguageName
            targetLangCode = translatedLangCode
            targetLangPosition = translatedLangPosition
            viewModel.setLangData(TARGET_LANG_NAME, targetLangName)
            viewModel.setLangData(TARGET_LANG_CODE, targetLangCode)
            viewModel.putLangPosition(TARGET_LANG_POSITION, targetLangPosition)
            binding.tvLangTarget.text = targetLangName

//            binding.tvOutputLangName.text = targetLangName
            binding.tvOutputWord.text = translatedWord
            binding.layoutOutputAreaMain.visibility = View.VISIBLE
        }

    }

    private fun setClickListeners() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })

        binding.ivBackInput.setOnClickListener {
            backPressed()
        }
        binding.ivClearInput.setOnClickListener {
            resetData()
        }
        binding.newTranslationTV.setOnClickListener {
            if (mTts?.isSpeaking!!)
                mTts?.stop()
            if (fromDocs) {
                finish()
            } else {
                resetData()
            }
        }
        binding.layoutActionGo.setOnClickListener {
            isAppOpenAdShow(false)
            hideSoftKeyboard()
            if (!getBoolean(Constants.IS_PREMIUM)) {
                showTranslateInterAd()
            } else checkAndTranslate()
        }
        binding.layoutLanguagesLeft.setOnClickListener {
            if (isDoubleClick())
                openLanguageSheet(Constants.LANGUAGE_TYPE_SOURCE)
        }
        binding.layoutLanguagesRight.setOnClickListener {
            if (isDoubleClick())
                openLanguageSheet(Constants.LANGUAGE_TYPE_TARGET)
        }
        binding.layoutSwapInput.setOnClickListener {
            switchLanguages()
        }

        binding.ivStarFavoriteDetail.setOnClickListener {
            toggleFavorite()
        }
        binding.ivCopyTranslated.setOnClickListener {
            onClickCopy()
        }
        binding.ivShareTranslation.setOnClickListener {
            shareWord(binding.tvOutputWord.text.toString().trim())
        }
        binding.ivSpeakTranslated.setOnClickListener {
            onclickSpeaker()
        }
        binding.ivFullScreen.setOnClickListener {
            launchFullScreen(binding.tvOutputWord.text.toString().trim())
        }
    }

    private fun backPressed() {
        if (mTts?.isSpeaking!!)
            mTts?.stop()
        if (fromDocs) {
            finish()
            //launchMain(true)
        } else {
            showAd()
        }
    }

    private fun onClickCopy() {
        showToast(getString(R.string.text_copied_successfully), 0)
        val word = binding.tvOutputWord.text.toString().trim()
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val sb = StringBuilder()

        sb.append(word)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("ocr_copy", sb.toString()))
    }

    private fun onclickSpeaker() {
        mTts?.let {
            if (it.isSpeaking)
                it.stop()
            else {
                speakTranslation(binding.tvOutputWord.text.toString().trim(), targetLangCode)
            }
        }
    }

    private fun checkAndTranslate() {
        val wordText = binding.etInput.text.toString().trim()
        if (wordText.isNotEmpty()) {
            binding.newTranslationTV.gone()
            binding.layoutOutputAreaMain.gone()
            binding.spaceView.gone()
            favorite = false
            binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_detail_unfill)
            translationModel = null
            searchInputWord(wordText)
        }
    }

    private fun searchInputWord(input: String) {
        if (Controller.isOnline(this)) {
            pauseEngine()
            binding.scrollMain.post { binding.scrollMain.fullScroll(ScrollView.FOCUS_DOWN) }
            binding.progressMain.visibility = View.VISIBLE

            translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
                override fun onFailedResult() {
                    runOnUiThread {
                        showToast(resources.getString(R.string.stt_error_network_error), 0)
                        binding.progressMain.visibility = View.GONE
                    }
                }

                override fun onReceiveResult(result: String?) {
                    runOnUiThread {
                        result?.let {
                            val outputWord = it
                            presentData(input, outputWord)
                        }

                    }
                }
            }, input, srcLangCode, targetLangCode)
            translationUtils!!.execute()


        } else {
            showToast(getString(R.string.check_internet_connection), 0)
        }
    }

    private fun speakTranslation(speakingWord: String, langCode: String) {
        val langLocal = getLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }

    private fun pauseEngine() {
        checkTTSSpeaking()
    }

    private fun stopEngine() {
        mTts?.let {
            if (it.isSpeaking)
                it.shutdown()
        }
    }

    private fun checkTTSSpeaking() {
        mTts?.let {
            if (it.isSpeaking)
                it.stop()
        }
    }

    private fun resetData() {
        if (mTts?.isSpeaking!!)
            mTts?.stop()
        binding.newTranslationTV.gone()
        binding.layoutOutputAreaMain.gone()
        binding.spaceView.gone()
        favorite = false
        binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_detail_unfill)
        translationModel = null
        binding.etInput.setText("")
        binding.ivClearInput.gone()
    }

    private fun toggleFavorite() {
        favorite = !favorite
        if (favorite) {
            binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_history_fill)
            Toast.makeText(this, getString(R.string.added_to_favourite), Toast.LENGTH_SHORT).show()
        } else
            binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_detail_unfill)
        runBlocking {
            translationModel!!.apply {
                isFavorite = favorite
                updateFavorite(this)
            }
        }

    }

    private fun updateFavorite(translationHistory: TranslationHistory) {
        kotlin.runCatching {
            myDataBase!!.translationDao()
                .update(translationHistory.primaryId, translationHistory.isFavorite)
        }
    }

    private fun presentData(mInput: String, mOutput: String) {
        val mPrimaryId = mInput + srcLangName + targetLangName + mOutput
        translationModel = TranslationHistory().apply {
            inputWord = mInput
            translatedWord = mOutput
            srcLang = srcLangName
            targetLang = targetLangName
            srcCode = srcLangCode
            trCode = targetLangCode
            primaryId = mPrimaryId
            isFavorite = false
        }
//        binding.layoutMain.ivStarFavoriteDetail.visible()
        insertHistory(translationModel!!)
//        binding.layoutMain.tvOutputLangName.text = targetLangName
        binding.tvOutputWord.text = mOutput
        binding.layoutOutputAreaMain.visible()
        binding.spaceView.visible()
        binding.newTranslationTV.visible()
        binding.progressMain.gone()
        binding.layoutActionGo.gone()
        checkTranslatedSpeakerVisibility()
    }

    private fun insertHistory(
        translationHistory: TranslationHistory,
    ) {
        runBlocking {
            viewModel.insertHistory(translationHistory)
        }
    }

    private fun checkTranslatedSpeakerVisibility() {
        binding.ivSpeakTranslated.visibility = if (isSpeakerVisible(targetLangCode))
            View.VISIBLE
        else
            View.GONE
    }

    /*private fun checkForAd() {
        showTranslateInterAd()
    }*/

    private fun openLanguageSheet(type: String) {
        launchLanguageScreen(type, "main")
    }

    private fun setEditTextChangeListener() {
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (char.toString().trim().isNotEmpty()) {
                    binding.ivClearInput.visible()
                    binding.layoutActionGo.visible()
                    binding.rippleMic.startRippleAnimation()
                    /* if (isFromMic && !AdsManagerX.isInterAdLoaded(AdConfigManager.INTER_AD_TRANSLATE_BUTTON) && !isMicInterAdRequested) {
                         isMicInterAdRequested = true
                         loadInterTranslateActionButton()
                     }*/
                } else {
                    binding.ivClearInput.gone()
                    binding.layoutActionGo.gone()
                    binding.rippleMic.stopRippleAnimation()
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun activateEditText(etInput: AppCompatEditText) {
        etInput.post {
            etInput.isActivated = true
            etInput.isPressed = true
            etInput.isCursorVisible = true
            etInput.requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                etInput.focusable = View.FOCUSABLE
            }
            etInput.setSelection(etInput.text.toString().length)
        }

    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(Constants.SOURCE_LANG_CODE)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(Constants.SOURCE_LANG_CODE, srcLangCode)
        }
        srcLangName = viewModel.getLangData(SOURCE_LANG_NAME)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
        }

        srcLangPosition = viewModel.getLangPosition(Constants.SOURCE_LANG_POSITION)

        if (srcLangPosition == -1) {
            srcLangPosition = Constants.DEFAULT_SRC_LANG_POSITION
            viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION, srcLangPosition)
        }

        targetLangCode = viewModel.getLangData(Constants.TARGET_LANG_CODE)
        if (targetLangCode == "") {
            targetLangCode = "fr"
            viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
        }

        targetLangName = viewModel.getLangData(Constants.TARGET_LANG_NAME)
        if (targetLangName == "") {
            targetLangName = "French"
            viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
        }

        targetLangPosition = viewModel.getLangPosition(Constants.TARGET_LANG_POSITION)
        if (targetLangPosition == -1) {
            targetLangPosition = Constants.DEFAULT_TAR_LANG_POSITION
            viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
        }
        setLanguagesNames()
    }

    private fun setLanguagesNames() {
        binding.tvLangSrc.text = srcLangName
        binding.tvLangTarget.text = targetLangName
    }

    private fun getInputWord(): String {
        return binding.etInput.text.toString().trim()
    }

    private var mLastClickTime: Long = 0

    private fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private fun switchLanguages() {


        if (isSwitched) {
            isSwitched = false
            binding.ivLangArrowsMain.animate().rotation(-180f).start()
        } else {
            isSwitched = true
            binding.ivLangArrowsMain.animate().rotation(180f).start()
        }
        val tempSrcCode = srcLangCode
        val temSrcName = srcLangName
        val temSrcPosition = srcLangPosition

        val temTarCode = targetLangCode
        val temTarName = targetLangName
        val temTarPosition = targetLangPosition

        srcLangCode = temTarCode
        srcLangName = temTarName
        srcLangPosition = temTarPosition

        targetLangCode = tempSrcCode
        targetLangName = temSrcName
        targetLangPosition = temSrcPosition


        viewModel.setLangData(Constants.SOURCE_LANG_CODE, srcLangCode)
        viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
        viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION, srcLangPosition)

        viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
        viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
        viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
        setLanguagesNames()
        onSwitchLanguages?.invoke()
    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {
        langModel?.let { model ->
            onLanguageChange?.invoke(
                type!!,
                model,
                position
            )

            if (type == Constants.LANGUAGE_TYPE_SOURCE) {
                srcLangName = model.languageName
                srcLangCode = model.languageCode
                srcLangPosition = position

                viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
                viewModel.setLangData(Constants.SOURCE_LANG_CODE, srcLangCode)
                viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION, srcLangPosition)


                binding.tvLangSrc.text = srcLangName


            } else if (type == Constants.LANGUAGE_TYPE_TARGET) {
                targetLangName = model.languageName
                targetLangCode = model.languageCode
                targetLangPosition = position

                viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
                viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
                viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
                binding.tvLangTarget.text = targetLangName

            }
        }

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            mTts!!.setSpeechRate(0.9f)
            mTts!!.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {


                }

                override fun onDone(utteranceId: String) {

                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    super.onStop(utteranceId, interrupted)

                }

                override fun onError(utteranceId: String) {


                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                }

            })

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_LANG_SELECTOR) {

            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val langModel = getParcelableExtra("language_model") as LanguageModel?
                    val langType = getStringExtra("language_type")
                    val langPosition = getIntExtra("language_position", 0)
                    setLanguages(langType, langModel, langPosition)
                    checkAndTranslate()
                }

            }
        }
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//    }

    private fun showTranslateInterAd() {
        if (!isFromMic) {
            if (AdsManagerX.isAppOpenAdShowing(AdConfigManager.APP_OPEN)) {
                checkAndTranslate()
            } else {
                val showtr_adars = IS_SHOW_TRANSLATE_INTER_AD.getBooleanRemoteConfigValue()

                Log.d("showtr_adars", "showtr_adars = $showtr_adars")
                if (showtr_adars) {
                    AdsManagerX.showInterAd(
                        this@InputActivity,
                        AdConfigManager.INTER_AD_TRANSLATE_BUTTON.apply {
                            adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                        },
                        onAdClose = {
                            checkAndTranslate()
                        },
                        funBlock = {
                            checkAndTranslate()
                        })
                }
            }
        } else {
            checkAndTranslate()
        }

    }

    private fun showAd() {
        if (!isVoiceResultInterAdShown && isFromMic) {
            AdsManagerX.showInterAd(
                this@InputActivity,
                AdConfigManager.INTER_AD_VOICE_RESULT_FINDING.apply {
                    adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                },
                onAdClose = {
                    finish()
                },
                funBlock = {
                    finish()
                })
        } else {
            finish()
        }
        /*  showInterstitialAdBase(
              RemoteConfigValues.INPUT_SCREEN_BACK_INTER_AD,
              preAdShow = {

              },
              onClosed = {
                  finish()
              },
              onFailedToShow = {
                  finish()
              },
              onToShow = {}
          )*/
    }

    override fun onPause() {
        //     bannerAd?.pause()
        super.onPause()
        mTts?.let {
            if (it.isSpeaking)
                it.stop()
        }
    }

    override fun onDestroy() {
        //bannerAd?.destroy()
        super.onDestroy()
        mTts?.let {
            it.stop()
            it.shutdown()
        }
        isVoiceResultInterAdShown = false
        isFromMic = false
        // isMicInterAdRequested = false
        translationUtils?.StopBackground()

    }

    var adConfig = AdConfigManager.NATIVE_AD_TRANSLATE
    private fun loadShowNativeAd() {
        if (isPremium().not() && isOnline(this@InputActivity)) {
            binding.bannerContainerParent.visible()
            AdsManagerX.loadNativeAd(
                this@InputActivity,
                adConfig.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_seventy
                }, binding.bannerContainer, onAdFailed = {
                    binding.bannerContainerParent.gone()
                }
            )
        } else {
            binding.bannerContainerParent.gone()
        }
    }
}