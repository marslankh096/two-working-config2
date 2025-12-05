package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary

import androidx.annotation.Keep

@Keep
data class DefinitionItem(
    val definition: String,
    val synonyms: List<String>?,
    val antonyms: List<String>?,
    val example: String?
)