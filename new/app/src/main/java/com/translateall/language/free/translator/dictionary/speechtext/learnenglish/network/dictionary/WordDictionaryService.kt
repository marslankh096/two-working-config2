package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.dictionary

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import retrofit2.http.GET

interface WordDictionaryService {
    @GET("entries/en/{word}")
    suspend fun getDictionaryWord(
        @retrofit2.http.Path("word") word: String
    ): retrofit2.Response<WordResponse>
}