package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.dictionary

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WordDictionaryClient {

    private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/"
    val wordDictionaryService: WordDictionaryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(makeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WordDictionaryService::class.java)
    }

    private fun makeOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(makeLoggingInterceptor())
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    private fun makeLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

}