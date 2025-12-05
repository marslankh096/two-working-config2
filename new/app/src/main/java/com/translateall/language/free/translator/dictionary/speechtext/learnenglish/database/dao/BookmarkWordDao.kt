package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.BookmarkWord
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookmark: BookmarkWord)
    @Query("SELECT * FROM bookmark_word ORDER BY time DESC")
    fun getAllBookmarks(): Flow<List<BookmarkWord>>

    @Query("SELECT * FROM bookmark_word WHERE word = :word")
    fun checkWordExist(word: String): BookmarkWord
    @Delete
    fun delete(bookmark: BookmarkWord)
    @Query("DELETE FROM bookmark_word WHERE word IN (:selectedItems)")
    fun deleteList(selectedItems: List<String>)
}