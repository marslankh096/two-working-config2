package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.PhraseFavoriteDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases

@Database(entities = [FavouritePhrases::class], version = 2, exportSchema = false)
abstract class PhraseDatabase : RoomDatabase() {
    abstract val phraseDao: PhraseFavoriteDao
}