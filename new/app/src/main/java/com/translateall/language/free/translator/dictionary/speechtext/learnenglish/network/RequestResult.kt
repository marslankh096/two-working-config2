package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network

sealed class RequestResult {
    data class Success<T>(val data: T) : RequestResult()
    data class Error(val exception: Exception) : RequestResult()
}