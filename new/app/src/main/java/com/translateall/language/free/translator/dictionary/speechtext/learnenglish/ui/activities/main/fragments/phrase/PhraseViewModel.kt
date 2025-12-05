package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.PhraseRepository
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCatSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCategory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class PhraseViewModel (
    private val repo: PhraseRepository,
) : ViewModel() {
    private val listPhraseCats: LiveData<List<PhraseCategory>> = repo.getPhraseCats()
    private val listPhrase: SingleLiveEvent<List<PhraseCatSentences>> = repo.getPhraseSentence()
    val exception: MutableLiveData<String> = repo.exception
    var isSearching = false
    var query = ""
    var isPhraseSearching = false
    var queryPhrases = ""

    suspend fun getFavPhrase(id: String): String? = repo.getFavPhrase(id)

    suspend fun addData(data: FavouritePhrases): Long = repo.insertFavPhrase(data)

    suspend fun deleteFavPhrase(id: String) = repo.deleteFavPhrase(id)

    //    fun getFavPhrases(catId: Int): LiveData<List<FavouritePhrases>> = repo.getFavPhrases(catId)
    fun getPhraseCats(langCode: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.getCategories(langCode)
    }


    fun getPhrases(catId: Int) {
        repo.getPhrases(catId)
    }

    fun getPhraseCategories(): LiveData<List<PhraseCategory>> {
        return listPhraseCats
    }

    fun getPhrases(): LiveData<List<PhraseCatSentences>> {
        return listPhrase
    }

    fun checkSaveDefaultSourcePosition(){
        repo.checkSaveDefaultSourcePosition()
    }

    fun checkSaveDefaultTargetPosition(){
        repo.checkSaveDefaultTargetPosition()
    }

    fun getLangPosition(key: String): Int {
        return repo.getLangPosition(key)
    }

    fun setLangPosition(key: String, value: Int) {
        repo.setLangPosition(key,value)
    }

    fun setLanguageData(key: String, value: String) {
        repo.setLanguageData(key, value)
    }

    fun getLanguageData(key: String): String {
        return repo.getLanguageData(key)
    }

    //pass tinyDB.phraseTranslatedLangCode
    fun getLocale(): Locale {
        return when (val targetLang = getLanguageData(KEY_PHRASE_TRANSLATED_LANG_CODE)) {
            "tl" -> {
                Locale("fil", "PH")
            }

            "id" -> {
                Locale("id", "ID")
            }

            "en" -> {
                Locale("en", "US")
            }

            "sq" -> {
                Locale("alb", "AL")
            }

            "fr" -> {
                Locale.FRANCE
            }

            "zh" -> {
                Locale.CHINA
            }

            "ur" -> {
                Locale("ur-PK")
            }

            "ar" -> {
                Locale("ar-SA")
            }

            "hr" -> {
                Locale("en", "UK")
            }

            "bs" -> {
                Locale("alb", "AL")
            }

            "sw" -> {
                Locale("swc", "CD")
            }

            "cy" -> {
                Locale("en", "UK")
            }

            else -> {
                Locale(targetLang)
            }
        }
    }

    fun getRecognizerIntent(s: String): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, s)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, s)
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, s)
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, s)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, s)
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, s)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        return intent
    }

}