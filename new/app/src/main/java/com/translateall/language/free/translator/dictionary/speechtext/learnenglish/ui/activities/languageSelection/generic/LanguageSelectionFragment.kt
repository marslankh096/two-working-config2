package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.generic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentLanguageSelectionBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.adapter.LanguageSelectionAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguageSelectionFragment : Fragment() {

    private lateinit var binding: FragmentLanguageSelectionBinding
    private var languageAdapter: LanguageSelectionAdapter? = null
    private var langList: ArrayList<LanguageModel> = ArrayList()

    private var mContext: Context? = null
    private var langType: String? = null
    private var langListType: String? = null
    private var from: String? = null

    companion object {
        fun newInstance(type: String, listType: String, from: String): LanguageSelectionFragment {
            val fragment = LanguageSelectionFragment().apply {
                arguments = Bundle().apply {
                    putString("language_type", type)
                    putString("list_type", listType)
                    putString("from", from)
                }
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
//        val rootView = inflater.inflate(R.layout.fragment_language_selection, container, false)
        binding = FragmentLanguageSelectionBinding.inflate(layoutInflater)
        val bundle = arguments
        if (bundle != null) {
            langType = bundle.getString("language_type")
            langListType = bundle.getString("list_type")
            from = bundle.getString("from")
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            if (isAdded && isVisible)
                setLanguages()
        }
    }

    var selectedPosition: Int = 0
    private fun setLanguages() {
        if (mContext == null) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                if (isAdded && isVisible) {
                    setLanguages()
                }
            }
        } else {
            mContext?.let { ctx ->
                binding.apply {
                    languageAdapter = LanguageSelectionAdapter()
                    var sourcePositionString: String
                    var targetPositionString: String
                    var defaultSelectedPosition: Int

                    if (langListType!! == LANGUAGE_LIST_TYPE_ALL) {
                        sourcePositionString = SOURCE_LANG_POSITION
                        targetPositionString = TARGET_LANG_POSITION
                        defaultSelectedPosition = DEFAULT_SRC_LANG_POSITION
                        langList = fetchLanguages()
                    } else if (langListType == LANGUAGE_LIST_TYPE_PHRASE) {
                        sourcePositionString = KEY_PHRASE_INPUT_LANG_POSITION
                        targetPositionString = KEY_PHRASE_TRANSLATED_LANG_POSITION
                        defaultSelectedPosition = DEFAULT_SRC_LANG_POSITION_PHRASE
                        langList = fetchPhraseLanguages()
                    } else {
                        sourcePositionString = SOURCE_LANG_POSITION_OCR
                        targetPositionString = TARGET_LANG_POSITION
                        defaultSelectedPosition = DEFAULT_SRC_LANG_POSITION_OCR
                        langList = fetchOCRLanguages()
                    }

                    if (langType == LANGUAGE_TYPE_SOURCE) {
                        selectedPosition = ctx.getLangInt(sourcePositionString)
                        if (selectedPosition == -1) {
                            selectedPosition = defaultSelectedPosition
                            ctx.putInt(sourcePositionString, selectedPosition)
                        }

                    } else {
                        selectedPosition = ctx.getLangInt(targetPositionString)
                        if (selectedPosition == -1) {
                            selectedPosition = if (langListType != LANGUAGE_LIST_TYPE_PHRASE)
                                DEFAULT_TAR_LANG_POSITION
                            else
                                DEFAULT_TAR_LANG_POSITION_PHRASE
                            ctx.putInt(targetPositionString, selectedPosition)
                        }

                    }


//            langList[selectedPosition].isSelected = true
                    binding.rvLanguages.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvLanguages.adapter = languageAdapter

                    languageAdapter?.setData(langList, selectedPosition)

                    binding.rvLanguages.smoothScrollToPosition(selectedPosition)
                    binding.progressLanguages.visibility = View.GONE
                    binding.containerLanguages.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun fetchLanguages(): ArrayList<LanguageModel> {
        return fetchAllLanguages()
    }

    private fun fetchPhraseLanguages(): ArrayList<LanguageModel> {
        return getPhraseLanguages()
    }

    private fun fetchOCRLanguages(): ArrayList<LanguageModel> {
        return langsDataOCr()
    }

    fun filterLanguage(text: String) {
        val filteredList = ArrayList<LanguageModel>()
        val list = if (langListType!! == LANGUAGE_LIST_TYPE_ALL) {
            fetchLanguages()
        } else if (langListType!! == LANGUAGE_LIST_TYPE_PHRASE) {
            mContext?.let {
                fetchPhraseLanguages()
            }
        } else {
            fetchOCRLanguages()
        }
        if (list != null) {
            for (la in list) {
                if ((la.languageName != null && la.languageName.toLowerCase()
                        .startsWith(text.toLowerCase())) || (la.langMean != null && la.langMean.startsWith(
                        text.toLowerCase()
                    ))
                ) {
                    filteredList.add(la)
                }
                languageAdapter?.setData(filteredList, selectedPosition)
            }
        }

    }

    fun setUnFilteredList() {
        languageAdapter?.setData(langList, selectedPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}