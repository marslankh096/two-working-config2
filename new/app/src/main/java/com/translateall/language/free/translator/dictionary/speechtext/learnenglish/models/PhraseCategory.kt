package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models

data class PhraseCategory(
    val id: Int,
    val name: String,
    val priority: Int,
    val icon: String?,
    val subText: String,
    val subcategoryList: ArrayList<Pair<String, Int>>,
    var isExpanded:Boolean = false
)
