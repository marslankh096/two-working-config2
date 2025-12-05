package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private var context: Context? = null

    init {
        this.context = application.applicationContext
    }

    fun getLangData(key: String): String {
        return context!!.getString(key)

    }

    fun setLangData(key: String, value: String) {
        context!!.putString(key, value)
    }

    fun getLangPosition(key: String): Int {
        return context!!.getPrefInt(key)
    }

    fun putLangPosition(key: String, value: Int) {
        context!!.putInt(key, value)
    }


    fun fetchLanguages(): ArrayList<LanguageModel> {

        return fetchAllLanguages()

    }

}