package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.annotation.RequiresApi
import java.util.*

class TextToSpeechUtil(private val mContext: Context) : OnInitListener {
    var mTts: TextToSpeech? = null
    fun initEngine() {
        mTts = TextToSpeech(mContext, this, "com.google.android.tts")
    }

    override fun onInit(p0: Int) {

        if (p0 == TextToSpeech.SUCCESS) {
            mTts?.setSpeechRate(0.7f)
            mTts?.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {


                }

                override fun onDone(utteranceId: String) {


                }

                override fun onError(utteranceId: String) {


                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)

                }

            })

        }
    }

    private fun checkTTSSpeaking() {

        mTts?.let {
            if (it.isSpeaking)
                it.stop()
        }

    }

    fun speakTranslation(speakingWord: String, langCode: String) {

        checkTTSSpeaking()

        val langLocal = getLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)

    }

    fun stopEngine() {

        mTts?.let {
            if (it.isSpeaking)
                it.shutdown()
        }
    }

    fun pauseEngine() {
        checkTTSSpeaking()

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun setMaleVoice() {
        val a: MutableSet<String> = HashSet()
        a.add("male")
        val v = Voice("en-us-x-sfg#male_3-local", Locale("en", "US"), 400, 200, true, a)
        mTts?.voice = v
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun setFemaleVoice() {
        val a: MutableSet<String> = HashSet()
        a.add("female")
        val v = Voice("en-gb-x-rjs#female_3-local", Locale("en", "UK"), 400, 200, true, a)
        mTts?.voice = v
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
                Locale("en", "US")
            }
            "sq" -> {
                Locale("sq", "AL")
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
            else -> {
                Locale(targetLang)
            }
        }
    }
}