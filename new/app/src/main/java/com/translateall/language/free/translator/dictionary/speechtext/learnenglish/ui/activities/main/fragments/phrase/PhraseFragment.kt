package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentPhraseBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCategory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.generic.LanguageSelectionActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence.SentencesActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_SOURCE_PHRASE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_SOURCE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_TARGET
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class PhraseFragment : Fragment() {
    private var _binding: FragmentPhraseBinding? = null
    private val binding: FragmentPhraseBinding get() = _binding!!
    private var mContext: Context? = null
    private lateinit var viewModel: PhraseViewModel
    private var adapter: PhraseMainAdapter? = null
    private val categoryList: ArrayList<PhraseCategory> = ArrayList()
    private var swapClicked = false
    private var isSwitched = false
    private var inputLanguageName: String = ""
    private var inputLanguageCode: String = ""
    private var inputLanguagePosition: Int = -1
    private var translatedLanguageName: String = ""
    private var translatedLanguageCode: String = ""
    private var translatedLanguagePosition: Int = -1

    private val startForLangResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.let {
                    val langModel = it.getParcelableExtra("language_model") as LanguageModel?
                    val langType = it.getStringExtra("language_type")
                    val langPosition = it.getIntExtra("language_position", -1)
                    if (langType == LANGUAGE_TYPE_SOURCE) {
                        langModel?.let { model ->
                            viewModel.setLanguageData(
                                KEY_PHRASE_INPUT_LANG_CODE,
                                model.languageCode
                            )
                            viewModel.setLanguageData(
                                KEY_PHRASE_INPUT_LANG_NAME,
                                model.languageName
                            )
                            if (langPosition == -1) {
                                viewModel.checkSaveDefaultSourcePosition()
                            } else {
                                viewModel.setLangPosition(
                                    KEY_PHRASE_INPUT_LANG_POSITION,
                                    langPosition
                                )
                            }
                            getCategoryData()
                        }
                    } else {
                        langModel?.let { model ->
                            viewModel.setLanguageData(
                                KEY_PHRASE_TRANSLATED_LANG_CODE,
                                model.languageCode
                            )
                            viewModel.setLanguageData(
                                KEY_PHRASE_TRANSLATED_LANG_NAME,
                                model.languageName
                            )
                            if (langPosition == -1) {
                                viewModel.checkSaveDefaultTargetPosition()
                            } else {
                                viewModel.setLangPosition(
                                    KEY_PHRASE_TRANSLATED_LANG_POSITION,
                                    langPosition
                                )
                            }
                        }
                    }
                    setLanguagesData()
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
            _binding = FragmentPhraseBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
    }

    private fun init() {
        mContext?.let {
            adapter = PhraseMainAdapter(categoryList) { id, name, category ->
                launchSentencesActivity(id, name, category)
            }
            binding.categoryRV.layoutManager = LinearLayoutManager(it)
            binding.categoryRV.adapter = adapter
            viewModel = getViewModel()
            getCategoryData()
            setLanguagesData()
        }
    }

    private fun setListeners() {
        binding.layoutLanguagesMainSrc.setOnClickListener {
            if (isDoubleClick()) {
                launchLangScreen(LANGUAGE_TYPE_SOURCE)
            }
        }

        binding.layoutLanguagesMainTarget.setOnClickListener {
            if (isDoubleClick()) {
                launchLangScreen(LANGUAGE_TYPE_TARGET)
            }
        }

        binding.layoutSwapMain.setOnClickListener {
            if (isDoubleClick()) {
                swapViews()
            }
        }
    }

    private fun swapViews() {
        if (!swapClicked) {
            swapClicked = true
            if (isSwitched) {
                isSwitched = false
                binding.layoutSwapMain.animate().rotation(-180f).start()
            } else {
                isSwitched = true
                binding.layoutSwapMain.animate().rotation(180f).start()
            }
            swapLanguages()
            lifecycleScope.launch {
                delay(300)
                swapClicked = false
                getCategoryData()
            }
        }
    }

    private fun swapLanguages() {
        val tempInputName = inputLanguageName
        val tempInputCode = inputLanguageCode
        val tempInputPosition = inputLanguagePosition
        val tempTranslatedName = translatedLanguageName
        val tempTranslatedCode = translatedLanguageCode
        val temTranslatedPosition = translatedLanguagePosition

        translatedLanguageName = tempInputName
        translatedLanguageCode = tempInputCode
        translatedLanguagePosition = tempInputPosition
        inputLanguageName = tempTranslatedName
        inputLanguageCode = tempTranslatedCode
        inputLanguagePosition = temTranslatedPosition

        viewModel.setLanguageData(KEY_PHRASE_INPUT_LANG_NAME, inputLanguageName)
        viewModel.setLanguageData(KEY_PHRASE_INPUT_LANG_CODE, inputLanguageCode)
        viewModel.setLangPosition(KEY_PHRASE_INPUT_LANG_POSITION, inputLanguagePosition)
        viewModel.setLanguageData(KEY_PHRASE_TRANSLATED_LANG_NAME, translatedLanguageName)
        viewModel.setLanguageData(KEY_PHRASE_TRANSLATED_LANG_CODE, translatedLanguageCode)
        viewModel.setLangPosition(KEY_PHRASE_TRANSLATED_LANG_POSITION, translatedLanguagePosition)

        binding.tvSrcNameMain.text = inputLanguageName
        binding.tvTargetNameMain.text = translatedLanguageName
    }

    private fun launchLangScreen(type: String) {
        mContext?.let {
            val intent = Intent(it, LanguageSelectionActivity::class.java)
            intent.putExtra("language_type", type)
            intent.putExtra("language_source", LANGUAGE_SOURCE_PHRASE)
            startForLangResult.launch(intent)
        }
    }

    private fun getCategoryData() {
        viewModel.getPhraseCats(getSourceLanguageCode())
        viewModel.getPhraseCategories().observe(viewLifecycleOwner) { list ->
            categoryList.clear()
            categoryList.addAll(list as ArrayList<PhraseCategory>)
            categoryList.sortBy { it.name }
            adapter?.notifyDataSetChanged()
        }
    }

    private fun setLanguagesData() {
        inputLanguageName = viewModel.getLanguageData(KEY_PHRASE_INPUT_LANG_NAME)
        inputLanguageCode = viewModel.getLanguageData(KEY_PHRASE_INPUT_LANG_CODE)
        translatedLanguageName =
            viewModel.getLanguageData(KEY_PHRASE_TRANSLATED_LANG_NAME)
        translatedLanguageCode =
            viewModel.getLanguageData(KEY_PHRASE_TRANSLATED_LANG_CODE)
        viewModel.checkSaveDefaultSourcePosition()
        viewModel.checkSaveDefaultTargetPosition()
        inputLanguagePosition = viewModel.getLangPosition(KEY_PHRASE_INPUT_LANG_POSITION)
        translatedLanguagePosition =
            viewModel.getLangPosition(KEY_PHRASE_TRANSLATED_LANG_POSITION)

        binding.tvSrcNameMain.text = inputLanguageName
        binding.tvTargetNameMain.text = translatedLanguageName
    }

    private fun getSourceLanguageCode(): String {
        return viewModel.getLanguageData(KEY_PHRASE_INPUT_LANG_CODE)
    }

    private fun launchSentencesActivity(id: Int, title: String, category: String) {
        mContext?.let {
            Log.e("none", "launchSentencesActivity: $id")
            val intent = Intent(it, SentencesActivity::class.java)
            intent.apply {
                putExtra("id", id)
                putExtra("title", title)
                putExtra("category", category)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mContext = null
    }

}