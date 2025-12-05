package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep

@Keep
data class WordOfDay(
    val word: String,
    val definition: String,
    val date: String
)
