package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.dictionary

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.WordDictionaryRepository
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.RequestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DictionaryViewModel(
    private val dictionaryRepo: WordDictionaryRepository
    ): ViewModel() {
        val wordLiveData = MutableLiveData<WordResponse?>()
        val recentWordList = MutableLiveData<List<RecentWord>?>()
        val suggestionLiveData = MutableLiveData<List<String>>()
        fun searchWord(context: Context, word: String) {
            viewModelScope.launch {
                dictionaryRepo.callDictionary(word.trim()).collect {
                    when (it) {
                        is RequestResult.Success<*> -> {
                            wordLiveData.value = it.data as WordResponse
                        }
                        is RequestResult.Error -> {
                            if (it.exception.message == "no_word") {
                                Toast.makeText(context,
                                    context.getString(R.string.word_not_found), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context,
                                    context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                            }
                            wordLiveData.value = null
                        }

                        else -> {}
                    }
                }
            }
        }

    fun getRecentWordsList() {
        viewModelScope.launch {
            dictionaryRepo.getAllRecent().flowOn(Dispatchers.IO).collectLatest {
                recentWordList.value = it
            }
        }
    }

    fun setBookmarkStatus(item: RecentWord) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepo.bookmarkRecent(item)
        }
    }
    fun deleteRecentList(selectedItems: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepo.deleteRecentList(selectedItems)
        }
    }

    fun getWordSuggestions(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val suggestionWords = dictionaryRepo.getWordSuggestions(word)
            suggestionLiveData.postValue(suggestionWords)
        }
    }

}