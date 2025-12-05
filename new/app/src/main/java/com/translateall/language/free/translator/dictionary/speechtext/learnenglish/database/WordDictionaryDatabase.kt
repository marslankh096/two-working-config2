package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.BookmarkWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.RecentWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.BookmarkWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord

@Database(entities = [RecentWord::class, BookmarkWord::class], version = 1, exportSchema = false)
abstract class WordDictionaryDatabase  : RoomDatabase(){
    abstract val recentWordDao: RecentWordDao
    abstract val bookmarkWordDao: BookmarkWordDao
}