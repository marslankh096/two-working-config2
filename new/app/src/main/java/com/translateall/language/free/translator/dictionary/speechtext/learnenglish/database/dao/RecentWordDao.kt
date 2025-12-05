package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentWordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(recent: RecentWord)
    @Query("SELECT * FROM recent_word ORDER BY time DESC")
    fun getAllRecent(): Flow<List<RecentWord>>

    @Query("SELECT * FROM recent_word WHERE word = :word")
    fun getWord(word: String): RecentWord

    @Query("DELETE FROM recent_word WHERE word IN (:selectedItems)")
    fun deleteList(selectedItems: List<String>)

    @Delete
    fun delete(recent: RecentWord)

    @Query("UPDATE recent_word SET bookmark = :bookmark WHERE word = :word")
    fun update(word: String, bookmark: Boolean)
}