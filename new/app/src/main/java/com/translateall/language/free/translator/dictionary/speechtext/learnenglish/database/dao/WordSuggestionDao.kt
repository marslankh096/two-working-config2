package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.SuggestedWord

@Dao
interface WordSuggestionDao {
    @Query("SELECT * FROM words WHERE word LIKE :query || '%' LIMIT 5")
    fun getSuggestedWords(query: String): List<SuggestedWord>
}