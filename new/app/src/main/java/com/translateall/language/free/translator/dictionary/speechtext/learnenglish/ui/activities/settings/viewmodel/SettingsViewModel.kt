package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.settings.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import java.io.File
import java.util.*


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var context: Context
    private var myDataBase: MyDataBase? = null


    init {
        this.context = application.applicationContext
        myDataBase = MyDataBase.getInstance(application)
    }

    fun getFavoriteList(): LiveData<MutableList<TranslationHistory>>? {
       return myDataBase?.translationDao()?.getFavorites(true)
    }

    fun getHistoryList(): LiveData<MutableList<TranslationHistory>>? {
        return myDataBase?.translationDao()?.allHistory
    }

}