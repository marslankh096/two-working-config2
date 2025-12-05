package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "bookmark_word")
data class BookmarkWord(
    @PrimaryKey(autoGenerate = false) val word: String,
    val displayMeaning: String,
    val response: String,
    val time: Long
) : Serializable {
    @Ignore
    var isSelected: Boolean = false
}