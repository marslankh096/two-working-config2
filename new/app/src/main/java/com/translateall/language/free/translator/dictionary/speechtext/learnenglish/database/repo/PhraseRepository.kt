package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.PhrasesDbHelper
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.PhraseFavoriteDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCatSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCategory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhrasesSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DEFAULT_SRC_LANG_POSITION_PHRASE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DEFAULT_TAR_LANG_POSITION_PHRASE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_INPUT_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.KEY_PHRASE_TRANSLATED_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.SingleLiveEvent
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.toLowerCaseExceptFirst

class PhraseRepository(
    private val tinyDB: TinyDB,
    private val dbHelper: PhrasesDbHelper,
    private val phraseDb: PhraseFavoriteDao
) {
    private var mLivePhraseCatsList: MutableLiveData<List<PhraseCategory>> = MutableLiveData()
    var exception: MutableLiveData<String> = MutableLiveData()
    private var mLiveSentencesList: SingleLiveEvent<List<PhraseCatSentences>> = SingleLiveEvent()
    fun deleteFavPhrase(id: String) = phraseDb.deleteFavPhrase(id)
    fun insertFavPhrase(data: FavouritePhrases): Long = phraseDb.insertFavPhrase(data)
    fun getFavPhrase(catId: String): String? = phraseDb.getFavoritePhrase(catId)
//    fun getFavPhrases(catId: Int): LiveData<List<FavouritePhrases>> = phraseDb.getFavoritePhrases(catId)

    @SuppressLint("Range")
    fun getCategories(langData: String) {
        val listPhraseCats: ArrayList<PhraseCategory> = ArrayList()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("select * from categories", null)
            while (cursor.moveToNext()) {
                var subText = ""
                val subcategoryList: ArrayList<Pair<String, Int>> = ArrayList()
                val id = cursor.getInt(cursor.getColumnIndex("category_id"))
                val name = cursor.getString(cursor.getColumnIndex(langData))
                val icon = cursor.getString(cursor.getColumnIndex("icon"))
                val priority = cursor.getInt(cursor.getColumnIndex("priority"))
                if (name.isNotEmpty()) {
                    val listPareses: ArrayList<PhraseCatSentences> = ArrayList()
                    try {
                        val cursor2 =
                            db.rawQuery("select * from sections WHERE category_id=${id}", null)
                        while (cursor2.moveToNext()) {
                            val subId = cursor2.getInt(cursor2.getColumnIndex("section_id"))
                            val subName = cursor2.getString(
                                cursor2.getColumnIndex(
                                    sourceLanguageCode()
                                )
                            )
                            val subPriority = cursor2.getInt(cursor2.getColumnIndex("priority"))
                            listPareses.add(
                                PhraseCatSentences(
                                    subId,
                                    subName.toLowerCaseExceptFirst(),
                                    ArrayList(),
                                    priority = subPriority
                                )
                            )
                        }
                        cursor2.close()
                        listPareses.sortBy { it.priority }
                        for (cat in listPareses) {
                            val cursor3 = db.rawQuery(
                                "select count(*) from phrases WHERE section_id=${cat.id}",
                                null
                            )
                            subText = if (subText.isEmpty()) {
                                cat.cateName
                            } else {
                                subText +", " + cat.cateName
                            }
                            cursor3.moveToFirst()
                            val count: Int = cursor3.getInt(0)
                            subcategoryList.add(Pair(cat.cateName, count))
                            cursor3.close()

                        }
                        listPhraseCats.add(PhraseCategory(id, name, priority, icon, subText, subcategoryList))
//                        mLiveSentencesList.postValue(listPareses)
                    } catch (e: Exception) {
                        exception.postValue(e.message)
                        e.printStackTrace()
                    }
                }

            }
            cursor.close()
            listPhraseCats.sortBy { it.priority }
            mLivePhraseCatsList.postValue(listPhraseCats)
        } catch (e: Exception) {
            e.printStackTrace()
            exception.postValue(e.message)
        }
    }

    @SuppressLint("Range")
    fun getPhrases(catId: Int) {
        val listPareses: ArrayList<PhraseCatSentences> = ArrayList()
        val listSentences: ArrayList<PhrasesSentences> = ArrayList()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("select * from sections WHERE category_id=${catId}", null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("section_id"))
                val name = cursor.getString(
                    cursor.getColumnIndex(
                        sourceLanguageCode()
                    )
                )
                val priority = cursor.getInt(cursor.getColumnIndex("priority"))
                listPareses.add(PhraseCatSentences(id, name, listSentences, priority = priority))
            }
            cursor.close()
            listPareses.sortBy { it.priority }
            for (cat in listPareses) {
                val cursor1 = db.rawQuery("select * from phrases WHERE section_id=${cat.id}", null)

                while (cursor1.moveToNext()) {
                    val name = cursor1.getString(
                        cursor1.getColumnIndex(
                            sourceLanguageCode()
                        )
                    )
                    val translation = cursor1.getString(
                        cursor1.getColumnIndex(
                            transitionLanguageCode()
                        )
                    )
                    val id = cursor1.getInt(cursor1.getColumnIndex("phrase_id"))
//                    val fav = phraseDb.getFavoritePhrase(
//                        getFavTransId(id)
//                    ) != null
                    val priority = cursor1.getInt(cursor1.getColumnIndex("priority"))
                    val check = phraseDb.getFavoritePhrase(
                        "${id}_${
                            sourceLanguageCode() + "_" + transitionLanguageCode()
                        }"
                    )
                    val isFavorite = check != null
                    listSentences.add(
                        PhrasesSentences(
                            id,
                            cat.id,
                            name,
                            translation,
                            false,
                            priority = priority,
                            isFavorite
                        )
                    )
                    listSentences.sortBy { it.priority }
                    cat.list = ArrayList(listSentences)
                }
                cursor1.close()
                listSentences.clear()

            }
            mLiveSentencesList.postValue(listPareses)
        } catch (e: Exception) {
            exception.postValue(e.message)
            e.printStackTrace()
        }

    }

    private fun getFavTransId(id: Int): String {
        return "${id}_${
            sourceLanguageCode() + "_" + transitionLanguageCode()
        }"
    }

    private fun transitionLanguageCode(): String {
        return tinyDB.phraseTranslatedLangCode
    }

    private fun sourceLanguageCode(): String {
        return tinyDB.phraseInputLangCode
    }

    fun getPhraseCats(): LiveData<List<PhraseCategory>> {
        return mLivePhraseCatsList
    }

    fun getPhraseSentence(): SingleLiveEvent<List<PhraseCatSentences>> {
        return mLiveSentencesList
    }

    fun checkSaveDefaultSourcePosition() {
        val position = tinyDB.getInt(KEY_PHRASE_INPUT_LANG_POSITION)
        if (position == -1)
            tinyDB.putInt(KEY_PHRASE_INPUT_LANG_POSITION, DEFAULT_SRC_LANG_POSITION_PHRASE)
    }

    fun checkSaveDefaultTargetPosition() {
        val position = tinyDB.getInt(KEY_PHRASE_TRANSLATED_LANG_POSITION)
        if (position == -1)
            tinyDB.putInt(KEY_PHRASE_TRANSLATED_LANG_POSITION, DEFAULT_TAR_LANG_POSITION_PHRASE)
    }

    fun getLangPosition(key: String): Int {
        return tinyDB.getInt(key)
    }

    fun setLangPosition(key: String, value: Int) {
        tinyDB.putInt(key, value)
    }

    fun setLanguageData(key: String, value: String) {
        tinyDB.putString(key, value)
    }

    fun getLanguageData(key: String): String {
        var value = tinyDB.getString(key)
        if (value == "") {
            when (key) {
                KEY_PHRASE_INPUT_LANG_NAME -> {
                    value = "English"
                }

                KEY_PHRASE_INPUT_LANG_CODE -> {
                    value = "en-US"
                }

                KEY_PHRASE_TRANSLATED_LANG_NAME -> {
                    value = "French"
                }

                KEY_PHRASE_TRANSLATED_LANG_CODE -> {
                    value = "fr-FR"
                }
            }
            setLanguageData(key, value)
        }
        return value
    }


}