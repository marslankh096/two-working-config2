package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryDetail

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.gson.Gson
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponseItem
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityDictionaryDetailBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.DictionaryDetailHeaderBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.DictionaryDetailItemBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.copyWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class DictionaryDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDictionaryDetailBinding
    private val viewModel: DictionaryDetailViewModel by viewModel()
    private var word: WordResponse? = null
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDictionaryDetailBinding.inflate(layoutInflater)
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
        val data = intent.getStringExtra("detail")
        val isWOD = intent.getBooleanExtra("isWOD", false)
        if (data != null) {
            viewModel.getWordDetail(data)
        }
        isWOD?.let {
            if (isWOD) {
                binding.titleTV.text = getString(R.string.word_of_the_day)
            }
        }

        viewModel.wordDetail.observe(this) {
//            binding.detailTV.text = it.response
            setBookmarkView(it.bookmark)
            word = Gson().fromJson(it.response, WordResponse::class.java)
            word?.let { word ->
                parseWord(word)
            }
        }
    }

    private fun setListeners() {
        binding.copyIV.setOnClickListener {
            if (isDoubleClick()) {
                word?.let {
                    copyWordData(it)
                }
            }
        }

        binding.bookmarkIV.setOnClickListener {
            if (isDoubleClick()) {
                setBookmark()
            }
        }

        binding.shareIV.setOnClickListener {
            if (isDoubleClick()) {
                word?.let {
                    shareWordData(it)
                }
            }
        }

        binding.ivBack.setOnClickListener {
            if (isDoubleClick()) {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )

    }

    private fun parseWord(response: WordResponse) {
        parseHeader(response[0])

        for (item in response) {
            val meaningItem = item.meanings
            for (meaning in meaningItem) {
                val itemBinding = DictionaryDetailItemBinding.inflate(
                    layoutInflater,
                    binding.dictionaryDetailLL,
                    false
                )
                itemBinding.typeTV.text = "${meaning.partOfSpeech}:"
                if (item.phonetics.isEmpty()) {
                    itemBinding.phoneticTV.visibility = GONE
                    itemBinding.phoneticSpeakerIV.visibility = GONE
                } else {
                    for (phonetic in item.phonetics) {
                        if (!phonetic.text.isNullOrEmpty()) {
                            itemBinding.phoneticTV.visibility = VISIBLE
                            itemBinding.phoneticTV.text = phonetic.text
                            if (phonetic.audio.isNullOrEmpty()) {
                                itemBinding.phoneticSpeakerIV.visibility = GONE
                            } else {
                                itemBinding.phoneticSpeakerIV.visibility = VISIBLE
                                itemBinding.phoneticSpeakerIV.setOnClickListener {
                                    if (isDoubleClick())
                                        playAudio(phonetic.audio)
                                }
                            }
                            break
                        }
                    }
                }

                if (meaning.synonyms.isNotEmpty() || meaning.antonyms.isNotEmpty()) {
                    itemBinding.synonymAntonymCL.visibility = VISIBLE
                    if (meaning.synonyms.isNotEmpty()) {
                        itemBinding.synTitleTV.visibility = VISIBLE
                        itemBinding.synWordsTV.visibility = VISIBLE
                        itemBinding.synWordsTV.text =
                            Html.fromHtml(meaning.synonyms.joinToString("<span style=\"color: #8c8c8c;\"> / </span>"))
//                        body += "== " + meaning.synonyms.joinToString(", ") + "<br><br>"
                    } else {
                        itemBinding.synTitleTV.visibility = GONE
                        itemBinding.synWordsTV.visibility = GONE
                    }

                    if (meaning.antonyms.isNotEmpty()) {
                        itemBinding.antTitleTV.visibility = VISIBLE
                        itemBinding.antWordsTV.visibility = VISIBLE
                        itemBinding.antWordsTV.text =
                            Html.fromHtml(meaning.antonyms.joinToString("<span style=\"color: #8c8c8c;\"> / </span>"))
//                        body += "~~ " + meaning.antonyms.joinToString(", ")
                    } else {
                        itemBinding.antTitleTV.visibility = GONE
                        itemBinding.antWordsTV.visibility = GONE
                    }
                } else {
                    itemBinding.synonymAntonymCL.visibility = GONE
                }

                var body = ""
                var def = 1
                for (definition in meaning.definitions) {
                    body += "$def. ${definition.definition}"
                    def++
                    if (!definition.example.isNullOrEmpty()) {
//                        body += definition.example
                        body += "' <span style=\"color: #279b37;\"> ${definition.example}</span> '"
                    }
//                    if (itemBinding.synonymAntonymCL.visibility == VISIBLE)
                    body += "<br>"
                }

                itemBinding.detailTV.text = Html.fromHtml(body)
                binding.dictionaryDetailLL.addView(itemBinding.root)

            }
        }

    }

    private fun setBookmarkView(isBookmark: Boolean) {
        if (isBookmark) {
            binding.bookmarkIV.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_dictionary_detail_bookmark_selected
                )
            )
        } else {
            binding.bookmarkIV.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_dictionary_detail_bookmark_unselected
                )
            )
        }
    }

    private fun parseHeader(wordResponseItem: WordResponseItem) {
        val headerBinding = DictionaryDetailHeaderBinding.inflate(layoutInflater)
        headerBinding.wordTV.text = wordResponseItem.word

        for (phoneticItem in wordResponseItem.phonetics) {
            if (!phoneticItem.text.isNullOrEmpty()) {
                if (phoneticItem.audio.isNullOrEmpty()) {
                    headerBinding.headerSpeakerIV.visibility = GONE
                } else {
                    headerBinding.headerSpeakerIV.visibility = VISIBLE
                    headerBinding.headerSpeakerIV.setOnClickListener {
                        if (isDoubleClick())
                            playAudio(phoneticItem.audio)
                    }
                    break
                }
            }
        }

        binding.dictionaryDetailLL.addView(headerBinding.root)
    }

    private fun playAudio(audio: String) {
        if (Controller.isOnline(this)) {
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(audio)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                }
                mediaPlayer.setOnCompletionListener {
                    mediaPlayer.release()
                }
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    mediaPlayer.release()
                    false
                }
            } catch (e: Exception) {
                Log.e("DictionaryDetails", "playAudio: ${e.message}")
                mediaPlayer.release()
            }
        } else {
            showToast(getString(R.string.check_internet_connection), 0)
        }
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        super.onDestroy()
    }

    private fun setBookmark() {
        viewModel.wordDetail.value?.let { word ->
            word.bookmark = !word.bookmark
            setBookmarkView(word.bookmark)
            viewModel.setBookmark(word)
        }
    }
}