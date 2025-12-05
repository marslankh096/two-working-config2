package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models

data class PhrasesSentences(
    val id: Int,
    val cat_id: Int,
    val phraseSentences: String,
    val translation: String,
    var open: Boolean,
    val priority: Int,
    var favourite: Boolean = false,
    var isProcessing: Boolean = false,
    var isSpeaking: Boolean = false,
    var durationInMillis: Int = 0,
    var progress: Int = 0,
    var maxProgress: Int = 0
)
