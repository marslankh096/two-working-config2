package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.WordDictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictionaryDetailViewModel(
    private val wordRepo: WordDictionaryRepository,
) : ViewModel() {
    val wordDetail = MutableLiveData<RecentWord>()

    fun getWordDetail(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            wordRepo.getRecentWord(word.trim()).collect {
                withContext(Dispatchers.Main) {
                    wordDetail.value = it
                }
            }
        }
    }

    fun setBookmark(word: RecentWord) {
        viewModelScope.launch(Dispatchers.IO) {
            wordRepo.bookmarkRecent(word)
        }
    }

}