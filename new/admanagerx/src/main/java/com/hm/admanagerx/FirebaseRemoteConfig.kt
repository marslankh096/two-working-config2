package com.hm.admanagerx

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


private const val FETCH_TIME_INTERVAL = 1 * 60 * 60L   //  hours * minutes * seconds


var configMap: Map<String, Any>? = null

fun checkDefaultConfigValue(key: String): Boolean = configMap?.get(key) == null

fun isRemoteShowAd(adRemoteConfigKey: String): Boolean {
    if (checkDefaultConfigValue(adRemoteConfigKey)) return true
    return FirebaseRemoteConfig.getInstance().getBoolean(adRemoteConfigKey)
}

fun remoteCountShowAd(adRemoteConfigKey: String) =
    FirebaseRemoteConfig.getInstance().getLong(adRemoteConfigKey)

fun initFirebaseRemoteConfig(onComplete: ((Boolean) -> Unit)? = null) {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(1)
        .build()
    remoteConfig.setConfigSettingsAsync(configSettings)

    configMap = mapOf(
        IS_CHECK_OPEN_ONBOARD to true,
        IS_CHECK_OPEN_FIRST_LANGUAGE to true,
        IS_UNLOCK_CAMERA to true,
        /*IS_CHECK_LANGUAGE_RELOAD to false,

        //^^ checks booleans

        IS_SHOW_ONBOARD_NATIVE_1 to true,
        IS_SHOW_ONBOARD_NATIVE_2 to true,
        IS_SHOW_ONBOARD_NATIVE_3 to true,
        IS_SHOW_LANGUAGE_NATIVE to true,
        IS_SHOW_HISTORY_NATIVE to true,
        IS_SHOW_FAVOURITE_NATIVE to true,
        IS_SHOW_SETTING_NATIVE to true,
        IS_SHOW_EXIT_DIALOG_NATIVE to true,

        //^^ Native Ads

        IS_SHOW_BANNER_HOME_SCREEN to true,
        IS_SHOW_BANNER_DICTIONARY_SCREEN to true,
        IS_SHOW_BANNER_CAMERA_SCREEN to true,
        IS_SHOW_BANNER_CROP_SCREEN to true,
        IS_SHOW_BANNER_CONVERSATION_SCREEN to true,
        IS_SHOW_BANNER_DICTIONARY_DETAIL_SCREEN to true,
        IS_SHOW_BANNER_LANGUAGE_SELECT_SCREEN to true,
        IS_SHOW_BANNER_LANGUAGE_SELECT_OCR_SCREEN to true,

        //^^ Banner Ads

        IS_SHOW_APP_OPEN_AD to true,

        //^^App open ad

        isShowInterstitialLoader to true,
        IS_CHECK_SPLASH_AD to true,
        OPEN_MIC_INTER_AD to true,
        OPEN_CAMERA_INTER_AD to true,
        OPEN_CONVERSATION_INTER_AD to true,
        OPEN_INPUT_INTER_AD to true,
        DICTIONARY_SEARCH_INTER_AD to true,
        INPUT_SCREEN_TRANSLATE_BTN_INTER_AD to true,
        INPUT_SCREEN_BACK_INTER_AD to true,
        OPEN_DICTIONARY_RECENT_INTER_AD to true,
        CAMERA_BACK_INTER_AD to true,
        CAMERA_CAPTURE_INTER_AD to true,
        CAMERA_RETAKE_INTER_AD to true,
        CAMERA_TRANSLATE_RESULT_INTER_AD to true,
        CONVERSATION_BACK_INTER_AD to true,
        OPEN_DICTIONARY_FAV_INTER_AD to true,
        DICTIONARY_RESULT_BACK_INTER_AD to true,
        FAVOURITE_BACK_INTER_AD to true,
        HISTORY_BACK_INTER_AD to true,
        LANGUAGE_SELECTION_BACK_INTER_AD to true,
        LANGUAGE_SELECTION_LANG_SELECT_INTER_AD to true,
        OPEN_FAV_SCREEN_FROM_SETTING_INTER_AD to true,
        OPEN_HISTORY_SCREEN_FROM_SETTING_INTER_AD to true

        ///^^Inter ads*/
    ).apply {
        remoteConfig.setDefaultsAsync(this)
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Fetch remote config with timeout in IO thread
            val fetchResult = withTimeoutOrNull(8000) {
                remoteConfig.fetchAndActivate().await()
            }

            withContext(Dispatchers.Main) {
                if (fetchResult == null) {
                    // Timeout occurred
                    Log.e("initRemoteConfing", "Firebase fetch timeout")
                    onComplete?.invoke(false)
                } else {
                    // Fetch successful
                    Log.e("initRemoteConfing", "Firebase fetch successful")
                    onComplete?.invoke(true)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("initRemoteConfing", "Firebase fetch failed: ${e.message}")
                onComplete?.invoke(false)
            }
        }
    }

}

fun String.getLongRemoteConfigValue(): Long {
    return FirebaseRemoteConfig.getInstance()
        .getLong(this)
}

fun String.getStringRemoteConfigValue(): String {
    return FirebaseRemoteConfig.getInstance()
        .getString(this)
}

fun String.equals(list: List<String>, ignoreCase: Boolean = false): Boolean {
    return list.any { this.equals(it, ignoreCase) }
}


fun String.getBooleanRemoteConfigValue(): Boolean {
    return FirebaseRemoteConfig.getInstance()
        .getBoolean(this)
}


