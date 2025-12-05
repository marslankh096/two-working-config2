package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import org.intellij.lang.annotations.Language
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

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

    fun getMicCode(langCode: String): String? {
        when (langCode) {
            "af" -> return "af-ZA"
            "am" -> return "am-ET"
            "sq" -> return "sq-AL"
            "ar" -> return "ar-SA"
            "hy" -> return "hy-AM"
            "az" -> return "az-AZ"
            "eu" -> return "eu-ES"
            "bn" -> return "bn-BD"
            "bg" -> return "bg-BG"
            "ca" -> return "ca-ES"
            "zh" -> return "cmn-Hans-CN"
            "hr" -> return "hr-HR"
            "cs" -> return "cs-CZ"
            "da" -> return "da-DK"
            "nl" -> return "nl-NL"
            "en" -> return "en-US"
            "et" -> return "et-EE"
            "fi" -> return "fi-FI"
            "fr" -> return "fr-FR"
            "tl" -> return "fil-PH"
            "gl" -> return "gl-ES"
            "ka" -> return "ka-GE"
            "de" -> return "de-DE"
            "el" -> return "el-GR"
            "gu" -> return "gu-IN"
            "he" -> return "he-IL"
            "hi" -> return "hi-IN"
            "hu" -> return "hu-HU"
            "is" -> return "is-IS"
            "id" -> return "id-ID"
            "it" -> return "it-IT"
            "ja" -> return "ja-JP"
            "jw" -> return "jw-ID"
            "kn" -> return "kn-IN"
            "km" -> return "km-KH"
            "ko" -> return "ko-KR"
            "lo" -> return "lo-LA"
            "lv" -> return "lv-LV"
            "lt" -> return "lt-LT"
            "ms" -> return "ms-MY"
            "ml" -> return "ml-IN"
            "mr" -> return "mr-IN"
            "my" -> return "my-MM"
            "ne" -> return "ne-NP"
            "nb" -> return "nb-NO"
            "fa" -> return "fa-IR"
            "pl" -> return "pl-PL"
            "pt" -> return "pt-PT"
            "pa" -> return "pa-Guru-IN"
            "ro" -> return "ro-RO"
            "ru" -> return "ru-RU"
            "sr" -> return "sr-RS"
            "sk" -> return "sk-SK"
            "sl" -> return "sl-SI"
            "es" -> return "es-ES"
            "su" -> return "su-ID"
            "sw" -> return "sw-TZ"
            "sv" -> return "sv-SE"
            "ta" -> return "ta-IN"
            "te" -> return "te-IN"
            "th" -> return "th-TH"
            "tr" -> return "tr-TR"
            "uk" -> return "uk-UA"
            "ur" -> return "ur-PK"
            "uz" -> return "uz-UZ"
            "vi" -> return "vi-VN"
            "zu" -> return "zu-ZA"
            else -> {
                return null
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

    suspend fun insertHistory(translationHistory: TranslationHistory) {
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

     fun getLanguagePositionFromList(languageCode: String): Int {
        val codes: MutableList<String> = java.util.ArrayList()
        for (Language in fetchAllLanguages()) {
            codes.add(Language.languageCode)
        }
        val selectedId = codes.indexOf(languageCode)
        return selectedId

    }

    fun triggerAudioPermission(
        activity: Activity,
        permissionName: String,
        allowed: (Boolean) -> Unit,
    ) {
        Permissions.check(
            activity,
            permissionName,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    allowed.invoke(true)
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    super.onDenied(context, deniedPermissions)
                    allowed.invoke(false)
                }
            })
    }

    fun isAppInstalled(activity: Activity, packageName: String): Boolean {
        val pm = activity.packageManager
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            pm.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }

    }

    fun getLocale(targetLang: String): Locale {
        return when (targetLang) {
            "tl" -> {
                Locale("fil", "PH")
            }
            "id" -> {
                Locale("id", "ID")
            }
            "en" -> {
                Locale("en", "UK")
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

    fun getHistoryList(): LiveData<MutableList<TranslationHistory>>? {
        return myDataBase?.translationDao()?.allHistory
    }

}