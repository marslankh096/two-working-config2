package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep

@Keep
data class WordResponseItem(
    val word: String,
    val phonetics: List<PhoneticItem>,
    val meanings: List<MeaningItem>
)