const val IS_CHECK_OPEN_ONBOARD = "IS_CHECK_OPEN_ONBOARD"
const val IS_CHECK_OPEN_FIRST_LANGUAGE = "IS_CHECK_OPEN_FIRST_LANGUAGE"
const val IS_UNLOCK_CAMERA = "IS_UNLOCK_CAMERA"
const val IS_CHECK_OPEN_PLANS = "IS_CHECK_OPEN_PLANS"
const val IS_SHOW_TRANSLATE_INTER_AD = "IS_SHOW_TRANSLATE_INTER_AD"


/*
const val IS_CHECK_LANGUAGE_RELOAD = "IS_CHECK_LANGUAGE_RELOAD"
const val IS_SHOW_APP_OPEN_AD = "IS_SHOW_APP_OPEN_AD"
const val IS_SHOW_ONBOARD_NATIVE_1 = "IS_SHOW_ONBOARD_NATIVE_1"
const val IS_SHOW_ONBOARD_NATIVE_2 = "IS_SHOW_ONBOARD_NATIVE_2"
const val IS_SHOW_ONBOARD_NATIVE_3 = "IS_SHOW_ONBOARD_NATIVE_3"
const val IS_SHOW_LANGUAGE_NATIVE = "IS_SHOW_LANGUAGE_NATIVE"
const val IS_SHOW_HISTORY_NATIVE = "IS_SHOW_HISTORY_NATIVE"
const val IS_SHOW_FAVOURITE_NATIVE = "IS_SHOW_FAVOURITE_NATIVE"
const val IS_SHOW_SETTING_NATIVE = "IS_SHOW_SETTING_NATIVE"
const val IS_SHOW_EXIT_DIALOG_NATIVE = "IS_SHOW_EXIT_DIALOG_NATIVE"
///^^Native Ad

const val IS_SHOW_BANNER_HOME_SCREEN = "IS_SHOW_BANNER_HOME_SCREEN"
const val IS_SHOW_BANNER_DICTIONARY_SCREEN = "IS_SHOW_BANNER_DICTIONARY_SCREEN"
const val IS_SHOW_BANNER_CAMERA_SCREEN = "IS_SHOW_BANNER_CAMERA_SCREEN"
const val IS_SHOW_BANNER_CROP_SCREEN = "IS_SHOW_BANNER_CROP_SCREEN"
const val IS_SHOW_BANNER_CONVERSATION_SCREEN = "IS_SHOW_BANNER_CONVERSATION_SCREEN"
const val IS_SHOW_BANNER_DICTIONARY_DETAIL_SCREEN = "IS_SHOW_BANNER_DICTIONARY_DETAIL_SCREEN"
const val IS_SHOW_BANNER_LANGUAGE_SELECT_SCREEN = "IS_SHOW_BANNER_LANGUAGE_SELECT_SCREEN"
const val IS_SHOW_BANNER_LANGUAGE_SELECT_OCR_SCREEN = "IS_SHOW_BANNER_LANGUAGE_SELECT_OCR_SCREEN"
////^^^Banner Ads

const val isShowInterstitialLoader = "is_show_interstitial_loader"
const val IS_CHECK_SPLASH_AD = "IS_CHECK_SPLASH_AD"
const val OPEN_MIC_INTER_AD = "OPEN_MIC_INTER_AD"
const val OPEN_CAMERA_INTER_AD = "OPEN_CAMERA_INTER_AD"
const val OPEN_CONVERSATION_INTER_AD = "OPEN_CONVERSATION_INTER_AD"
const val OPEN_INPUT_INTER_AD = "OPEN_INPUT_INTER_AD"
const val DICTIONARY_SEARCH_INTER_AD = "DICTIONARY_SEARCH_INTER_AD"
const val OPEN_DICTIONARY_RECENT_INTER_AD = "OPEN_DICTIONARY_RECENT_INTER_AD"
const val CAMERA_BACK_INTER_AD = "CAMERA_BACK_INTER_AD"
const val CAMERA_CAPTURE_INTER_AD = "CAMERA_CAPTURE_INTER_AD"
const val CAMERA_RETAKE_INTER_AD = "CAMERA_RETAKE_INTER_AD"
const val CAMERA_TRANSLATE_RESULT_INTER_AD = "CAMERA_TRANSLATE_RESULT_INTER_AD"
const val CONVERSATION_BACK_INTER_AD = "CONVERSATION_BACK_INTER_AD"
const val OPEN_DICTIONARY_FAV_INTER_AD = "DICTIONARY_OPEN_FAV_INTER_AD"
const val DICTIONARY_RESULT_BACK_INTER_AD = "DICTIONARY_RESULT_BACK_INTER_AD"
const val FAVOURITE_BACK_INTER_AD = "FAVOURITE_BACK_INTER_AD"
const val HISTORY_BACK_INTER_AD = "HISTORY_BACK_INTER_AD"
const val INPUT_SCREEN_TRANSLATE_BTN_INTER_AD = "INPUT_SCREEN_TRANSLATE_BTN_INTER_AD"
const val INPUT_SCREEN_BACK_INTER_AD = "INPUT_SCREEN_BACK_INTER_AD"
const val LANGUAGE_SELECTION_BACK_INTER_AD = "LANGUAGE_SELECTION_BACK_INTER_AD"
const val LANGUAGE_SELECTION_LANG_SELECT_INTER_AD = "LANGUAGE_SELECTION_LANG_SELECT_INTER_AD"
const val OPEN_FAV_SCREEN_FROM_SETTING_INTER_AD = "OPEN_FAV_SCREEN_FROM_SETTING_INTER_AD"
const val OPEN_HISTORY_SCREEN_FROM_SETTING_INTER_AD = "OPEN_HISTORY_SCREEN_FROM_SETTING_INTER_AD"
*/
