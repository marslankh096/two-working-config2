package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models

data class PhraseCatSentences(
    val id: Int,
    val cateName: String,
    var list: List<PhrasesSentences>,
    val priority: Int,
    var isFav: Boolean = false
)