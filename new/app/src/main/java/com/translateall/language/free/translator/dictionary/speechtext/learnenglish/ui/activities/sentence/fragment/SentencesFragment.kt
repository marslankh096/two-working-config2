package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentSentencesBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhrasesSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase.PhraseViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.HashMap

class SentencesFragment() : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: FragmentSentencesBinding
    private lateinit var adapter: SentenceAdapter
    private lateinit var viewModel: PhraseViewModel

    private var mContext: Context? = null
    private var sentencesList: List<PhrasesSentences>? = null
    private var id: Int = 0
    private var speakPosition = -1
    private var mTts: TextToSpeech? = null

    constructor(list: List<PhrasesSentences>, id: Int) : this() {
        this.sentencesList = list
        this.id = id
    }

    fun updateRecycleData(list: List<PhrasesSentences>) {
        this.sentencesList = list
        adapter.updateList(list, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSentencesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    private fun init() {
        mContext?.let { _ ->
            viewModel = getViewModel()
            initTTS()
            setAdapter()
        }

    }

    private fun setAdapter() {
        mContext?.let { ctx ->
            adapter = SentenceAdapter(speakCallBack = { sentenceData, position ->
                val reformedSentence = sentenceData.translation.replace("_", "")
                onClickSpeakerTranslation(reformedSentence, position)
            }, favouriteCallBack = { sentenceData, position ->
                lifecycleScope.launch(Dispatchers.IO) {

                    if (sentenceData.favourite) {
                        viewModel.deleteFavPhrase(getFavTransId(sentenceData.id))
                        sentencesList!![position].favourite = false
                    } else {
                        viewModel.addData(
                            FavouritePhrases(
                                System.currentTimeMillis(),
                                getFavTransId(sentenceData.id)
                            )
                        )
                        sentencesList!![position].favourite = true
                    }

                    withContext(Dispatchers.Main) {
                        adapter.updateList(sentencesList!!, position)
                    }

                }
            })
            sentencesList?.let {
                updateData(it)
            }
            binding.sentenceRV.layoutManager = LinearLayoutManager(ctx)
            binding.sentenceRV.adapter = adapter
        }
    }

    private fun initTTS() {
        mContext?.let {
            mTts = TextToSpeech(it, this, "com.google.android.tts")
        }
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
            mContext?.let {
                Toast.makeText(
                    it,
                    getString(
                        R.string.speech_input_is_not_available_for_selected_language,
                        viewModel.getLanguageData(
                            Constants.KEY_PHRASE_TRANSLATED_LANG_NAME
                        )
                    ),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun updateData(it: List<PhrasesSentences>) {
        adapter.updateList(it, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTts?.let {
            it.stop()
            it.shutdown()
        }
        mContext = null
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
        sentencesList!![speakPosition].isProcessing = true
        (mContext as? Activity)?.runOnUiThread {
            adapter.notifyItemChanged(speakPosition)
        }
    }

    private fun stopProcessing() {
        sentencesList!![speakPosition].isSpeaking = false
        sentencesList!![speakPosition].isProcessing = false
        activity?.runOnUiThread {
            adapter.notifyItemChanged(speakPosition)
        }
    }

    private fun getFavTransId(id: Int): String {
        return "${id}_${
            getSourceLanguageCode() + "_" + transitionLanguageCode()
        }"
    }

    private fun getSourceLanguageCode(): String {
        return viewModel.getLanguageData(KEY_PHRASE_INPUT_LANG_CODE)
    }

    private fun transitionLanguageCode(): String {
        return viewModel.getLanguageData(KEY_PHRASE_TRANSLATED_LANG_CODE)
    }

}