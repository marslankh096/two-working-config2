package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity (tableName = "words")
data class SuggestedWord(
    @PrimaryKey(autoGenerate = false) val word: String
)
