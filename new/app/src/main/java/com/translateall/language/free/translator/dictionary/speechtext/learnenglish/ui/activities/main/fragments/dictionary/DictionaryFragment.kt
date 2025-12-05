package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.dictionary

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentDictionaryBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryBookmark.DictionaryBookmarkActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryDetail.DictionaryDetailActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.getWoD
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.copyWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getBottomDialogs
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getLocale
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class DictionaryFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding get() = _binding!!
    private var mContext: Context? = null
    private lateinit var adapter: RecentWordAdapter
    private val viewModel: DictionaryViewModel by viewModel()
    private var mTts: TextToSpeech? = null
    private var isSelectionEnabled = false
    private var isAllSelection = false
    private var deleteDialog: Dialog? = null
    private var trackRespond = false

    private val micResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            mContext?.let {
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val recognizedResult =
                        intent?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (recognizedResult != null) {
                        val inputWord = recognizedResult[0]
                        if (inputWord.toString().isNotEmpty()) {
                            searchDictionary(word = inputWord.toString().trim().lowercase())
                        }
                    }
                }
            }

        }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentDictionaryBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
    }

    private fun init() {
        mContext?.let { ctx ->

            mTts = TextToSpeech(ctx, this, "com.google.android.tts")
            viewModel.wordLiveData.observe(viewLifecycleOwner) {
                binding.searchTV.isEnabled = true
                binding.loadingCL.gone()
                if (it != null) {
                    binding.searchTV.isEnabled = true
                    binding.searchTV.setText("")
                    launchDetailScreen(it[0].word)
                } else {
                    binding.searchTV.isEnabled = true
                }
            }
            setRecentAdapter()
            viewModel.getRecentWordsList()
            viewModel.recentWordList.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it.isNotEmpty()) {
                        binding.noRecentGroup.visibility = GONE
                        binding.recentGroup.visibility = VISIBLE
                        adapter.updateList(it)
                    } else {
                        binding.noRecentGroup.visibility = VISIBLE
                        binding.recentGroup.visibility = GONE
                    }
                } else {
                    binding.noRecentGroup.visibility = VISIBLE
                    binding.recentGroup.visibility = GONE
                }

            }
            setWordOfDay()
            setWordSuggestion()
            setDeleteDialog()
        }
    }

    private fun setListeners() {
        binding.searchTV.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (isDoubleClick()) {
                    if (v.text.toString().isEmpty()) {
                        return@setOnEditorActionListener false
                    }
                    searchDictionary(word = v.text.toString().lowercase())
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.bookmarkIV.setOnClickListener {
            mContext?.let {
                if (isDoubleClick()) {
                    trackRespond = true
                    startActivity(Intent(it, DictionaryBookmarkActivity::class.java))
                }
            }
        }

        binding.selectionIV.setOnClickListener {
            if (isDoubleClick()) {
                isSelectionEnabled = true
                binding.selectionGroup.gone()
                binding.bookmarkIV.gone()
                binding.selectAllIV.visible()
                binding.deleteIV.visible()
                binding.titleTV.text = getString(R.string.label_selected, 0)
                adapter?.let {
                    it.changeSelectionState(true)
                }
            }
        }

        binding.selectAllIV.setOnClickListener {
            if (isDoubleClick()) {
                if (isAllSelection) {
                    isAllSelection = false
                    adapter.unselectAll()
                } else {
                    adapter.setCheckedAll(true)
                }
            }
        }

        binding.deleteIV.setOnClickListener {
            if (isDoubleClick()) {
                deleteDialog?.show()
            }
        }

        binding.dictionaryMicIV.setOnClickListener {
            if (isDoubleClick()) {
                hideSoftKeyboard()
                speakIn()
            }
        }

    }

    private fun setRecentAdapter() {
        adapter = RecentWordAdapter(object : RecentWordAdapter.RecentWordCallbacks {
            override fun onWordCalled(word: String, position: Int) {
                if (adapter.isSelection()) {
                    adapter.setChecked(position)
                } else {
                    launchDetailScreen(word)
                }
            }

            override fun onCopyCalled(item: RecentWord) {
                mContext?.let {
                    (it as Activity).copyWordData(
                        Gson().fromJson(
                            item.response,
                            WordResponse::class.java
                        )
                    )
                }
            }

            override fun onBookmarkCalled(item: RecentWord) {
                viewModel.setBookmarkStatus(item)
            }

            override fun onShareCalled(item: RecentWord) {
                mContext?.let {
                    (it as Activity).shareWordData(
                        Gson().fromJson(
                            item.response,
                            WordResponse::class.java
                        )
                    )
                }
            }

            override fun onSelectionChange(selection: Boolean, count: Int) {
                mContext?.let {
                    if (isSelectionEnabled)
                        binding.titleTV.text = getString(R.string.label_selected, count)
                    if (count == viewModel.recentWordList.value?.size) {
                        binding.selectAllIV.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_selection_check
                            )
                        )
                    } else {
                        binding.selectAllIV.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_selection_uncheck
                            )
                        )
                    }
                }
            }

            override fun onSelectAll(isAllSelected: Boolean, count: Int) {
                mContext?.let {
                    isAllSelection = isAllSelected
                    if (isAllSelected) {
                        binding.titleTV.text = getString(R.string.label_selected, count)
                        binding.selectAllIV.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_selection_check
                            )
                        )
                    } else {
                        binding.selectAllIV.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_selection_uncheck
                            )
                        )
                    }
                }
            }

            override fun onLongClick(position: Int) {
                adapter.let {
                    binding.selectionIV.performClick()
                    if (!it.isSelection()) {
                        it.setSelection(true)
                    }
                    it.setChecked(position)
                }
            }

        })

        mContext?.let {
            binding.recentRV.layoutManager = LinearLayoutManager(it)
            binding.recentRV.adapter = adapter
        }
    }

    private fun searchDictionary(word: String) {
        mContext?.let {
            if (Controller.isOnline(it)) {
                binding.loadingCL.visible()
                binding.searchTV.clearFocus()
                binding.searchTV.isEnabled = false
                hideSoftKeyboard()
                viewModel.searchWord(it, word)
            } else {
                it.showToast(getString(R.string.check_internet_connection), 0)
            }
        }
    }

    private fun setWordOfDay() {
        val wod = getWoD()

        binding.wodWordTV.text = wod.word
        binding.wodMeaningTV.text = wod.definition
        binding.wodSpeakerIV.setOnClickListener {
            if (isDoubleClick())
                speakWord(wod.word)
        }
        binding.wodCL.setOnClickListener {
            if (isDoubleClick()) {
                searchDictionary(word = wod.word.toLowerCase())
            }
        }
    }

    private fun setWordSuggestion() {
        mContext?.let { ctx ->

            viewModel.suggestionLiveData.observe(viewLifecycleOwner) {
                binding.searchTV.setAdapter(
                    ArrayAdapter(ctx, android.R.layout.simple_list_item_1, it)
                )
                binding.searchTV.showDropDown()
            }

            val handler = Handler(Looper.myLooper()!!)
            val runnable = Runnable {
                if (isAdded)
                    viewModel.getWordSuggestions(binding.searchTV.text.toString())
            }

            binding.searchTV.doAfterTextChanged {
                val word = binding.searchTV.text.toString()
                if (word.length > 1) {
                    if (binding.searchTV.isPerformingCompletion) {
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

            binding.searchTV.setOnItemClickListener { _, _, _, _ ->
                mContext?.let {
                    if (isDoubleClick()) {
                        if (Controller.isOnline(it)) {
                            binding.searchTV.clearFocus()
                            binding.loadingCL.visible()
                            binding.searchTV.isEnabled = false
                            hideSoftKeyboard()
                            viewModel.searchWord(
                                it,
                                binding.searchTV.text.toString().lowercase()
                            )
                        } else {
                            it.showToast(getString(R.string.check_internet_connection), 0)
                        }
                    }
                }
            }
        }
    }

    private fun speakWord(word: String) {
        mTts?.let {
            if (it.isSpeaking)
                it.stop()
            else {
                speakTranslation(word, "en")
            }
        }
    }

    private fun hideSoftKeyboard() {
        mContext?.let {
            val imm = it.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            mTts?.setSpeechRate(0.7f)
            mTts?.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}

                override fun onDone(utteranceId: String) {}

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    super.onStop(utteranceId, interrupted)
                }

                override fun onError(utteranceId: String) {}

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                }
            })
        }
    }

    private fun checkTTSSpeaking() {
        mTts?.let {
            if (it.isSpeaking)
                it.stop()
        }
    }

    private fun speakTranslation(speakingWord: String, langCode: String) {
        val langLocal = getLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }

    private fun stopEngine() {
        mTts?.let {
            if (it.isSpeaking)
                it.shutdown()
        }
    }

    private fun pauseEngine() {
        checkTTSSpeaking()
    }

    override fun onPause() {
        //bannerAd?.pause()
        super.onPause()
        pauseEngine()
    }

    override fun onStop() {
        super.onStop()
        stopEngine()
    }

    private fun speakIn() {
        val micIntent = getRecognizerIntent()
        try {
            micResult.launch(micIntent)
//            startActivityForResult(micIntent, 2211)
        } catch (e: Exception) {
            e.printStackTrace()
            mContext?.showToast(resources.getString(R.string.stt_error_device), 0)
        }
    }

    private fun getRecognizerIntent(): Intent {
        val code = "en-US"
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, code)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, code)
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, code)
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, code)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, code)
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, code)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        return intent
    }

    private fun setDeleteDialog() {
        mContext?.let {
            deleteDialog = (it as AppCompatActivity).getBottomDialogs(
                R.layout.dlg_clear_history,
                getString(R.string.delete_permanently),
                getString(R.string.are_you_sure_you_want_to_delete_selected_words),
                onCancel = {
                    deleteDialog?.dismiss()
                },
                onDelete = {
                    viewModel.deleteRecentList(adapter.allSelectedItems)
                    adapter.clearSelection()
                    resetSelectedView()
                    deleteDialog?.dismiss()
//                if (isSelectionEnabled)
//                    binding.ivBack.performClick()
                })
        }
    }

    private fun launchDetailScreen(word: String) {
        mContext?.let {
            trackRespond = true
            startActivity(
                Intent(it, DictionaryDetailActivity::class.java).putExtra("detail", word)
                    .putExtra(
                        "isWOD",
                        binding.wodWordTV.text.toString().toString().toLowerCase() == word
                    )
            )
        }
    }

    private fun hideLoadingDialog() {
        mContext?.let { ctx ->
            (ctx as MainActivity).hideLoadingDialog()
        }
    }

    private fun showLoadingDialog() {
        mContext?.let { ctx ->
            (ctx as MainActivity).showLoadingDialog()
        }
    }

    fun resetSelectedView() {
        if (isSelectionEnabled) {
            isSelectionEnabled = false
            isAllSelection = false
            binding.selectionGroup.visible()
            binding.bookmarkIV.visible()
            binding.selectAllIV.gone()
            binding.deleteIV.gone()
            binding.titleTV.text = getString(R.string.dictionary)
            adapter?.let {
                it.unselectAll()
                it.changeSelectionState(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mContext = null
    }

}