package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryBookmark

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.BookmarkWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.WordDictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val dictionaryRepo: WordDictionaryRepository
) : ViewModel() {

    val bookmarkList = MutableLiveData<List<BookmarkWord>?>()

    fun getBookmarkList() {
        viewModelScope.launch {
            dictionaryRepo.getAllBookmarks().flowOn(Dispatchers.IO).collectLatest {
                bookmarkList.value = it
            }
        }
    }

    fun deleteBookmark(bookmark: BookmarkWord) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepo.deleteBookmark(bookmark)
        }
    }

    fun deleteBookmarkList(selectedWords: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepo.deleteBookmarkList(selectedWords)
        }
    }

}