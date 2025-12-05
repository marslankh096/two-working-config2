package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivitySentencesBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCatSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhrasesSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase.PhraseViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence.fragment.SentenceAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence.fragment.SentencesFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getPhraseMicCode
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.hideKeyboard
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.invisible
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isAppInstalled
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.setDefaultMaxLines
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.toLowerCaseExceptFirst
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.HashMap
import java.util.Locale

class SentencesActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivitySentencesBinding
    private lateinit var viewModel: PhraseViewModel
    private lateinit var searchAdapter: SentenceAdapter
    private lateinit var currentFragment: SentencesFragment
    private var id: Int = -1
    private var title: String = ""
    private var category: String = ""
    private var speakPosition = -1
    private var mTts: TextToSpeech? = null
    private var pagerAdapter: PhraseFragmentAdapter? = null
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var listPareses: ArrayList<PhraseCatSentences>
    private lateinit var listSentences: ArrayList<PhrasesSentences>
    private lateinit var listSearch: ArrayList<PhrasesSentences>
    private var changeNoticed = false

    private val startForSpeech =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent: ActivityResult ->
            if (resultIntent.resultCode == RESULT_OK) {
                val recognizedResult =
                    resultIntent.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (recognizedResult != null) {
                    val inputWord = recognizedResult[0]
                    binding.searchTV.setText(inputWord)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySentencesBinding.inflate(layoutInflater)
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
        intent.apply {
            id = getIntExtra("id", -1)
            title = getStringExtra("title") ?: ""
            category = getStringExtra("category") ?: ""
            Log.e("none", "onCreate: $id")
        }
        init()
        setListeners()
    }

    private fun init() {
        viewModel = getViewModel()
        listSentences = ArrayList()
        listSearch = ArrayList()
        getAllPhrases()

        binding.titleTV.text = title
        pagerAdapter = PhraseFragmentAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.isSaveFromParentEnabled = false
        binding.viewPager.adapter = pagerAdapter
        tabLayoutMediator =
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = pagerAdapter!!.getPageTitle(position)
                tab.view.setDefaultMaxLines(1)
            }

        tabLayoutMediator.attach()

        initTTS()
        setSearching()
        observePhrases()
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })
        binding.backIV.setOnClickListener {
            backPressed()
        }
        binding.searchIV.setOnClickListener {
            if (isDoubleClick()) {
                showSearchViews()
                binding.searchTV.requestFocus()
                binding.searchTV.performClick()
//                binding.searchTV.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchTV, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        binding.micIV.setOnClickListener {
            if (isDoubleClick()) {
                hideKeyboard()
                checkAudioPermission()
            }
        }

    }

    private fun getAllPhrases() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getPhrases(id)
        }
    }

    private fun observePhrases() {
        viewModel.getPhrases().observe(this) {
            listSentences.clear()
            Log.e("none", "getAllPhrases: Observer called")
            listPareses = it as ArrayList<PhraseCatSentences>
            listPareses.forEach { sentences ->
                listSentences.addAll(sentences.list)
            }
            CoroutineScope(Dispatchers.Main).launch {
                updateRecycler(listPareses)
                if (category.isNotEmpty()) {
                    delay(300)
                    for (i in 0 until binding.tabLayout.tabCount) {
                        val tab = binding.tabLayout.getTabAt(i)
                        if (tab != null && tab.text.toString() == category) {
                            tab.select()
                            category = ""
                            break
                        }
                    }
                }

            }

        }

    }

    private fun setSearching() {
        searchAdapter = SentenceAdapter(speakCallBack = { sentence, position ->
            val reformedSentence = sentence.translation.replace("_", "")
            onClickSpeakerTranslation(reformedSentence, position)
        }, favouriteCallBack = { sentence, position ->
            lifecycleScope.launch(Dispatchers.IO) {
                changeNoticed = true
                if (sentence.favourite) {
                    viewModel.deleteFavPhrase(getFavTransId(sentence.id))
                    listSearch[position].favourite = false
                } else {
                    viewModel.addData(
                        FavouritePhrases(
                            System.currentTimeMillis(),
                            getFavTransId(sentence.id)
                        )
                    )
                    listSearch[position].favourite = true
                }

                withContext(Dispatchers.Main) {
                    searchAdapter.updateList(listSearch, position)
                }

            }
        })
        binding.sentenceRV.layoutManager = LinearLayoutManager(this)
        binding.sentenceRV.adapter = searchAdapter

        val handler = android.os.Handler(Looper.myLooper()!!)
        val runnable = Runnable {
            filterSearch(binding.searchTV.text.toString().trim())
        }

        binding.searchTV.doAfterTextChanged {
            if (binding.searchTV.isPerformingCompletion) {
                handler.removeCallbacks(runnable)
                return@doAfterTextChanged
            } else {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 200)
            }
        }

    }

    private fun updateRecycler(listPareses: ArrayList<PhraseCatSentences>) {
        pagerAdapter?.let { adapter ->
            if (adapter.itemCount > 0) {
                try {
                    for (i in listPareses.indices) {
                        currentFragment = adapter.getFragment(i) as SentencesFragment
                        currentFragment.updateRecycleData(listPareses[i].list)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (listPareses.isNotEmpty()) listPareses.forEach {
                    adapter.addFragment(
                        SentencesFragment(it.list, id),
                        it.cateName.toLowerCaseExceptFirst()
                    )
                }
            }


        }

    }

    private fun filterSearch(query: String) {
        listSearch.clear()
        if (query.isEmpty()) {
            listSearch.addAll(listSentences)
        } else {
            listSentences.let {

                for (item in it) {
                    if (item.phraseSentences.lowercase(Locale.getDefault())
                            .contains(query.lowercase(Locale.getDefault()))
                    ) {
                        listSearch.add(item)
                    }
                }
            }
        }

        if (listSearch.isEmpty())
            binding.noResultCL.visible()
        else
            binding.noResultCL.gone()
        searchAdapter.updateList(listSearch, null)
//        searchAdapter?.notifyDataSetChanged()
    }

    private fun showSearchViews() {
        binding.searchTV.setText("")
        binding.tabLayout.invisible()
        binding.viewPager.invisible()
        binding.titleTV.gone()
        binding.searchIV.gone()

        binding.micIV.visible()
        binding.searchTV.visible()
        binding.searchCL.visible()
    }

    private fun hideSearchViews() {
        binding.tabLayout.visible()
        binding.viewPager.visible()
        binding.titleTV.visible()
        binding.searchIV.visible()

        binding.micIV.gone()
        binding.searchTV.gone()
        binding.searchCL.gone()
        binding.noResultCL.gone()
    }

    private fun checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            startMic()
        } else {
            if (isAppInstalled("com.google.android.googlequicksearchbox", this)
                ||
                isAppInstalled("com.google.android.apps.searchlite", this)
            ) {
                startMic()
            } else {
                showToast(getString(R.string.message_app_install), 1)
            }
        }

    }

    private fun startMic() {
        getSourceLanguageCode().let { code ->
            getPhraseMicCode(code)?.let {
                speakIn(it)
            }
        }
    }

    private fun speakIn(code: String) {
        val micIntent = viewModel.getRecognizerIntent(code)
        try {
            startForSpeech.launch(micIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resources.getString(R.string.stt_error_device), 0)
        }
    }

    private fun initTTS() {
        mTts = TextToSpeech(this, this, "com.google.android.tts")
    }

    private fun onClickSpeakerTranslation(text: String, position: Int) {
        mTts?.let {
            if (it.isSpeaking && speakPosition == position) {
                it.stop()
            } else {
                speakPosition = position
                speakWord(text)
            }
        }
    }

    private fun speakWord(word: String) {
        val langLocal = viewModel.getLocale()
        val result = mTts?.setLanguage(langLocal)
        val isSpeakAvailable =
            !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
//        mTts?.language = langLocal
        if (isSpeakAvailable) {
            val map = HashMap<String, String>()
            map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
            mTts?.speak(word, TextToSpeech.QUEUE_FLUSH, map)
        } else {
            Toast.makeText(
                this,
                getString(R.string.speech_input_is_not_available_for_selected_language),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTts?.let {
            it.stop()
            it.shutdown()
        }
    }

    override fun onPause() {
        super.onPause()
        mTts?.stop()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            mTts?.let {
                it.setSpeechRate(0.9f)
                it.setPitch(1f)
                it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        startProcessing()
                    }

                    override fun onDone(utteranceId: String?) {
                        stopProcessing()
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        super.onStop(utteranceId, interrupted)
                        stopProcessing()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        stopProcessing()
                    }
                })
            }
        }
    }

    private fun startProcessing() {
        listSearch[speakPosition].isProcessing = true
        runOnUiThread {
            searchAdapter.notifyItemChanged(speakPosition)
        }
    }

    private fun stopProcessing() {
        listSearch[speakPosition].isSpeaking = false
        listSearch[speakPosition].isProcessing = false
        runOnUiThread {
            searchAdapter.notifyItemChanged(speakPosition)
        }
    }

    private fun getFavTransId(id: Int): String {
        return "${id}_${
            getSourceLanguageCode() + "_" + transitionLanguageCode()
        }"
    }

    private fun getSourceLanguageCode(): String {
        return viewModel.getLanguageData(Constants.KEY_PHRASE_INPUT_LANG_CODE)
    }

    private fun transitionLanguageCode(): String {
        return viewModel.getLanguageData(Constants.KEY_PHRASE_TRANSLATED_LANG_CODE)
    }

    private fun backPressed() {
        if (binding.searchTV.isVisible) {
            if (changeNoticed) {
                changeNoticed = false
                getAllPhrases()
            }
            hideKeyboard()
            hideSearchViews()
        } else {
            finish()
        }
    }

}