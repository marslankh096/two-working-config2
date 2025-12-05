package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep

@Keep
data class MeaningItem(
    val partOfSpeech: String,
    val definitions: List<DefinitionItem>,
    val synonyms: List<String>,
    val antonyms: List<String>
)