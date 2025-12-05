package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.clipboard

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.hm.admanagerx.isAppOpenAdShow
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityClipboardBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.SplashActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.clipboard.adapter.CustomAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import kotlinx.coroutines.runBlocking

class ClipboardActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityClipboardBinding
    private var srcLangCode: String? = null
    private var srcLangName: String? = null
    private var clipBoardData: String? = null
    private var translatedWord: String? = null
    private var isFromService = false
    private var targetLangName: String? = null
    private var targetLangCode: String? = null
    private var myDataBase: MyDataBase? = null

    private var mTts: TextToSpeech? = null
    private var speakerClick = SpeakerClick.NONE


    var langList: List<LanguageModel> = ArrayList()

    private var isClipboardOn = true
    private var translationHistory: TranslationHistory? = null


    private var mLastClickTime: Long = 0
    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        myDataBase = MyDataBase.getInstance(this)
        binding = ActivityClipboardBinding.inflate(layoutInflater)
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

        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#60000000")))
        getCopiedData()
        setViews()
        binding.tvTranslatedWordClip.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        isAppOpenAdShow(false)
//        TinyDB.getInstance(this).putBoolean("isOutside", true)
    }

    private fun setViews() {
        binding.tvCopiedWord.setText(clipBoardData)
        binding.tvCopiedWord.isLongClickable = false
        binding.tvCopiedWord.setTextIsSelectable(false)
        binding.tvCopiedWord.customSelectionActionModeCallback = object : ActionMode.Callback,
            android.view.ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(
                mode: android.view.ActionMode?,
                item: MenuItem?
            ): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
        }
        langList = fetchAllLanguages()
        setSourceViews()
        setTargetViews()

        if (!isFromService) {
            binding.layoutClipController.visibility = View.GONE
        }

        val handler = android.os.Handler(Looper.myLooper()!!)
        val runnable = Runnable {
            callTranslation()
        }

        binding.tvCopiedWord.doAfterTextChanged {
            val word = binding.tvCopiedWord.text.toString()
            if (word.length > 1) {
                if (binding.tvCopiedWord.isPerformingCompletion) {
                    handler.removeCallbacks(runnable)
                    return@doAfterTextChanged
                } else {
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 200)
                }
            } else {
                handler.removeCallbacks(runnable)
            }
        }

        setClickListeners()


    }

    private fun setSourceViews() {
        try {
            val popupSource =
                Spinner::class.java.getDeclaredField(resources.getString(R.string.mpopUp))
            popupSource.isAccessible = true
            val popupWindow = popupSource[binding.spinnerSourceLang] as ListPopupWindow
            val spinerHeight: Int = getScreenHeight() / 2
            assert(popupWindow != null)
            popupWindow.height = spinerHeight
        } catch (e: NoClassDefFoundError) {
            // silently fail...
        } catch (e: ClassCastException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }

        srcLangCode = getString("clipboard_lang_source_code")
        if (srcLangCode == null || srcLangCode == "") {
            srcLangCode = "en"
            putString("clipboard_lang_source_code", srcLangCode!!)
        }

        val adapter = CustomAdapter(this, langList)

        val countries: MutableList<String> = ArrayList()
        for (languageModel in langList) {
            countries.add(languageModel.languageCode)
        }
        val selectedId = countries.indexOf(srcLangCode)
        binding.spinnerSourceLang.adapter = adapter

        binding.spinnerSourceLang.setSelection(selectedId)
        binding.spinnerSourceLang.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                srcLangCode = langList[position].languageCode
                srcLangName = langList[position].languageName
                binding.tvClipSourceLangName.text = srcLangName

                putString("clipboard_lang_source_code", srcLangCode!!)
                checkTTSSpeaking()

                callTranslation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun setTargetViews() {
        try {
            val popup = Spinner::class.java.getDeclaredField(resources.getString(R.string.mpopUp))
            popup.isAccessible = true
            val popupWindow = popup[binding.spinnerLang] as ListPopupWindow
            val spinerHeight: Int = getScreenHeight() / 2
            assert(popupWindow != null)
            popupWindow.height = spinerHeight
        } catch (e: NoClassDefFoundError) {
            // silently fail...
        } catch (e: ClassCastException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }

        targetLangCode = getString("clipboard_lang_code")
        if (targetLangCode == null || targetLangCode == "") {
            targetLangCode = "fr"
            putString("clipboard_lang_code", targetLangCode!!)
        }

        val adapter = CustomAdapter(this, langList)

        val countries: MutableList<String> = ArrayList()
        for (languageModel in langList) {
            countries.add(languageModel.languageCode)
        }
        val selectedId = countries.indexOf(targetLangCode)
        binding.spinnerLang.adapter = adapter

        binding.spinnerLang.setSelection(selectedId)
        binding.spinnerLang.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                targetLangCode = langList[position].languageCode
                targetLangName = langList[position].languageName
                binding.tvClipTargetLangName.text = targetLangName

                putString("clipboard_lang_code", targetLangCode!!)
                checkTTSSpeaking()

                callTranslation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

    }

    private fun setClickListeners() {
        binding.ivExitClipboard.setOnClickListener {
            finish()
        }
        binding.switchViewQuick.setOnClickListener {

            if (isClipboardOn) {
                isClipboardOn = false
                binding.switchClipToggle.isChecked = true
                stopClipboardService()
            } else {
                isClipboardOn = true
                binding.switchClipToggle.isChecked = false
                launchClipboardService()
            }
            putBoolean(Constants.CLIP_BOARD_SERVICE, isClipboardOn)
            AppUtils.onStopClipBoard?.invoke(isClipboardOn)
        }
//        tv_more.setOnClickListener {
//            presentData()
//
//        }

        binding.ivSpeakerClipTar.setOnClickListener {
            if (speakerClick == SpeakerClick.TRANSLATED) {
                if (checkSpeaker()) {
                    stopSpeaker()
                }
                resetAllSpeakerImages()
            } else {
                if (checkSpeaker()) {
                    stopSpeaker()
                }
                resetAllSpeakerImages()

                val outPutWord = binding.tvTranslatedWordClip.text.toString().trim()
                speakerClick = SpeakerClick.TRANSLATED
                speakWord(outPutWord, targetLangCode!!)
            }


        }
        binding.ivSpeakerClipSrc.setOnClickListener {
            if (speakerClick == SpeakerClick.SAMPLE_ONE) {
                if (checkSpeaker()) {
                    stopSpeaker()
                }
                resetAllSpeakerImages()
            } else {
                if (checkSpeaker()) {
                    stopSpeaker()
                }
                resetAllSpeakerImages()
                val mInputWord = binding.tvCopiedWord.text.toString().trim()
                speakerClick = SpeakerClick.SAMPLE_ONE
                speakWord(mInputWord, srcLangCode!!)
            }


        }
        binding.tvClipSourceLangName.setOnClickListener {
            binding.spinnerSourceLang.performClick()
        }

        binding.tvClipTargetLangName.setOnClickListener {
            binding.spinnerLang.performClick()
        }

        binding.clearTV.setOnClickListener {
            binding.tvCopiedWord.setText("")
            binding.tvTranslatedWordClip.text = ""
        }

        binding.newTranslationTV.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }

    }

    private fun presentData() {
        if (isDoubleClick()) {
            translationHistory?.apply {
                launchDetailScreen(this, "general")
                finish()
            }
        }

    }

    private fun launchDetailScreen(translationHistory: TranslationHistory, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        intent.putExtra("from", "clipboard")
        intent.putExtra("history_model", translationHistory)
        startActivity(intent)
    }

    private fun resetAllSpeakerImages() {
        speakerClick = SpeakerClick.NONE
        binding.ivSpeakerClipSrc.setImageResource(R.drawable.ic_speaker_dictionary)
        binding.ivSpeakerClipTar.setImageResource(R.drawable.ic_speaker_dictionary)

    }

    private fun speakWord(word: String, langCode: String) {

        val langLocal = getLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(word, TextToSpeech.QUEUE_FLUSH, map)
//        textToSpeechUtil?.speakTranslation(word, langCode)
    }


    private fun getTranslationModel(): TranslationHistory {
        val mInputWord = binding.tvCopiedWord.text.toString().trim()
        val outPutWord = binding.tvTranslatedWordClip.text.toString().trim()
        val mPrimaryId = mInputWord + srcLangName + targetLangName + outPutWord
        return TranslationHistory().apply {
            inputWord = mInputWord
            translatedWord = outPutWord
            srcLang = srcLangName
            targetLang = targetLangName
            srcCode = srcLangCode
            trCode = targetLangCode
            primaryId = mPrimaryId
            isFavorite = false
        }
    }

    private fun getCopiedData() {
        val intent = intent
        val from = intent.getStringExtra("from")
        if (from != null && from == "service") {
            clipBoardData = intent.getStringExtra("clip_board_data")
            isFromService = true
        } else {
            try {
                val charSequenceExtra =
                    intent.getCharSequenceExtra("android.intent.extra.PROCESS_TEXT")
                if (charSequenceExtra != null) {
                    clipBoardData = charSequenceExtra.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    private fun callTranslation() {
        if (binding.tvCopiedWord.text.isNullOrEmpty())
            return

        binding.tvTranslatedWordClip.visibility = View.GONE
        binding.progressClip.visibility = View.VISIBLE

        val translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
            override fun onReceiveResult(result: String?) {
                runOnUiThread {
                    setTransaltionResult(result)
                    binding.progressClip.visibility = View.GONE
                }
            }

            override fun onFailedResult() {
                runOnUiThread {
                    binding.tvTranslatedWordClip.text = getString(R.string.something_went_wrong)
                    binding.tvTranslatedWordClip.visibility = View.VISIBLE
                    binding.progressClip.visibility = View.GONE
                }
            }
        }, binding.tvCopiedWord.text.toString().trim(), srcLangCode, targetLangCode)
        translationUtils.execute()
    }

    private fun setTransaltionResult(result: String?) {
        result?.let {
            binding.tvTranslatedWordClip.text = it
            binding.tvTranslatedWordClip.visibility = View.VISIBLE
            translatedWord = it
            if (isSpeakerVisible(targetLangCode!!)) {
                binding.ivSpeakerClipTar.visibility = View.VISIBLE
            } else {
                binding.ivSpeakerClipTar.visibility = View.GONE
            }
//            tv_more.visibility = View.VISIBLE
            translationHistory = getTranslationModel()
            translationHistory?.let {
                insertHistory(it)

            }

        }

    }

    private fun insertHistory(
        translationHistory: TranslationHistory,
    ) {

        runBlocking {
            insertHistoryinDb(translationHistory)
        }

    }

    suspend fun insertHistoryinDb(
        translationHistory: TranslationHistory,
    ) {
        translationHistory.apply {
            id = getDatabaseSize() + 1
            setDatabase(this)
        }
//        setDatabase(translationHistory)

    }

    private suspend fun setDatabase(translationHistory: TranslationHistory) {
        myDataBase!!.translationDao().insert(translationHistory)

    }

    private fun getDatabaseSize(): Int {
        return myDataBase!!.translationDao().all.size
    }


    override fun onResume() {
        super.onResume()
        mTts = TextToSpeech(this, this, "com.google.android.tts")


    }

    private fun checkSpeaker() = mTts?.isSpeaking ?: run {
        false
    }

    private fun stopSpeaker() {
        mTts?.stop()
    }


    private fun checkTTSSpeaking() {
        resetAllSpeakerImages()
        mTts?.let {
            if (it.isSpeaking) {
                it.stop()

            }
        }

    }


    override fun onPause() {

        checkTTSSpeaking()
        finish()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()

        mTts?.let {
            if (it.isSpeaking)
                it.shutdown()
        }
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            mTts?.setSpeechRate(0.7f)
            mTts?.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    when (speakerClick) {
                        SpeakerClick.SAMPLE_ONE -> {
                            binding.ivSpeakerClipSrc.startSpeaker()
                        }

                        SpeakerClick.TRANSLATED -> {
                            binding.ivSpeakerClipTar.startSpeaker()
                        }

                        else -> {
                        }
                    }

                }

                override fun onDone(utteranceId: String) {
                    when (speakerClick) {
                        SpeakerClick.SAMPLE_ONE -> {
                            binding.ivSpeakerClipSrc.stopSpeaker()
                        }

                        SpeakerClick.TRANSLATED -> {
                            binding.ivSpeakerClipTar.stopSpeaker()
                        }

                        else -> {
                        }
                    }


                }

                override fun onError(utteranceId: String) {


                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)

                }

            })

        }
    }

    private fun ImageView.stopSpeaker() {
        runOnUiThread {
            this.setImageResource(R.drawable.ic_speaker_dictionary)
            speakerClick = SpeakerClick.NONE
        }

    }

    private fun ImageView.startSpeaker() {
        runOnUiThread {
            this.setImageResource(R.drawable.ic_stop_gray)

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}