package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo

import android.util.Log
import com.google.gson.Gson
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.BookmarkWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.RecentWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.WordSuggestionDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.BookmarkWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.RequestResult
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.dictionary.WordDictionaryService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class WordDictionaryRepository(
    private val wordDictionaryService: WordDictionaryService,
    private val recentWordDao: RecentWordDao,
    private val bookmarkWordDao: BookmarkWordDao,
    private val wordSuggestionDao: WordSuggestionDao
) {

    suspend fun callDictionary(word: String) = flow {
        try {
            val response = wordDictionaryService.getDictionaryWord(word)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val data = Gson().toJson(body)
                    val meaning = "1. ${body[0].meanings[0].definitions[0].definition}"

                    val recent = RecentWord(word, meaning, data, time = System.currentTimeMillis())
                    // check row in table are less than 20
                    val recentList = recentWordDao.getAllRecent().first()
                    if (recentList.size < 20) {
                        recentWordDao.insert(recent)
                    } else {
                        recentWordDao.delete(recentList.last())
                        recentWordDao.insert(recent)
                    }
                    emit(RequestResult.Success(body))
                } else {
                    emit(RequestResult.Error(Exception("no_word")))
                }
            } else {
                emit(RequestResult.Error(Exception("no_word")))
            }
        } catch (e: Exception) {
            emit(RequestResult.Error(e))
        }
    }

    fun getAllRecent() = recentWordDao.getAllRecent()

    fun getAllBookmarks() = bookmarkWordDao.getAllBookmarks()

    fun getWordSuggestions(query: String): List<String> {
        return try {
            wordSuggestionDao.getSuggestedWords(query).map { it.word }
        } catch (e: Exception) {
            Log.e("none", "getWordSuggestions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecentWord(word: String) = flow {
        val recent = recentWordDao.getWord(word)
        if (recent != null) {
            emit(recent)
        } else {
            val bookmark = bookmarkWordDao.checkWordExist(word)
//            val displayMeaning = bookmark.displayMeaning ?: ""
            emit(RecentWord(word, bookmark.displayMeaning, bookmark.response, true, bookmark.time))
        }
    }

    fun bookmarkRecent(data: RecentWord) {
        val bookmark = BookmarkWord(data.word, data.displayMeaning, data.response, data.time)
        if (bookmarkWordDao.checkWordExist(data.word) == null) {
            bookmarkWordDao.insert(bookmark)
            recentWordDao.update(data.word, true)
        } else {
            bookmarkWordDao.delete(bookmark)
            recentWordDao.update(data.word, false)
        }
    }

    fun deleteBookmark(bookmark: BookmarkWord) {
        recentWordDao.update(bookmark.word, false)
        bookmarkWordDao.delete(bookmark)
    }

    fun deleteBookmarkList(selectedItems: List<String>) {
        for (item in selectedItems) {
            recentWordDao.update(item, false)
        }
        bookmarkWordDao.deleteList(selectedItems)
    }


    fun deleteRecentList(selectedItems: List<String>) {
        recentWordDao.deleteList(selectedItems)
    }

}