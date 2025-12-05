package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.conversation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.isAppOpenAdShow
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.SubscriptionServiceListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityConversationBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.LIFE_TIME_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.MONTHLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.WEEKLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.YEARLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inputscreen.InputActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.viewmodel.MainViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.premiumActive
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isSpeakerVisible
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchFullScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchLanguageScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import java.text.DecimalFormat
import java.util.Objects

class ConversationActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityConversationBinding
    private var srcLangCode: String = ""
    private var srcLangName: String = ""
    private var targetLangCode: String = ""
    private var targetLangName: String = ""

    private var canSubscribe = false

    private var srcLangPosition: Int = Constants.DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = Constants.DEFAULT_TAR_LANG_POSITION

    private var conversationClickType = ConversationClickType.NONE
    private var mTts: TextToSpeech? = null
    private var isTranslationAvailable = false
    private var translationUtils: TranslationUtils? = null
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private var iapConnector: IapConnector? = null

    //var bannerAd: AdView? = null

    private enum class ConversationClickType {
        NONE, LEFT, RIGHT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
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

        setClicksListeners()
        getLanguagesData()
        mTts = TextToSpeech(this, this, "com.google.android.tts")
        //showBanner()
        if (!isPremium()) {
            settingUpWeeklySubscription()
        }
        if (isPremium().not() && isOnline(this@ConversationActivity)) {
            AdsManagerX.loadInterAd(this, AdConfigManager.INTER_AD_CONVERSATION)
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
                }


            }
        } else if (requestCode == Constants.REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                val recognizedResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (recognizedResult != null) {
                    val inputWord = recognizedResult[0]
                    if (AdsManagerX.isAppOpenAdShowing(AdConfigManager.APP_OPEN)){
                        setInputFieldText(inputWord)
                    }else{
                        AdsManagerX.showInterAd(
                            this@ConversationActivity,
                            AdConfigManager.INTER_AD_CONVERSATION.apply {
                                adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                            },
                            onAdClose = {
                                setInputFieldText(inputWord)
                            },
                            funBlock = {
                                setInputFieldText(inputWord)
                            })
                    }

                }
            }
        }
    }


    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
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

    private fun setClicksListeners() {

        setBottomScrollClickListener()
        setTopScrollClickListener()
        setEditTextListeners()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.layoutPremium.layoutPremiumMain.visibility != View.VISIBLE) {
                        finish()
                    }

                }
            })

        binding.ivBack.setOnClickListener {
            if (binding.layoutPremium.layoutPremiumMain.visibility != View.VISIBLE) {
                finish()
            }
        }

        binding.layoutConLangTo.setOnClickListener {
            if (isDoubleClick())
                openLanguageSheet(Constants.LANGUAGE_TYPE_TARGET)
        }
        binding.layoutConLangFrom.setOnClickListener {
            if (isDoubleClick())
                openLanguageSheet(Constants.LANGUAGE_TYPE_SOURCE)
        }

        binding.ivCrossBottom.setOnClickListener {
            conversationClickType = ConversationClickType.NONE
            mTts?.stop()
            binding.tvWordBottom.reset()
            binding.etInputBottom.hideEditText()
            binding.tvWordTop.gone()
            binding.ivCrossBottom.gone()
            binding.ivArrowTop.gone()
            binding.layoutAreaActionsTop.gone()
            binding.layoutAreaActionsBottom.gone()
        }

        binding.ivCrossTop.setOnClickListener {
            conversationClickType = ConversationClickType.NONE
            mTts?.stop()
            binding.tvWordTop.reset()
            binding.etInputTop.hideEditText()
            binding.etInputBottom.hideEditText()
            binding.tvWordBottom.gone()
            binding.ivCrossTop.gone()
            binding.ivArrowBottom.gone()
            binding.layoutAreaActionsTop.gone()
            binding.layoutAreaActionsBottom.gone()
        }

        binding.ivArrowBottom.setOnClickListener {
            showResult()
        }
        binding.ivArrowTop.setOnClickListener {
            showResult()
        }

        binding.layoutInputRight.setOnClickListener {
            binding.tvHintOutput.gone()
            binding.tvHintInput.gone()
            conversationClickType = ConversationClickType.RIGHT
            initConversation()
        }
        binding.layoutInputLeft.setOnClickListener {
            binding.tvHintOutput.gone()
            binding.tvHintInput.gone()
            conversationClickType = ConversationClickType.LEFT
            initConversation()
        }

        binding.layoutContainerTop.setOnClickListener {
            if (mTts?.isSpeaking!!)
                mTts?.stop()

            val word = binding.tvWordTop.text.toString().trim()
            if (conversationClickType == ConversationClickType.LEFT && isTranslationAvailable) {
                if (binding.etInputTop.text.toString().trim().isEmpty()) {
                    binding.layoutContainerTop.gone()
                    binding.tvWordTop.gone()
                    binding.etInputTop.isClickable = true
                    binding.etInputTop.setText(word)
                    binding.ivArrowBottom.gone()
                    binding.ivArrowTop.gone()

                }

                setEditTextStateActive(binding.etInputTop)
            }

        }
        binding.layoutContainerBottom.setOnClickListener {
            if (mTts?.isSpeaking!!)
                mTts?.stop()
            val word = binding.tvWordBottom.text.toString().trim()

            if (conversationClickType == ConversationClickType.RIGHT && isTranslationAvailable) {
                if (binding.etInputBottom.text.toString().trim().isEmpty()) {
                    binding.layoutContainerBottom.gone()
                    binding.ivArrowBottom.gone()
                    binding.ivArrowTop.gone()
                    binding.tvWordBottom.gone()
                    binding.etInputBottom.setText(word)
                    binding.etInputBottom.isClickable = false

                }
                setEditTextStateActive(binding.etInputBottom)

            }

        }

        binding.ivSpeakerTop.setOnClickListener {
            mTts?.stop()
            val word = binding.tvWordTop.text.toString().trim()
            speakWord(srcLangCode, word)
        }
        binding.ivSpeakerBottom.setOnClickListener {
            mTts?.stop()
            val word = binding.tvWordBottom.text.toString().trim()
            speakWord(targetLangCode, word)
        }

        binding.ivCopyConversationTop.setOnClickListener {
            copyText(binding.tvWordTop.text.toString().trim())
        }
        binding.ivCopyConversationBottom.setOnClickListener {
            copyText(binding.tvWordBottom.text.toString().trim())
        }

        binding.ivShareConversationTop.setOnClickListener {
            shareWord(binding.tvWordTop.text.toString().trim())
        }
        binding.ivShareConversationBottom.setOnClickListener {
            shareWord(binding.tvWordBottom.text.toString().trim())
        }

        binding.ivFullConversationTop.setOnClickListener {
            launchFullScreen(binding.tvWordTop.text.toString().trim())
        }
        binding.ivFullConversationBottom.setOnClickListener {
            launchFullScreen(binding.tvWordBottom.text.toString().trim())
        }
        binding.layoutPremium.premiumButton.setOnClickListener {
            if (isDoubleClick()) {
                iapConnector?.subscribe(this, WEEKLY_SUBS_ID)
            }
        }
        binding.layoutPremium.ivClosePremium.setOnClickListener { binding.layoutPremium.layoutPremiumMain.gone() }
        binding.layoutPremium.layoutPremiumMain.setOnClickListener {}

    }

    private fun settingUpWeeklySubscription() {

        binding.layoutPremium.tvPremiumDetail

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

            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {}

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

                        YEARLY_SUBS_ID -> {}

                        MONTHLY_SUBS_ID -> {}

                        LIFE_TIME_SUBS_ID -> {
//                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
//                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))
//
//                            binding.layoutPremium.tvPremiumDetail.text =
//                                String.format(getString(R.string.just_lifetime), priceTxt)
                        }

                        WEEKLY_SUBS_ID -> {
                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))

                            binding.layoutPremium.tvPremiumDetail.text =
                                String.format(
                                    getString(R.string.just_1_s_weekly_cancel_anytime),
                                    priceTxt
                                )
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

    fun activatePremium() {
        binding.layoutPremium.layoutPremiumMain.gone()
        premiumActive.postValue(true)
        TinyDB.getInstance(this@ConversationActivity)
            .putBoolean(Constants.IS_PREMIUM, true)
        //  binding.convAdsBanner.root.gone()
    }

    private fun openLanguageSheet(type: String) {
        launchLanguageScreen(type, "conversation")
    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(Constants.SOURCE_LANG_CODE)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(Constants.SOURCE_LANG_CODE, srcLangCode)
        }
        srcLangName = viewModel.getLangData(Constants.SOURCE_LANG_NAME)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(Constants.SOURCE_LANG_NAME, srcLangName)
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

        binding.tvLanguageFrom.text = srcLangName
        binding.tvLanguageTo.text = targetLangName

        if (srcLangCode == "en") {
            binding.tvHintInput.setText(getString(R.string.hintText))
            callHintTranslation(srcLangCode, targetLangCode, false)
        } else {
            callHintTranslation("en", srcLangCode, true)
            callHintTranslation("en", targetLangCode, false)
        }

    }

    private var mLastClickTime: Long = 0
    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {
        langModel?.let { model ->
            AppUtils.onLanguageChange?.invoke(
                type!!,
                model,
                position
            )

            if (type == Constants.LANGUAGE_TYPE_SOURCE) {
                srcLangName = model.languageName
                srcLangCode = model.languageCode
                srcLangPosition = position

                viewModel.setLangData(Constants.SOURCE_LANG_NAME, srcLangName)
                viewModel.setLangData(Constants.SOURCE_LANG_CODE, srcLangCode)
                viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION, srcLangPosition)
                binding.tvLanguageFrom.text = srcLangName


            } else if (type == Constants.LANGUAGE_TYPE_TARGET) {
                targetLangName = model.languageName
                targetLangCode = model.languageCode
                targetLangPosition = position

                viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
                viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
                viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
                binding.tvLanguageTo.text = targetLangName
            }
        }


    }

    private fun initConversation() {
        binding.layoutContainerTop.visible()
        binding.layoutContainerBottom.visible()
        binding.ivArrowBottom.gone()
        binding.ivArrowTop.gone()
        binding.etInputTop.hideEditText()
        binding.etInputBottom.hideEditText()
        mTts?.stop()
        if (Build.VERSION.SDK_INT >= 30) {
            initMic()
        } else {
            if (viewModel.isAppInstalled(this, "com.google.android.googlequicksearchbox")
                ||
                viewModel.isAppInstalled(this, "com.google.android.apps.searchlite")
            ) {
                initMic()
            } else {
                showToast(getString(R.string.message_app_install), 0)
            }
        }
    }

    private fun initMic() {
        val languageCode = if (conversationClickType == ConversationClickType.LEFT) {
            srcLangCode
        } else {
            targetLangCode
        }
        viewModel.getMicCode(languageCode)?.let {
            speakIn(it)
        }

    }

    private fun speakIn(code: String) {
        val micIntent = viewModel.getRecognizerIntent(code)
        isTranslationAvailable = false
        try {
            isAppOpenAdShow(false)
            startActivityForResult(micIntent, Constants.REQ_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resources.getString(R.string.stt_error_device), 0)
        }
    }

    private fun AppCompatEditText.hideEditText() {
        this.setText("")
        this.gone()
        this.hideSoftKeyboard()
        isClickable = false

    }

    private fun AppCompatEditText.hideSoftKeyboard() {
        (Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).hideSoftInputFromWindow(
            this.applicationWindowToken,
            0
        )
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        clearFocus()
    }

    private fun TextView.reset() {
        this.text = ""
        this.visible()
    }

    private fun TextView.setInput(word: String) {
        text = word
        setTextColor(resources.getColor(R.color.ripple_color_left))
        visible()
    }

    private fun TextView.setOutPut(word: String) {
        text = word
        setTextColor(resources.getColor(R.color.color_conversation_translation))
        visible()
    }

    private fun setInputFieldText(inputWord: String) {
        binding.tvWordTop.reset()
        binding.tvWordBottom.reset()
        binding.ivCrossTop.gone()
        binding.ivCrossBottom.gone()
        binding.layoutAreaActionsTop.gone()
        binding.layoutAreaActionsBottom.gone()

        var inputCode = ""
        var targetCode = ""

        if (conversationClickType == ConversationClickType.LEFT) {
            inputCode = srcLangCode
            targetCode = targetLangCode
            binding.tvWordTop.setInput(inputWord)
            binding.progressBottom.visible()
        } else {
            inputCode = targetLangCode
            targetCode = srcLangCode
            binding.tvWordBottom.setInput(inputWord)
            binding.progressTop.visible()
        }
        if (Controller.isOnline(this)) {

            callTranslation(inputWord, inputCode, targetCode)
        } else {
            showToast(getString(R.string.check_internet_connection), 0)
        }
    }

    private fun callTranslation(inputWord: String, inputCode: String, targetCode: String) {
        translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
            override fun onFailedResult() {
                runOnUiThread {
                    handleErrorResponse(resources.getString(R.string.stt_error_network_error))
                }
            }

            override fun onReceiveResult(result: String?) {
                runOnUiThread {
                    result?.let {
                        val outputWord = it
                        presentData(inputWord, outputWord)
                    }

                }
            }
        }, inputWord, inputCode, targetCode)
        translationUtils!!.execute()
    }

    private fun callHintTranslation(inputCode: String, targetCode: String, isInput: Boolean) {
        val translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
            override fun onFailedResult() {

            }

            override fun onReceiveResult(result: String?) {
                runOnUiThread {
                    result?.let {
                        val outputWord = it
                        if (isInput) {
                            binding.tvHintInput.setText(outputWord)
                        } else
                            binding.tvHintOutput.setText(outputWord)
                    }

                }
            }
        }, getString(R.string.hintText), inputCode, targetCode)
        translationUtils!!.execute()
    }

    private fun presentData(inputWord: String, outputWord: String) {
        if (!isPremium() && !(application as TranslateApplication).conversationPremiumShown) {
            (application as TranslateApplication).conversationPremiumShown = true
            binding.layoutPremium.layoutPremiumMain.visible()
        }

        var speakerCode = ""
        binding.layoutAreaActionsTop.visible()
        binding.layoutAreaActionsBottom.visible()
        isTranslationAvailable = true


        binding.ivSpeakerBottom.visibility =
            if (isSpeakerVisible(targetLangCode)) View.VISIBLE else View.GONE

        binding.ivSpeakerTop.visibility =
            if (isSpeakerVisible(srcLangCode)) View.VISIBLE else View.GONE

        if (conversationClickType == ConversationClickType.LEFT) {
            binding.tvWordBottom.setOutPut(outputWord)
            binding.ivCrossTop.visible()
            binding.ivArrowBottom.visible()
            binding.progressBottom.gone()
            speakerCode = targetLangCode
            binding.layoutContainerTop.visible()
            binding.layoutContainerBottom.gone()

        } else {
            binding.tvWordTop.setOutPut(outputWord)
            binding.ivCrossBottom.visible()
            binding.ivArrowTop.visible()
            binding.progressTop.gone()
            speakerCode = srcLangCode
            binding.layoutContainerBottom.visible()
            binding.layoutContainerTop.gone()

        }
        if (isSpeakerVisible(speakerCode))
            speakWord(speakerCode, outputWord)
    }

    private fun handleErrorResponse(error: String) {
        isTranslationAvailable = false
        showToast(error, 0)
    }

    private fun speakWord(speakerCode: String, word: String) {
        val langLocal = viewModel.getLocale(speakerCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(word, TextToSpeech.QUEUE_FLUSH, map)
    }

    private fun showResult() {
        var mInputWord = ""
        var mTranslatedWord = ""
        if (conversationClickType == ConversationClickType.LEFT) {
            mInputWord = binding.tvWordTop.text.toString().trim()
            mTranslatedWord = binding.tvWordBottom.text.toString().trim()

        } else {
            mInputWord = binding.tvWordBottom.text.toString().trim()
            mTranslatedWord = binding.tvWordTop.text.toString().trim()

        }
        if (mInputWord.isEmpty()) {
            return
        }

        val mPrimaryId = mInputWord + srcLangName + targetLangName + mTranslatedWord
        var history = TranslationHistory().apply {
            inputWord = mInputWord
            translatedWord = mTranslatedWord
            srcLang = srcLangName
            targetLang = targetLangName
            srcCode = srcLangCode
            trCode = targetLangCode
            primaryId = mPrimaryId
            isFavorite = false
        }

        val intent = Intent(this@ConversationActivity, InputActivity::class.java)
        intent.putExtra("history_model", history)
        startActivity(intent)
        finish()

    }

    var mXOld = 0
    var mYOld = 0

    @SuppressLint("ClickableViewAccessibility")
    private fun setBottomScrollClickListener() {
        binding.conversationScrollBottom.setOnTouchListener(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                mXOld = x
                mYOld = y
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (x == mXOld || y == mYOld) {  // detecting it's not a horizontal/vertical scrolling in ScrollView

                    mTts?.let { tts ->
                        if (tts.isSpeaking) {
                            tts.stop()
                        } else {
                            val word = binding.tvWordBottom.text.toString().trim()
                            speakWord(targetLangCode, word)
                        }
                    }
                    return@OnTouchListener false
                }
            }
            false
        })
    }

    var mXOldTop = 0
    var mYOldTop = 0

    @SuppressLint("ClickableViewAccessibility")
    private fun setTopScrollClickListener() {
        binding.conversationScrollTop.setOnTouchListener(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                mXOldTop = x
                mYOldTop = y
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (x == mXOldTop || y == mYOldTop) {  // detecting it's not a horizontal/vertical scrolling in ScrollView

                    mTts?.let { tts ->
                        if (tts.isSpeaking) {
                            tts.stop()
                        } else {
                            val word = binding.tvWordTop.text.toString().trim()
                            speakWord(srcLangCode, word)
                        }
                    }
                    return@OnTouchListener false
                }
            }
            false
        })
    }

    private fun setEditTextListeners() {
        binding.etInputTop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim().isNotEmpty()) {
                    binding.ivCrossTop.visible()
                    binding.layoutAreaActionsTop.visible()
                } else {
                    binding.ivCrossTop.gone()
                    binding.layoutAreaActionsTop.gone()
                }

            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.etInputBottom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim().isNotEmpty()) {
                    binding.ivCrossBottom.visible()
                    binding.layoutAreaActionsBottom.visible()
                } else {
                    binding.ivCrossBottom.gone()
                    binding.layoutAreaActionsBottom.gone()
                }

            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.etInputTop.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                binding.tvWordTop.reset()
                binding.ivArrowBottom.gone()
                binding.ivArrowTop.gone()
                binding.etInputTop.gone()
                val inputText = binding.etInputTop.text.toString().trim()
                binding.etInputTop.setText("")
                conversationClickType = ConversationClickType.LEFT
                setInputFieldText(inputText)

            }
            false
        }
        binding.etInputBottom.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                binding.tvWordBottom.reset()
                binding.ivArrowBottom.gone()
                binding.ivArrowTop.gone()
                binding.etInputBottom.gone()
                val inputText = binding.etInputBottom.text.toString().trim()
                binding.etInputBottom.setText("")
                conversationClickType = ConversationClickType.RIGHT
                setInputFieldText(inputText)

            }
            false
        }
    }

    private fun setEditTextStateActive(etInput: AppCompatEditText) {
        etInput.visible()
        etInput.isActivated = true
        etInput.isPressed = true
        etInput.isCursorVisible = true
        etInput.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            etInput.focusable = View.FOCUSABLE
        }

        etInput.setHorizontallyScrolling(false)
        etInput.maxLines = 9999
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

        etInput.setTextColor(resources.getColor(R.color.colorLangBoxHeader))
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun copyText(word: String) {
        showToast(getString(R.string.text_copied_successfully), 0)
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val sb = StringBuilder()
        sb.append(word)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("ocr_copy", sb.toString()))
    }

    override fun onPause() {
        super.onPause()
        mTts?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        iapConnector?.closeAllConnection()
        mTts?.let {
            it.stop()
            it.shutdown()
        }
    }
}