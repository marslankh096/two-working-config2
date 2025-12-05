package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database

import androidx.room.RoomDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.WordSuggestionDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.SuggestedWord

@androidx.room.Database(entities = [SuggestedWord::class], version = 1, exportSchema = false)
abstract class WordSuggestionDatabase : RoomDatabase() {
    abstract val suggestedWordDao: WordSuggestionDao
}