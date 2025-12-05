package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.cameraTranslate

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityCameraTranslateBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.viewmodel.MainViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getLocale
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isSpeakerVisible
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchFullScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchLanguageScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchMain
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.runBlocking
import java.util.HashMap

class CameraTranslateActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityCameraTranslateBinding
    private var srcLangCode: String = ""
    private var srcLangName: String = ""
    private var targetLangCode: String = ""
    private var targetLangName: String = ""

    private var favorite = false
    private var targetLangChange = false
    private var translationUtils: TranslationUtils? = null
    private var translationModel: TranslationHistory? = null
    private var myDataBase: MyDataBase? = null
    private var mTts: TextToSpeech? = null

    private var srcLangPosition: Int = Constants.DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = Constants.DEFAULT_TAR_LANG_POSITION

    private var isSwitched = false
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTranslateBinding.inflate(layoutInflater)
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

        myDataBase = MyDataBase.getInstance(this)
        getLanguagesData()
        val hasData = intent.getStringExtra("has_data")
        mTts = TextToSpeech(this, this, "com.google.android.tts")
        activateEditText(binding.etInput)
        setEditTextChangeListener()
        setClickListeners()
        if (hasData!! == "yes") {
            val inputWord = intent.getStringExtra(Constants.INPUT_TYPE_KEY)!!
            binding.etInput.setText(inputWord)
            hideSoftKeyboard()
            checkAndTranslate()
        }
    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(Constants.SOURCE_LANG_CODE_OCR)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
        }
        srcLangName = viewModel.getLangData(Constants.SOURCE_LANG_NAME_OCR)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
        }

        srcLangPosition = viewModel.getLangPosition(Constants.SOURCE_LANG_POSITION_OCR)

        if (srcLangPosition == -1) {
            srcLangPosition = Constants.DEFAULT_SRC_LANG_POSITION
            viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
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

    private fun setEditTextChangeListener() {
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (char.toString().trim().isNotEmpty()) {
                    binding.ivClearInput.visible()
                    binding.layoutActionGo.visible()
                    binding.rippleMic.startRippleAnimation()
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

    private fun setClickListeners() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mTts?.isSpeaking!!)
                    mTts?.stop()
                    val intent = Intent(this@CameraTranslateActivity, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            })

        binding.ivBackInput.setOnClickListener {
            if (mTts?.isSpeaking!!)
                mTts?.stop()
            val intent = Intent(this@CameraTranslateActivity, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        binding.ivClearInput.setOnClickListener {
            resetData()
        }
        binding.cameraIV.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        binding.layoutActionGo.setOnClickListener {
            hideSoftKeyboard()
//            if (!getBoolean(Constants.IS_PREMIUM))
//                checkForAd()
//            else
            checkAndTranslate()
        }
        binding.layoutLanguagesLeft.setOnClickListener {
            if (isDoubleClick())
                openLanguageSheet(Constants.LANGUAGE_TYPE_SOURCE)
        }
        binding.layoutLanguagesRight.setOnClickListener {
            if (isDoubleClick()) {
                targetLangChange = true
                openLanguageSheet(Constants.LANGUAGE_TYPE_TARGET)
            }
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

    private fun openLanguageSheet(type: String) {
        launchLanguageScreen(type)
    }

    private fun resetData() {
        if (mTts?.isSpeaking!!)
            mTts?.stop()
//        binding.cameraIV.gone()
        binding.layoutOutputAreaMain.gone()
        binding.spaceView.gone()
        favorite = false
        binding.ivStarFavoriteDetail.setImageResource(R.drawable.ic_star_detail_unfill)
        translationModel = null
        binding.etInput.setText("")
        binding.ivClearInput.gone()
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun checkAndTranslate() {
        val wordText = binding.etInput.text.toString().trim()
        if (wordText.isNotEmpty()) {
//            binding.cameraIV.gone()
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
//        binding.cameraIV.visible()
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
        viewModel.setLangData(Constants.SOURCE_LANG_NAME, srcLangName)
        viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION, srcLangPosition)

        viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
        viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
        viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
        setLanguagesNames()
        AppUtils.onSwitchLanguages?.invoke()
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

    override fun onPause() {
        super.onPause()
        mTts?.let {
            if (it.isSpeaking)
            it.stop()
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
                    if (binding.etInput.text.toString().trim().isNotEmpty() && targetLangChange) {
                        targetLangChange = false
                        searchInputWord(binding.etInput.text.toString().trim())
                    }
                }

            } else if (targetLangChange) {
                targetLangChange = false
            }
        }
    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {
        langModel?.let { model ->
            if (type == Constants.LANGUAGE_TYPE_SOURCE) {
                srcLangName = model.languageName
                srcLangCode = model.languageCode
                srcLangPosition = position

                viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
                viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
                viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)

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

    override fun onDestroy() {
        super.onDestroy()
        mTts?.let {
            it.stop()
            it.shutdown()
        }
        translationUtils?.StopBackground()
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

}