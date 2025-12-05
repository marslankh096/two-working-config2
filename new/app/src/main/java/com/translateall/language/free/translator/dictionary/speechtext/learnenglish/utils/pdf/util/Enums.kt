package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util

sealed class DownloadStatus {
    object Started : DownloadStatus()
    object Success : DownloadStatus()
    object Failure : DownloadStatus()
    data class Progress(val progress: Int) : DownloadStatus()
}

enum class saveTo {
    DOWNLOADS,
    ASK_EVERYTIME
}
