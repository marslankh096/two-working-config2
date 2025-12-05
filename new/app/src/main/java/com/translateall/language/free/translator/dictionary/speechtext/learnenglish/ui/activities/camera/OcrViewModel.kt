package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.camera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import org.intellij.lang.annotations.Language
import java.util.ArrayList

class OcrViewModel(application: Application) : AndroidViewModel(application) {
    private var context: Context? = null
    private var myDataBase: MyDataBase? = null

    init {
        myDataBase = MyDataBase.getInstance(application)
        this.context = application.applicationContext
    }

    fun getLangData(key: String): String {
        return context!!.getString(key)

    }

    fun setLangData(key: String, value: String) {
        context!!.putString(key, value)
    }

    fun getLangPosition(key: String): Int {
        return context!!.getLangInt(key)
    }

    fun putLangPosition(key: String, value: Int) {
        context!!.putInt(key, value)
    }


    fun extractText(
        bitmap: Bitmap,
        inputLang: String,
        resultSuccess: (String?) -> Unit,
        languageResult: (String?, String?) -> Unit,
        onErrorDetected: (String) -> Unit,
    ) {
        kotlin.runCatching {

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { mSuccess ->

                    var languageDetected = ""
                    for (blocks in mSuccess.textBlocks) {
                        languageDetected = blocks.recognizedLanguage
                    }
                    mSuccess.let { result ->
                        var originalStr = result.text
                        originalStr = originalStr.replace("\n", " ")
                        if (languageDetected == inputLang) {
                            resultSuccess.invoke(originalStr)
                        } else {
                            val languageName = getOcrLangNameFromList(languageDetected)
                            if (languageName != null ) {

                                languageResult.invoke(languageDetected, originalStr)
                            }
                            else if (originalStr.isNotEmpty()){
                                resultSuccess.invoke(originalStr)
                            }

                            else {
                                onErrorDetected.invoke("default_lang")
                            }
                        }
                    }

                }
                .addOnFailureListener {
                    onErrorDetected.invoke(it.localizedMessage)
                }
        }.onFailure {
            onErrorDetected.invoke(it.localizedMessage)

        }

    }

    fun getOcrLangNameFromList(languageDetected: String): String? {
        val langList: List<LanguageModel> = langsDataOCr()
        val countries: MutableList<String> =
            java.util.ArrayList()
        for (Language in langList) {
            countries.add(Language.languageCode)
        }
        val selectedId = countries.indexOf(languageDetected)
        return if (selectedId >= 0) {
            langList[selectedId].languageName
        } else null
    }

    fun getLanguageNameFromList(languageDetected: String): String? {
        val langList: List<LanguageModel> = fetchAllLanguages()
        val countries: MutableList<String> = ArrayList()
        for (languageModel in langList) {
            countries.add(languageModel.languageCode)
        }
        val selectedId = countries.indexOf(languageDetected)
        return if (selectedId >= 0) {
            langList[selectedId].languageName
        } else null
    }
    fun getLanguagePositionFromList(languageDetected: String):Int{
        val langList: List<LanguageModel> = langsDataOCr()
        val countries: MutableList<String> =
            java.util.ArrayList()
        for (Language in langList) {
            countries.add(Language.languageCode)
        }
        val selectedId = countries.indexOf(languageDetected)
        return selectedId
    }

    suspend fun insertHistory(
        translationHistory: TranslationHistory,
    ) {
        translationHistory.apply {
            val size = getDatabaseSize()
            size?.let {
                id = it + 1
                setDatabase(this)
            } ?: run {
                setDatabase(this)
            }

        }


    }

    private suspend fun setDatabase(translationHistory: TranslationHistory) {
        kotlin.runCatching {

            myDataBase?.translationDao()?.insert(translationHistory)
        }


    }

    private fun getDatabaseSize(): Int? {
        return myDataBase?.translationDao()?.all?.size
    }

}